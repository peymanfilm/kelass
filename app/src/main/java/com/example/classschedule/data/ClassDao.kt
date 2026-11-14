package com.example.classschedule.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * رابط دسترسی به داده‌ها (DAO)
 * Room به صورت خودکار پیاده‌سازی این توابع را تولید می‌کند.
 */
@Dao
interface ClassDao {

    // گرفتن همه کلاس‌ها به ترتیب روز و ساعت شروع
    // Flow باعث می‌شود هر تغییری در دیتابیس، به صورت خودکار در UI به‌روزرسانی شود
    @Query("SELECT * FROM class_schedule ORDER BY day ASC, startTime ASC")
    fun getAllClasses(): Flow<List<ClassSchedule>>

    // افزودن کلاس جدید
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(classSchedule: ClassSchedule)

    // ویرایش کلاس موجود
    @Update
    suspend fun update(classSchedule: ClassSchedule)

    // حذف کلاس
    @Delete
    suspend fun delete(classSchedule: ClassSchedule)

    // حذف همه کلاس‌ها
    @Query("DELETE FROM class_schedule")
    suspend fun deleteAll()
}
