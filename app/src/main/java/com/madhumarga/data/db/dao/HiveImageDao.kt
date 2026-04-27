package com.madhumarga.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.madhumarga.data.db.entity.HiveImage
import kotlinx.coroutines.flow.Flow

@Dao
interface HiveImageDao {
    @Query("SELECT * FROM hive_images WHERE hiveId = :hiveId ORDER BY addedAt DESC")
    fun getImagesForHive(hiveId: Long): Flow<List<HiveImage>>

    @Query("SELECT COUNT(*) FROM hive_images")
    fun getTotalImageCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: HiveImage): Long

    @Query("DELETE FROM hive_images WHERE id = :id")
    suspend fun deleteImage(id: Long)
}
