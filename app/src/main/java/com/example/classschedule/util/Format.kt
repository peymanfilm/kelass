package com.example.classschedule.util

import java.util.Locale

/**
 * تبدیل ارقام لاتین به ارقام فارسی برای نمایش زیباتر
 * مثلا "08:30" به "۰۸:۳۰" تبدیل می‌شود.
 */
fun String.toPersianDigits(): String = buildString {
    for (ch in this@toPersianDigits) {
        append(if (ch in '0'..'9') ('۰' + (ch - '0')) else ch)
    }
}

/**
 * ساخت رشته‌ی زمان با فرمت "HH:mm" (ارقام لاتین) از ساعت و دقیقه
 * مثلا (8, 30) به "08:30" تبدیل می‌شود.
 */
fun formatHourMinute(hour: Int, minute: Int): String =
    String.format(Locale.US, "%02d:%02d", hour, minute)
