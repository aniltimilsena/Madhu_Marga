package com.madhumarga.data.repository

import com.madhumarga.data.db.dao.InspectionDao
import com.madhumarga.data.db.entity.Inspection
import kotlinx.coroutines.flow.Flow

class InspectionRepository(private val inspectionDao: InspectionDao) {

    fun getInspectionsForHive(hiveId: Long): Flow<List<Inspection>> =
        inspectionDao.getInspectionsForHive(hiveId)

    fun getAllInspections(): Flow<List<Inspection>> = inspectionDao.getAllInspections()

    fun getRecentInspections(): Flow<List<Inspection>> = inspectionDao.getRecentInspections()

    suspend fun insertInspection(inspection: Inspection): Long =
        inspectionDao.insertInspection(inspection)

    suspend fun deleteInspection(id: Long) = inspectionDao.deleteInspection(id)
}
