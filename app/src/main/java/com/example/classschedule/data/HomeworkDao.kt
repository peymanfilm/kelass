package com.example.classschedule.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * رابط دسترسی به تکالیف در دیتابیس Room
 * Room به صورت خودکار پیاده‌سازی این توابع را تولید می‌کند.
 */
@Dao
interface HomeworkDao {

    // همه‌ی تکالیف: بازها اول و نزدیک‌ترین مهلت جلوتر؛ انجام‌شده‌ها آخر
    // Flow باعث می‌شود هر تغییری در دیتابیس، به صورت خودکار در UI به‌روزرسانی شود
    @Query(
        "SELECT * FROM homework " +
            "ORDER BY isDone ASC, dueYear ASC, dueMonth ASC, dueDay ASC, createdAt ASC"
    )
    fun getAllHomework(): Flow<List<Homework>>

    // افزودن تکلیف جدید
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(homework: Homework)

    // ویرایش تکلیف موجود
    @Update
    suspend fun update(homework: Homework)

    // حذف تکلیف
    @Delete
    suspend fun delete(homework: Homework)

    // حذف همه‌ی تکالیف
    @Query("DELETE FROM homework")
    suspend fun deleteAll()
}
