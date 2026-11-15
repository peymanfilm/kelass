package com.example.classschedule.util

import java.util.Locale
import com.example.classschedule.data.SubjectGrade
import kotlin.math.roundToInt

/**
 * تبدیل ارقام لاتین به ارقام فارسی برای نمایش زیباتر
 * مثلا "08:30" به «۰۸:۳۰» تبدیل می‌شود.
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

/**
 * قالب‌بندی نمره با ارقام لاتین و حداکثر دو رقم اعشار
 * مثلا 19.75 به "19.75" و 20.0 به "20" تبدیل می‌شود.
 */
fun formatGradeLatin(value: Float): String {
    val rounded = (value * 100f).roundToInt() / 100f
    return if (rounded == rounded.toInt().toFloat()) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

/**
 * قالب‌بندی نمره با ارقام فارسی و جداکننده‌ی اعشار «٫»
 * مثلا 19.75 به «۱۹٫۷۵» و 20.0 به «۲۰» تبدیل می‌شود.
 */
fun formatGradePersian(value: Float): String =
    formatGradeLatin(value).toPersianDigits().replace('.', '٫')

/**
 * خواندن نمره‌ی تایپ‌شده توسط کاربر — ارقام فارسی، عربی و لاتین پذیرفته می‌شود
 * و «٫» یا «,» هم جداکننده‌ی اعشار حساب می‌شود.
 * خروجی عددی بین ۰ تا ۲۰ است؛ اگر ورودی خالی یا نامعتبر باشد null برمی‌گردد.
 * ورودی خالی یعنی «پاک کردن نمره».
 */
fun parseGradeInput(raw: String): Float? {
    val normalized = raw.trim()
        .map { ch ->
            when (ch) {
                in '۰'..'۹' -> ('0' + (ch - '۰'))
                in '٠'..'٩' -> ('0' + (ch - '٠'))
                '٫' -> '.'
                else -> ch
            }
        }
        .joinToString("")
        .replace(',', '.')
    if (normalized.isEmpty()) return null
    val value = normalized.toFloatOrNull() ?: return null
    if (value < 0f || value > SubjectGrade.MAX_GRADE) return null
    return (value * 100f).roundToInt() / 100f
}
