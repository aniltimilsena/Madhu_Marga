package com.smarthive.manager.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HiveDao {
    @Query("SELECT * FROM hives WHERE userId = :userId")
    fun getAllHives(userId: String): Flow<List<Hive>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHive(hive: Hive)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateHive(hive: Hive)

    @Delete
    suspend fun deleteHive(hive: Hive)

    @Query("SELECT * FROM hives WHERE id = :id")
    suspend fun getHiveById(id: Int): Hive?

    @Query("SELECT * FROM hives WHERE isSynced = 0")
    suspend fun getUnsyncedHives(): List<Hive>

    // Claim all hives created offline (userId = "") for a real user after login
    @Query("UPDATE hives SET userId = :userId, isSynced = 0 WHERE userId = ''")
    suspend fun claimOfflineHives(userId: String)

    @Query("UPDATE harvests SET userId = :userId, isSynced = 0 WHERE userId = ''")
    suspend fun claimOfflineHarvests(userId: String)

    @Query("UPDATE inspections SET userId = :userId, isSynced = 0 WHERE userId = ''")
    suspend fun claimOfflineInspections(userId: String)

    // Harvests
    @Query("SELECT * FROM harvests WHERE userId = :userId ORDER BY id DESC")
    fun getAllHarvests(userId: String): Flow<List<Harvest>>

    @Query("SELECT * FROM harvests WHERE hiveId = :hiveId")
    fun getHarvestsForHive(hiveId: Int): Flow<List<Harvest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvest(harvest: Harvest)

    @Query("SELECT * FROM harvests WHERE isSynced = 0")
    suspend fun getUnsyncedHarvests(): List<Harvest>

    @Delete
    suspend fun deleteHarvest(harvest: Harvest)

    // Inspections
    @Query("SELECT * FROM inspections WHERE userId = :userId ORDER BY id DESC")
    fun getAllInspections(userId: String): Flow<List<Inspection>>

    @Query("SELECT * FROM inspections WHERE hiveId = :hiveId")
    fun getInspectionsForHive(hiveId: Int): Flow<List<Inspection>>

    @Query("SELECT * FROM inspections WHERE hiveId = :hiveId ORDER BY date DESC, id DESC")
    fun getInspectionsForHiveSorted(hiveId: Int): Flow<List<Inspection>>

    @Query("SELECT * FROM hives WHERE id = :id")
    fun getHiveByIdFlow(id: Int): Flow<Hive?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: Inspection): Long

    @Query("SELECT * FROM inspections WHERE isSynced = 0")
    suspend fun getUnsyncedInspections(): List<Inspection>

    @Delete
    suspend fun deleteInspection(inspection: Inspection)

    // User Profile
    @Query("SELECT * FROM user_profile WHERE userId = :userId")
    fun getUserProfile(userId: String): Flow<UserProfile?>

    /** One-shot fetch (not a Flow) — used to check existence before creating a default profile. */
    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfileOnce(userId: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    // Hive Images
    @Query("SELECT * FROM hive_images WHERE hiveId = :hiveId")
    fun getImagesForHive(hiveId: Int): Flow<List<HiveImage>>

    @Query("SELECT * FROM hive_images WHERE inspectionId = :inspectionId")
    fun getImagesForInspection(inspectionId: Int): Flow<List<HiveImage>>

    @Query("UPDATE hive_images SET inspectionId = :inspectionId WHERE id = :imageId")
    suspend fun updateImageInspectionId(imageId: Int, inspectionId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHiveImage(image: HiveImage): Long

    @Query("SELECT * FROM hive_images WHERE isSynced = 0")
    suspend fun getUnsyncedImages(): List<HiveImage>

    @Delete
    suspend fun deleteHiveImage(image: HiveImage)

    @Query("SELECT * FROM reminders ORDER BY date ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE hiveId = :hiveId")
    fun getRemindersForHive(hiveId: Int): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)
}
