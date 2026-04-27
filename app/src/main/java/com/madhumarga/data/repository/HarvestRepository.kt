package com.madhumarga.data.repository

import com.madhumarga.data.db.dao.HarvestDao
import com.madhumarga.data.db.entity.Harvest
import kotlinx.coroutines.flow.Flow

class HarvestRepository(private val harvestDao: HarvestDao) {

    fun getHarvestsForHive(hiveId: Long): Flow<List<Harvest>> =
        harvestDao.getHarvestsForHive(hiveId)

    fun getAllHarvests(): Flow<List<Harvest>> = harvestDao.getAllHarvests()

    fun getTotalHarvest(): Flow<Double?> = harvestDao.getTotalHarvest()

    fun getTotalHarvestForHive(hiveId: Long): Flow<Double?> =
        harvestDao.getTotalHarvestForHive(hiveId)

    suspend fun insertHarvest(harvest: Harvest): Long = harvestDao.insertHarvest(harvest)

    suspend fun deleteHarvest(id: Long) = harvestDao.deleteHarvest(id)
}
