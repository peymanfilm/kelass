package com.example.classschedule.util

import java.util.Calendar

/**
 * روزهای هفته بر اساس تقویم ایران (هفته از شنبه شروع می‌شود)
 */
object WeekDays {

    const val SATURDAY = 0
    const val SUNDAY = 1
    const val MONDAY = 2
    const val TUESDAY = 3
    const val WEDNESDAY = 4
    const val THURSDAY = 5
    const val FRIDAY = 6

    val names = listOf(
        "شنبه",
        "یک‌شنبه",
        "دوشنبه",
        "سه‌شنبه",
        "چهارشنبه",
        "پنج‌شنبه",
        "جمعه"
    )

    /**
     * شماره روزِ امروز هفته را بر اساس تقویم برمی‌گرداند
     * تا تب «امروز» به صورت خودکار انتخاب شود.
     */
    fun todayIndex(): Int {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> SATURDAY
            Calendar.SUNDAY -> SUNDAY
            Calendar.MONDAY -> MONDAY
            Calendar.TUESDAY -> TUESDAY
            Calendar.WEDNESDAY -> WEDNESDAY
            Calendar.THURSDAY -> THURSDAY
            Calendar.FRIDAY -> FRIDAY
            else -> SATURDAY
        }
    }

    /**
     * تاریخ شمسی روزهای هفته جاری؛ اندیس ۰ = شنبه این هفته تا اندیس ۶ = جمعه
     * برای نمایش زیر نام روزها (مثل ۱۴۰۵/۸/۲۴)
     */
    fun weekJalaliDates(): List<JalaliDate> {
        val calendar = Calendar.getInstance()
        // فاصله از شنبه (شروع هفته ایرانی): شنبه = ۰ … جمعه = ۶
        val daysSinceSaturday =
            (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SATURDAY + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, -daysSinceSaturday)
        val dates = ArrayList<JalaliDate>(7)
        repeat(7) {
            dates += gregorianToJalali(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return dates
    }
}
