package com.example.classschedule.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * دیتابیس اصلی Room — نسخه ۵: جدول حساب‌های کاربران (app_user) مثل وب‌سایت +
 * ستون «ثبت‌کننده» برای تکالیف
 * داده‌ها به صورت محلی و آفلاین ذخیره می‌شوند و با بستن برنامه پاک نمی‌شوند.
 * مهاجرت‌ها، داده‌های ثبت‌شده‌ی کاربر را بدون تغییر نگه می‌دارند.
 */
@Database(
    entities = [ClassSchedule::class, Homework::class, SubjectGrade::class, AppUser::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun classDao(): ClassDao

    abstract fun homeworkDao(): HomeworkDao

    abstract fun subjectGradeDao(): SubjectGradeDao

    abstract fun userDao(): UserDao

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

        /** مهاجرت نسخه ۳ به ۴: ساخت جدول کارنامه (نمره مستمر، نمره امتحان، منفی/مثبت دستی و قانون عکس هر درس) */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `subject_grade` (" +
                        "`name` TEXT NOT NULL, " +
                        "`continuousGrade` REAL, " +
                        "`examGrade` REAL, " +
                        "`manualNegatives` INTEGER NOT NULL DEFAULT 0, " +
                        "`manualPositives` INTEGER NOT NULL DEFAULT 0, " +
                        "`photoRequired` INTEGER NOT NULL DEFAULT 1, " +
                        "PRIMARY KEY(`name`)"
                )
            }
        }

        /**
         * مهاجرت نسخه ۴ به ۵: ساخت جدول حساب‌های کاربران (مدیریت چند دانش‌آموز، معلم و مدیر
         * مثل وب‌سایت) + ستون «ثبت‌کننده» برای تکالیف — داده‌های قبلی دست‌نخورده می‌مانند
         */
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `app_user` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`username` TEXT NOT NULL, " +
                        "`passwordHash` TEXT NOT NULL, " +
                        "`role` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_app_user_username` ON `app_user` (`username`)"
                )
                db.execSQL(
                    "ALTER TABLE `homework` ADD COLUMN `creatorName` TEXT NOT NULL DEFAULT ''"
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
