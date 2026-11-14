package com.example.classschedule.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * دیتابیس اصلی Room
 * داده‌ها به صورت محلی و آفلاین ذخیره می‌شوند و با بستن برنامه پاک نمی‌شوند.
 */
@Database(
    entities = [ClassSchedule::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun classDao(): ClassDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // الگوی Singleton: فقط یک نمونه از دیتابیس در کل برنامه ساخته می‌شود
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "class_schedule_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
