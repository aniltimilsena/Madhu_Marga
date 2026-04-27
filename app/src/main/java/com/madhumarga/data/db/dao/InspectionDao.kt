package com.madhumarga.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.madhumarga.data.db.entity.Inspection
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections WHERE hiveId = :hiveId ORDER BY date DESC")
    fun getInspectionsForHive(hiveId: Long): Flow<List<Inspection>>

    @Query("SELECT * FROM inspections ORDER BY date DESC")
    fun getAllInspections(): Flow<List<Inspection>>

    @Query("SELECT * FROM inspections ORDER BY date DESC LIMIT 10")
    fun getRecentInspections(): Flow<List<Inspection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: Inspection): Long

    @Query("DELETE FROM inspections WHERE id = :id")
    suspend fun deleteInspection(id: Long)
}
