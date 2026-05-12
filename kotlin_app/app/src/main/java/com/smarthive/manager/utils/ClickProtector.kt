package com.smarthive.manager.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * A simple click protector to prevent multiple rapid clicks.
 */
@Composable
fun rememberClickEnabled(debounceTime: Long = 1000L): Pair<Boolean, () -> Unit> {
    var enabled by remember { mutableStateOf(true) }
    
    val onClick = {
        if (enabled) {
            enabled = false
        }
    }

    LaunchedEffect(enabled) {
        if (!enabled) {
            delay(debounceTime)
            enabled = true
        }
    }

    return enabled to onClick
}

/**
 * ViewModel-level rate limiter helper
 */
class RateLimiter(private val thresholdMillis: Long = 2000L) {
    private var lastTimestamp: Long = 0L

    fun canExecute(): Boolean {
        val current = System.currentTimeMillis()
        return if (current - lastTimestamp >= thresholdMillis) {
            lastTimestamp = current
            true
        } else {
            false
        }
    }
}
