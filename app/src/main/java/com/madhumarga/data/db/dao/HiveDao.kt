package com.madhumarga.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.madhumarga.data.db.entity.Hive
import kotlinx.coroutines.flow.Flow

@Dao
interface HiveDao {
    @Query("SELECT * FROM hives ORDER BY createdAt DESC")
    fun getAllHives(): Flow<List<Hive>>

    @Query("SELECT * FROM hives WHERE id = :id")
    fun getHiveById(id: Long): Flow<Hive?>

    @Query("SELECT COUNT(*) FROM hives")
    fun getHiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM hives WHERE status = :status")
    fun getHiveCountByStatus(status: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHive(hive: Hive): Long

    @Update
    suspend fun updateHive(hive: Hive)

    @Delete
    suspend fun deleteHive(hive: Hive)

    @Query("DELETE FROM hives WHERE id = :id")
    suspend fun deleteHiveById(id: Long)
}
