package com.example.classschedule.util

/**
 * تبدیل ارقام لاتین به ارقام فارسی برای نمایش زیباتر
 * مثلا "08:30" به "۰۸:۳۰" تبدیل می‌شود.
 */
fun String.toPersianDigits(): String = buildString {
    for (ch in this@toPersianDigits) {
        append(if (ch in '0'..'9') ('۰' + (ch - '0')) else ch)
    }
}
