package com.madhumarga.data.repository

import com.madhumarga.data.db.dao.HiveDao
import com.madhumarga.data.db.entity.Hive
import kotlinx.coroutines.flow.Flow

class HiveRepository(private val hiveDao: HiveDao) {

    fun getAllHives(): Flow<List<Hive>> = hiveDao.getAllHives()

    fun getHiveById(id: Long): Flow<Hive?> = hiveDao.getHiveById(id)

    fun getHiveCount(): Flow<Int> = hiveDao.getHiveCount()

    suspend fun insertHive(hive: Hive): Long = hiveDao.insertHive(hive)

    suspend fun updateHive(hive: Hive) = hiveDao.updateHive(hive)

    suspend fun deleteHive(hive: Hive) = hiveDao.deleteHive(hive)

    suspend fun deleteHiveById(id: Long) = hiveDao.deleteHiveById(id)
}
