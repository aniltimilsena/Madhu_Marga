package com.smarthive.manager.data

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideHiveDao(db: AppDatabase): HiveDao {
        return db.hiveDao()
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return com.smarthive.manager.data.SupabaseClient.client
    }
}
