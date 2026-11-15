package com.example.classschedule.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * دیتابیس اصلی Room — نسخه ۳: افزوده شدن ستون عکس‌های تکلیف (imageNames)
 * داده‌ها به صورت محلی و آفلاین ذخیره می‌شوند و با بستن برنامه پاک نمی‌شوند.
 * مهاجرت‌ها، داده‌های ثبت‌شده‌ی کاربر را بدون تغییر نگه می‌دارند.
 */
@Database(
    entities = [ClassSchedule::class, Homework::class],
    version = 3,
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

        /** مهاجرت نسخه ۲ به ۳: افزودن ستون عکس‌های تکلیف (نام فایل‌ها جدا شده با ;) */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `homework` ADD COLUMN `imageNames` TEXT NOT NULL DEFAULT ''"
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
