package com.smarthive.manager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.Image
import com.smarthive.manager.R
import com.smarthive.manager.data.AuthViewModel
import com.smarthive.manager.data.AuthState
import com.smarthive.manager.ui.theme.Primary
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import com.smarthive.manager.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: androidx.navigation.NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account.idToken != null) {
                viewModel.signInWithGoogle(account.idToken!!)
            } else {
                errorMessage = "Google Sign-In failed: No ID Token received."
                showErrorDialog = true
            }
        } catch (e: com.google.android.gms.common.api.ApiException) {
            val statusCode = e.statusCode
            errorMessage = "Google Sign-In failed (Code $statusCode): " + when (statusCode) {
                10 -> "Developer Error. Please ensure your Android app's SHA-1 fingerprint is registered in the Google Cloud Console for this Web Client ID."
                12501 -> "Sign-in cancelled by user."
                else -> e.message ?: "Unknown error"
            }
            showErrorDialog = true
        } catch (e: Exception) {
            errorMessage = "Google Sign-In failed: ${e.localizedMessage}"
            showErrorDialog = true
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        } else if (authState is AuthState.Error) {
            val rawError = (authState as AuthState.Error).message
            errorMessage = when {
                rawError.contains("Unable to resolve host", ignoreCase = true) -> 
                    "Network Connection Error: It seems you're offline. Madhu-Marga works offline with your local data, but synchronization requires an internet connection."
                rawError.contains("invalid claim", ignoreCase = true) ->
                    "Authentication Error: Your session has expired. Please log in again."
                else -> rawError
            }
            showErrorDialog = true
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Authentication Issue") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showErrorDialog = false
                    navController.navigate("dashboard")
                }) {
                    Text("Skip for Now")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Madhu Marga Logo",
                modifier = Modifier.size(110.dp)
            )
            Text("Care · Monitor · Thrive", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("e.g. name@example.com", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("Your password", style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Login")
            }

            TextButton(onClick = { navController.navigate("sign_up") }) {
                Text("Don't have an account? Sign Up")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue with Google")
            }



        }
    }
}
