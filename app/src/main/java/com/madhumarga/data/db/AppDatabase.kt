package com.madhumarga.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.madhumarga.data.db.dao.ApiaryDao
import com.madhumarga.data.db.dao.HarvestDao
import com.madhumarga.data.db.dao.HiveDao
import com.madhumarga.data.db.dao.HiveImageDao
import com.madhumarga.data.db.dao.InspectionDao
import com.madhumarga.data.db.dao.UserProfileDao
import com.madhumarga.data.db.entity.Apiary
import com.madhumarga.data.db.entity.Harvest
import com.madhumarga.data.db.entity.Hive
import com.madhumarga.data.db.entity.HiveImage
import com.madhumarga.data.db.entity.Inspection
import com.madhumarga.data.db.entity.UserProfile

@Database(
    entities = [
        Hive::class,
        Inspection::class,
        Harvest::class,
        HiveImage::class,
        UserProfile::class,
        Apiary::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hiveDao(): HiveDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun harvestDao(): HarvestDao
    abstract fun hiveImageDao(): HiveImageDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun apiaryDao(): ApiaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "madhu_marga_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
