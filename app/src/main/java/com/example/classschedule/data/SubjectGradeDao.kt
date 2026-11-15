package com.example.classschedule.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * رابط دسترسی به جدول کارنامه (نمره و قانون عکس هر درس) در دیتابیس Room
 */
@Dao
interface SubjectGradeDao {

    // همه‌ی درس‌های کارنامه به ترتیب نام؛ Flow به‌روزرسانی خودکار UI را تضمین می‌کند
    @Query("SELECT * FROM subject_grade ORDER BY name ASC")
    fun getAll(): Flow<List<SubjectGrade>>

    // فقط اگر درس از قبل وجود نداشته باشد اضافه می‌شود؛
    // نمره و تنظیمات قبلی درس‌های موجود دست‌نخورده می‌مانند
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(grade: SubjectGrade)

    // ثبت یا پاک کردن نمره مستمر یک درس (grade = null یعنی پاک کردن نمره)
    @Query("UPDATE subject_grade SET continuousGrade = :grade WHERE name = :name")
    suspend fun setContinuousGrade(name: String, grade: Float?)

    // ثبت یا پاک کردن نمره امتحان یک درس (grade = null یعنی پاک کردن نمره)
    @Query("UPDATE subject_grade SET examGrade = :grade WHERE name = :name")
    suspend fun setExamGrade(name: String, grade: Float?)

    // افزودن/کم کردن منفی دستی معلم — هرگز از صفر پایین‌تر نمی‌رود
    @Query(
        "UPDATE subject_grade SET manualNegatives = " +
            "CASE WHEN manualNegatives + :delta < 0 THEN 0 ELSE manualNegatives + :delta END " +
            "WHERE name = :name"
    )
    suspend fun adjustManualNegatives(name: String, delta: Int)

    // افزودن/کم کردن مثبت دستی معلم — هرگز از صفر پایین‌تر نمی‌رود
    @Query(
        "UPDATE subject_grade SET manualPositives = " +
            "CASE WHEN manualPositives + :delta < 0 THEN 0 ELSE manualPositives + :delta END " +
            "WHERE name = :name"
    )
    suspend fun adjustManualPositives(name: String, delta: Int)

    // تغییر قانون عکس اجباری برای یک درس
    @Query("UPDATE subject_grade SET photoRequired = :required WHERE name = :name")
    suspend fun setPhotoRequired(name: String, required: Boolean)

    // حذف یک درس از کارنامه (تکالیف آن درس حذف نمی‌شوند)
    @Query("DELETE FROM subject_grade WHERE name = :name")
    suspend fun delete(name: String)
}
