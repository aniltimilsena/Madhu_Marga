package com.madhumarga.data.repository

import com.madhumarga.data.db.dao.UserProfileDao
import com.madhumarga.data.db.entity.UserProfile
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    fun getProfile(): Flow<UserProfile?> = userProfileDao.getProfile()

    suspend fun upsertProfile(profile: UserProfile) = userProfileDao.upsertProfile(profile)
}
