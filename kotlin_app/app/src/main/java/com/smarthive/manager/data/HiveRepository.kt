package com.smarthive.manager.data

import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ExperimentalCoroutinesApi

import javax.inject.Inject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus

@OptIn(ExperimentalCoroutinesApi::class)
class HiveRepository @Inject constructor(
    private val hiveDao: HiveDao,
    private val supabase: SupabaseClient,
    @ApplicationContext private val context: Context
) {
    private fun getCurrentUserId(): String {
        return supabase.auth.currentSessionOrNull()?.user?.id ?: ""
    }

    /** Flow of the current auth userId — emits a new value whenever the session changes. */
    private val userIdFlow: Flow<String> = flow {
        supabase.auth.sessionStatus.collect { status ->
            val id = when (status) {
                is SessionStatus.Authenticated -> status.session.user?.id ?: ""
                else -> ""
            }
            // When a real user logs in, claim any data they created while offline
            if (id.isNotEmpty()) {
                claimOfflineData(id)
            }
            emit(id)
        }
    }

    /**
     * Claims all locally-created offline records (userId = "") for the newly-authenticated user.
     * This is a simple bulk UPDATE — fast, safe, and runs only once per login.
     */
    private suspend fun claimOfflineData(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                hiveDao.claimOfflineHives(userId)
                hiveDao.claimOfflineHarvests(userId)
                hiveDao.claimOfflineInspections(userId)
                android.util.Log.d("HiveRepository", "Offline data claimed for user: $userId")
            } catch (e: Exception) {
                android.util.Log.e("HiveRepository", "Failed to claim offline data: ${e.message}", e)
            }
        }
    }

    /**
     * Public entry point for the ViewModel to explicitly trigger
     * offline-data claiming + Supabase sync after a user logs in.
     */
    suspend fun claimAndSync() {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            claimOfflineData(userId)
            syncPendingData()
        }
    }

    val allHives: Flow<List<Hive>> = userIdFlow.flatMapLatest { uid ->
        hiveDao.getAllHives(uid)
    }

    suspend fun refreshHives() {
        withContext(Dispatchers.IO) {
            syncPendingData() // Try to push local changes first
            try {
                val userId = getCurrentUserId()
                if (userId.isEmpty()) return@withContext
                
                val supabaseHives = supabase.from("hives").select {
                    filter { eq("userId", userId) }
                }.decodeList<Hive>()
                
                supabaseHives.forEach { remoteHive ->
                    val localHive = hiveDao.getHiveById(remoteHive.id)
                    // Conflict Resolution: Only overwrite if remote is newer AND local is already synced,
                    // OR if local doesn't exist.
                    if (localHive == null || (localHive.isSynced && remoteHive.lastModified > localHive.lastModified)) {
                        hiveDao.insertHive(remoteHive.copy(isSynced = true))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun insertHive(hive: Hive) {
        withContext(Dispatchers.IO) {
            val userId = getCurrentUserId()
            // 1. Save to Room with isSynced = false
            val hiveWithUser = hive.copy(userId = userId, isSynced = false)
            hiveDao.insertHive(hiveWithUser)
            
            // 2. Try to sync to Supabase if authenticated
            if (userId.isNotEmpty()) {
                try {
                    supabase.from("hives").upsert(hiveWithUser)
                    hiveDao.insertHive(hiveWithUser.copy(isSynced = true))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun updateHive(hive: Hive) {
        withContext(Dispatchers.IO) {
            val updatedHive = hive.copy(isSynced = false, lastModified = System.currentTimeMillis())
            hiveDao.updateHive(updatedHive)
            
            val userId = getCurrentUserId()
            if (userId.isNotEmpty()) {
                try {
                    supabase.from("hives").update(updatedHive) {
                        filter { eq("id", hive.id) }
                    }
                    hiveDao.updateHive(updatedHive.copy(isSynced = true))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    suspend fun deleteHive(hive: Hive) {
        withContext(Dispatchers.IO) {
            hiveDao.deleteHive(hive)
            try {
                supabase.from("hives").delete {
                    filter {
                        eq("id", hive.id)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- Harvests ---
    val allHarvests: Flow<List<Harvest>> = userIdFlow.flatMapLatest { uid ->
        hiveDao.getAllHarvests(uid)
    }

    suspend fun refreshHarvests() {
        withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                val supabaseHarvests = supabase.from("harvests").select {
                    filter { eq("userId", userId) }
                }.decodeList<Harvest>()
                supabaseHarvests.forEach { hiveDao.insertHarvest(it) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun insertHarvest(harvest: Harvest) {
        withContext(Dispatchers.IO) {
            val userId = getCurrentUserId()
            val harvestToSave = harvest.copy(userId = userId, isSynced = false)
            hiveDao.insertHarvest(harvestToSave)
            
            if (userId.isNotEmpty()) {
                try {
                    supabase.from("harvests").upsert(harvestToSave)
                    hiveDao.insertHarvest(harvestToSave.copy(isSynced = true))
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    suspend fun deleteHarvest(harvest: Harvest) {
        withContext(Dispatchers.IO) {
            hiveDao.deleteHarvest(harvest)
            try {
                supabase.from("harvests").delete {
                    filter { eq("id", harvest.id) }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- Inspections ---
    val allInspections: Flow<List<Inspection>> = userIdFlow.flatMapLatest { uid ->
        hiveDao.getAllInspections(uid)
    }

    suspend fun refreshInspections() {
        withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                val supabaseInspections = supabase.from("inspections").select {
                    filter { eq("userId", userId) }
                }.decodeList<Inspection>()
                supabaseInspections.forEach { hiveDao.insertInspection(it) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun insertInspection(inspection: Inspection): Int {
        return withContext(Dispatchers.IO) {
            val userId = getCurrentUserId()
            val inspectionToSave = inspection.copy(userId = userId, isSynced = false)
            val id = hiveDao.insertInspection(inspectionToSave).toInt()
            
            if (userId.isNotEmpty()) {
                try {
                    supabase.from("inspections").upsert(inspectionToSave.copy(id = id))
                    hiveDao.insertInspection(inspectionToSave.copy(id = id, isSynced = true))
                } catch (e: Exception) { e.printStackTrace() }
            }
            id
        }
    }

    fun getImagesForInspection(inspectionId: Int): Flow<List<HiveImage>> {
        return hiveDao.getImagesForInspection(inspectionId)
    }

    fun getInspectionsForHive(hiveId: Int): Flow<List<Inspection>> {
        return hiveDao.getInspectionsForHiveSorted(hiveId)
    }

    fun getHiveByIdFlow(hiveId: Int): Flow<Hive?> {
        return hiveDao.getHiveByIdFlow(hiveId)
    }

    suspend fun updateImageInspectionId(imageId: Int, inspectionId: Int) {
        withContext(Dispatchers.IO) {
            hiveDao.updateImageInspectionId(imageId, inspectionId)
        }
    }

    suspend fun deleteInspection(inspection: Inspection) {
        withContext(Dispatchers.IO) {
            hiveDao.deleteInspection(inspection)
            try {
                supabase.from("inspections").delete {
                    filter { eq("id", inspection.id) }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun syncPendingData() {
        withContext(Dispatchers.IO) {
            val userId = getCurrentUserId()
            if (userId.isEmpty()) return@withContext

            try {
                // Sync Hives
                hiveDao.getUnsyncedHives().forEach { hive ->
                    supabase.from("hives").upsert(hive)
                    hiveDao.updateHive(hive.copy(isSynced = true))
                }
                // Sync Harvests
                hiveDao.getUnsyncedHarvests().forEach { harvest ->
                    supabase.from("harvests").upsert(harvest)
                    hiveDao.insertHarvest(harvest.copy(isSynced = true))
                }
                // Sync Inspections
                hiveDao.getUnsyncedInspections().forEach { inspection ->
                    supabase.from("inspections").upsert(inspection)
                    hiveDao.insertInspection(inspection.copy(isSynced = true))
                }
                // Sync Images
                hiveDao.getUnsyncedImages().forEach { image ->
                    if (!image.imageUri.startsWith("http")) {
                        uploadImage(image)
                    } else {
                        hiveDao.insertHiveImage(image.copy(isSynced = true))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun uploadImage(image: HiveImage) {
        try {
            val uri = Uri.parse(image.imageUri)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                val fileName = "hive_${image.hiveId}_${System.currentTimeMillis()}.jpg"
                val bucket = supabase.storage.from("hive-images")
                
                try {
                    bucket.upload(fileName, bytes)
                    val publicUrl = bucket.publicUrl(fileName)
                    
                    // Update local DB with public URL and synced status
                    hiveDao.insertHiveImage(image.copy(imageUri = publicUrl, isSynced = true))
                    android.util.Log.d("HiveRepository", "Successfully uploaded image: $fileName")
                } catch (e: Exception) {
                    android.util.Log.e("HiveRepository", "Supabase storage upload failed: ${e.message}", e)
                    // Keep isSynced = false so it retries later
                }
            } else {
                android.util.Log.e("HiveRepository", "Failed to read bytes from URI: ${image.imageUri}")
            }
        } catch (e: Exception) {
            android.util.Log.e("HiveRepository", "Image processing failed: ${e.message}", e)
        }
    }

    // --- User Profile ---
    val userProfile: Flow<UserProfile?> = userIdFlow.flatMapLatest { uid ->
        hiveDao.getUserProfile(uid)
    }

    /** One-shot suspend fetch — used by AuthViewModel to avoid overwriting existing profiles. */
    suspend fun getUserProfileOnce(userId: String): UserProfile? {
        return withContext(Dispatchers.IO) {
            hiveDao.getUserProfileOnce(userId)
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        withContext(Dispatchers.IO) {
            val userId = getCurrentUserId()
            hiveDao.saveUserProfile(profile.copy(userId = userId))
        }
    }

    // --- Hive Images ---
    fun getImagesForHive(hiveId: Int): Flow<List<HiveImage>> = hiveDao.getImagesForHive(hiveId)

    suspend fun insertHiveImage(image: HiveImage): Int {
        return withContext(Dispatchers.IO) {
            val userId = getCurrentUserId()
            val imageToSave = image.copy(userId = userId, isSynced = false)
            val id = hiveDao.insertHiveImage(imageToSave).toInt()
            
            if (userId.isNotEmpty()) {
                uploadImage(imageToSave.copy(id = id))
            }
            id
        }
    }

    suspend fun deleteHiveImage(image: HiveImage) {
        withContext(Dispatchers.IO) {
            hiveDao.deleteHiveImage(image)
            try {
                if (image.imageUri.startsWith("http")) {
                    val fileName = image.imageUri.substringAfterLast("/")
                    supabase.storage.from("hive-images").delete(fileName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getRemindersForHive(hiveId: Int): Flow<List<Reminder>> = hiveDao.getRemindersForHive(hiveId)

    suspend fun insertReminder(reminder: Reminder) {
        withContext(Dispatchers.IO) {
            hiveDao.insertReminder(reminder)
        }
    }

    suspend fun deleteReminder(reminder: Reminder) {
        withContext(Dispatchers.IO) {
            hiveDao.deleteReminder(reminder)
        }
    }
}
