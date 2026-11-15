package com.example.classschedule.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * دیتابیس اصلی Room — نسخه ۲: افزوده شدن جدول تکالیف (homework)
 * داده‌ها به صورت محلی و آفلاین ذخیره می‌شوند و با بستن برنامه پاک نمی‌شوند.
 * مهاجرت ۱ به ۲، کلاس‌های ثبت‌شده‌ی کاربر را بدون تغییر نگه می‌دارد.
 */
@Database(
    entities = [ClassSchedule::class, Homework::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun classDao(): ClassDao

    abstract fun homeworkDao(): HomeworkDao

    companion object {

        /** مهاجرت نسخه ۱ به ۲: ساخت جدول تکالیف بدون دست‌زدن به داده‌های کلاس‌ها */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `homework` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`subject` TEXT NOT NULL, " +
                        "`dueYear` INTEGER NOT NULL, " +
                        "`dueMonth` INTEGER NOT NULL, " +
                        "`dueDay` INTEGER NOT NULL, " +
                        "`dueTime` TEXT NOT NULL, " +
                        "`note` TEXT NOT NULL, " +
                        "`isDone` INTEGER NOT NULL, " +
                        "`colorIndex` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        // الگوی Singleton: فقط یک نمونه از دیتابیس در کل برنامه ساخته می‌شود
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "class_schedule_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
