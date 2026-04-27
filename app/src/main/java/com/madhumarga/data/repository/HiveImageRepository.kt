package com.madhumarga.data.repository

import com.madhumarga.data.db.dao.HiveImageDao
import com.madhumarga.data.db.entity.HiveImage
import kotlinx.coroutines.flow.Flow

class HiveImageRepository(private val hiveImageDao: HiveImageDao) {

    fun getImagesForHive(hiveId: Long): Flow<List<HiveImage>> =
        hiveImageDao.getImagesForHive(hiveId)

    fun getTotalImageCount(): Flow<Int> = hiveImageDao.getTotalImageCount()

    suspend fun insertImage(image: HiveImage): Long = hiveImageDao.insertImage(image)

    suspend fun deleteImage(id: Long) = hiveImageDao.deleteImage(id)
}
