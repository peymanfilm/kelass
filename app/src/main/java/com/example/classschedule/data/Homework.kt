package com.example.classschedule.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.classschedule.util.JalaliDate

/**
 * مدل داده‌ی تکلیف: یک یادآور تکلیف درسی
 * به صورت جدولی به نام homework در دیتابیس Room ذخیره می‌شود.
 * تاریخ مهلت به شکل شمسی (جلالی) ذخیره می‌شود تا نمایش و مقایسه‌ی تاریخ‌ها ساده باشد.
 */
@Entity(tableName = "homework")
data class Homework(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,          // عنوان تکلیف (الزامی)
    val subject: String = "",   // نام درس مربوطه (اختیاری)
    val dueYear: Int,           // سال شمسی مهلت؛ مثل ۱۴۰۵
    val dueMonth: Int,          // ماه شمسی مهلت (۱ تا ۱۲)
    val dueDay: Int,            // روز شمسی مهلت
    val dueTime: String = "",   // ساعت مهلت "HH:mm" — خالی یعنی بدون ساعت
    val note: String = "",      // توضیحات تکلیف (اختیاری)
    val isDone: Boolean = false,// آیا انجام شده است؟
    val colorIndex: Int = 0,    // شماره رنگ کارت (برای تفکیک بصری تکالیف)
    val createdAt: Long = 0L    // زمان ثبت به میلی‌ثانیه
) {
    /** تاریخ شمسی مهلت به‌شکل JalaliDate برای نمایش و مقایسه */
    val dueJalali: JalaliDate
        get() = JalaliDate(year = dueYear, month = dueMonth, day = dueDay)
}
