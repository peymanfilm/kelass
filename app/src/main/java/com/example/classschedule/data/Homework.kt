package com.example.classschedule.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.classschedule.util.JalaliDate

/**
 * مدل داده‌ی تکلیف: یک یادآور تکلیف درسی
 * به صورت جدولی به نام homework در دیتابیس Room ذخیره می‌شود.
 * تاریخ مهلت به شکل شمسی (جلالی) ذخیره می‌شود تا نمایش و مقایسه‌ی تاریخ‌ها ساده باشد.
 * عکس‌های تکلیف انجام‌شده در حافظه‌ی داخلی برنامه ذخیره و نام فایل‌ها اینجا نگه‌داری می‌شود.
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
    // نام فایل عکس‌های تکلیف جدا شده با «;» — خود فایل‌ها در پوشه‌ی homework_images حافظه‌ی برنامه هستند
    @ColumnInfo(defaultValue = "")
    val imageNames: String = "",
    // نام نمایشی کسی که تکلیف را ثبت کرده (معلم یا دانش‌آموز) — مثل «ثبت‌کننده» در وب‌سایت
    @ColumnInfo(defaultValue = "")
    val creatorName: String = "",
    val createdAt: Long = 0L    // زمان ثبت به میلی‌ثانیه
) {
    /** تاریخ شمسی مهلت به‌شکل JalaliDate برای نمایش و مقایسه */
    val dueJalali: JalaliDate
        get() = JalaliDate(year = dueYear, month = dueMonth, day = dueDay)

    /** فهرست نام فایل عکس‌های این تکلیف به ترتیب افزودن */
    val imageList: List<String>
        get() = imageNames.split(";").map { it.trim() }.filter { it.isNotEmpty() }

    /**
     * قانون انجام‌شدن تکلیف: تکلیف فقط وقتی «انجام‌شده» واقعی است
     * که کاربر علامت تیک زده باشد و حداقل یک عکس از تکلیف بارگذاری کرده باشد.
     * بدون عکس، تکلیف همیشه در فهرست «باز» می‌ماند و عنوانش خط نمی‌خورد.
     */
    val isEffectivelyDone: Boolean
        get() = isDone && imageList.isNotEmpty()
}
