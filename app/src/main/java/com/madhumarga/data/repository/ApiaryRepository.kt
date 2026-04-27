package com.madhumarga.data.repository

import com.madhumarga.data.db.dao.ApiaryDao
import com.madhumarga.data.db.entity.Apiary
import kotlinx.coroutines.flow.Flow

class ApiaryRepository(private val apiaryDao: ApiaryDao) {

    fun getAllApiaries(): Flow<List<Apiary>> = apiaryDao.getAllApiaries()

    fun getApiaryById(id: Long): Flow<Apiary?> = apiaryDao.getApiaryById(id)

    suspend fun insertApiary(apiary: Apiary): Long = apiaryDao.insertApiary(apiary)

    suspend fun updateApiary(apiary: Apiary) = apiaryDao.updateApiary(apiary)

    suspend fun deleteApiary(apiary: Apiary) = apiaryDao.deleteApiary(apiary)
}
