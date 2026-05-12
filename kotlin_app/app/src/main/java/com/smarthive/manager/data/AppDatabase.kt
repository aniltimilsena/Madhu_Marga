package com.smarthive.manager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Hive::class, Harvest::class, Inspection::class, UserProfile::class, HiveImage::class, Reminder::class],
    version = 9,          // v9: added indices for performance
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hiveDao(): HiveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create User Profile table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_profile` (
                        `userId` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `email` TEXT NOT NULL, 
                        `location` TEXT NOT NULL, 
                        `experience` TEXT NOT NULL, 
                        `pushEnabled` INTEGER NOT NULL, 
                        `emailEnabled` INTEGER NOT NULL, 
                        `tempAlerts` INTEGER NOT NULL, 
                        `humidityAlerts` INTEGER NOT NULL, 
                        `harvestReminders` INTEGER NOT NULL, 
                        `marketingEnabled` INTEGER NOT NULL, 
                        `imageUri` TEXT, 
                        PRIMARY KEY(`userId`)
                    )
                """.trimIndent())

                // Create Hive Images table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `hive_images` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `hiveId` INTEGER NOT NULL, 
                        `imageUri` TEXT NOT NULL, 
                        `date` TEXT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `isSynced` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create Reminders table (original — may have wrong schema on some devices)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reminders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `hiveId` INTEGER NOT NULL, 
                        `title` TEXT NOT NULL, 
                        `message` TEXT NOT NULL, 
                        `dateMillis` INTEGER NOT NULL, 
                        `isCompleted` INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        // v7 → v8: Recreate reminders table with correct schema (date TEXT, description TEXT)
        // The v5→6 migration created wrong columns (dateMillis/title/message);
        // the Reminder entity uses date/description. This corrects that mismatch.
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `reminders`")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reminders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `hiveId` INTEGER NOT NULL,
                        `date` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE hive_images ADD COLUMN inspectionId INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smarthive_db"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .fallbackToDestructiveMigration() // Safety net if migration chain breaks
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
