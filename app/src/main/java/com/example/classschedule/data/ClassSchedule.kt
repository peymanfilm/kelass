package com.example.classschedule.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * مدل داده اصلی برنامه: یک کلاس هفتگی
 * این کلاس به صورت جدولی به نام class_schedule در دیتابیس Room ذخیره می‌شود.
 */
@Entity(tableName = "class_schedule")
data class ClassSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,        // نام درس
    val day: Int,            // شماره روز هفته: 0 = شنبه ... 6 = جمعه
    val startTime: String,   // ساعت شروع با فرمت "HH:mm" مثلا "08:30"
    val endTime: String,     // ساعت پایان با فرمت "HH:mm"
    val teacher: String = "",// نام استاد (اختیاری)
    val room: String = "",   // شماره یا نام کلاس/اتاق (اختیاری)
    val colorIndex: Int = 0  // شماره رنگ کارت (برای تفکیک بصری کلاس‌ها)
)
