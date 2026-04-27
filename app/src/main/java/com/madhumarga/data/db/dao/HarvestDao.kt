package com.madhumarga.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.madhumarga.data.db.entity.Harvest
import kotlinx.coroutines.flow.Flow

@Dao
interface HarvestDao {
    @Query("SELECT * FROM harvests WHERE hiveId = :hiveId ORDER BY date DESC")
    fun getHarvestsForHive(hiveId: Long): Flow<List<Harvest>>

    @Query("SELECT * FROM harvests ORDER BY date DESC")
    fun getAllHarvests(): Flow<List<Harvest>>

    @Query("SELECT SUM(quantityKg) FROM harvests")
    fun getTotalHarvest(): Flow<Double?>

    @Query("SELECT SUM(quantityKg) FROM harvests WHERE hiveId = :hiveId")
    fun getTotalHarvestForHive(hiveId: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvest(harvest: Harvest): Long

    @Query("DELETE FROM harvests WHERE id = :id")
    suspend fun deleteHarvest(id: Long)
}
