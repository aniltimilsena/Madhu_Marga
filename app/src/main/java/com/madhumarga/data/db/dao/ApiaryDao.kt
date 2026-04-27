package com.madhumarga.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.madhumarga.data.db.entity.Apiary
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiaryDao {
    @Query("SELECT * FROM apiaries ORDER BY createdAt DESC")
    fun getAllApiaries(): Flow<List<Apiary>>

    @Query("SELECT * FROM apiaries WHERE id = :id")
    fun getApiaryById(id: Long): Flow<Apiary?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiary(apiary: Apiary): Long

    @Update
    suspend fun updateApiary(apiary: Apiary)

    @Delete
    suspend fun deleteApiary(apiary: Apiary)
}
