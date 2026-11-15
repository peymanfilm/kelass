package com.example.classschedule.util

import java.util.Calendar

/**
 * تاریخ شمسی (جلالی): سال / ماه / روز
 * تبدیل میلادی به شمسی با الگوریتم استاندارد جلالی — دقیق و بدون کتابخانه خارجی
 */
data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    /** نمایش خلاصه با ارقام فارسی، مثل ۱۴۰۵/۸/۲۴ */
    fun toCompactString(): String = "$year/$month/$day".toPersianDigits()

    /** نام ماه شمسی این تاریخ؛ مثل «آذر» */
    fun monthName(): String = JALALI_MONTHS[(month - 1).coerceIn(0, JALALI_MONTHS.size - 1)]
}

/** نام ماه‌های سال شمسی */
val JALALI_MONTHS = listOf(
    "فروردین", "اردیبهشت", "خرداد",
    "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر",
    "دی", "بهمن", "اسفند"
)

/** تاریخ شمسی امروز */
fun jalaliToday(): JalaliDate {
    val calendar = Calendar.getInstance()
    return gregorianToJalali(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

/** تاریخ شمسیِ n روز بعد (مثبت) یا قبل (منفی) از امروز؛ مثلا ۱ = فردا */
fun jalaliTodayPlus(daysAhead: Int): JalaliDate {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysAhead)
    return gregorianToJalali(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

/** آیا سال شمسی کبیسه است؟ */
fun isJalaliLeap(jy: Int): Boolean = jalCal(jy).leap == 0

/** تعداد روزهای یک ماه شمسی (۶ ماه اول ۳۱ روز، ماه‌های ۷ تا ۱۱ سی روز، اسفند ۲۹ یا ۳۰ روز) */
fun jalaliMonthLength(jy: Int, jm: Int): Int = when {
    jm <= 6 -> 31
    jm <= 11 -> 30
    else -> if (isJalaliLeap(jy)) 30 else 29
}

/** فاصله‌ی روز بین دو تاریخ شمسی (مثبت = تاریخ دوم بعد از اول) */
fun daysBetween(from: JalaliDate, to: JalaliDate): Int =
    jalaliToJdn(to.year, to.month, to.day) - jalaliToJdn(from.year, from.month, from.day)

/** شماره‌ی روز مطلق (JDN) از تاریخ شمسی — برای مقایسه و فاصله‌گیری تاریخ‌ها */
private fun jalaliToJdn(jy: Int, jm: Int, jd: Int): Int {
    val info = jalCal(jy)
    return g2d(jy + 621, 3, info.marchDay) + (jm - 1) * 31 - div(jm, 7) * (jm - 7) + jd - 1
}

/** تبدیل تاریخ میلادی به تاریخ شمسی؛ مثلا 2026-11-15 به 1405/8/24 */
fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
    val jdn = g2d(gy, gm, gd)
    val gregorianYear = d2g(jdn).first
    var jy = gregorianYear - 621
    val info = jalCal(jy)
    val jdn1f = g2d(gy, 3, info.marchDay)
    var k = jdn - jdn1f
    if (k >= 0) {
        if (k <= 185) {
            // ۶ ماه اول سال: فروردین تا شهریور (۳۱ روزه)
            return JalaliDate(jy, 1 + div(k, 31), mod(k, 31) + 1)
        }
        k -= 186
    } else {
        // تاریخ قبل از اول فروردین → سال شمسی قبل
        jy -= 1
        k += 179
        if (info.leap == 1) k += 1
    }
    // ۶ ماه دوم سال: مهر تا اسفند (۳۰ روزه)
    return JalaliDate(jy, 7 + div(k, 30), mod(k, 30) + 1)
}

/** تقسیم صحیح هم‌معنای الگوریتم مرجع */
private fun div(a: Int, b: Int): Int = a / b

/** باقیمانده هم‌معنای الگوریتم مرجع */
private fun mod(a: Int, b: Int): Int = a - b * (a / b)

/** ثابت‌های الگوریتم استاندارد جلالی */
private val BREAKS = intArrayOf(
    -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
    1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
)

/** اطلاعات سال شمسی: کبیسته بودن + روز اسفند (ماه مارس) */
private data class JalaliCalendarInfo(val leap: Int, val marchDay: Int)

private fun jalCal(jy: Int): JalaliCalendarInfo {
    var leapJ = -14
    var jp = BREAKS[0]
    val gy = jy + 621
    var jump = 0
    for (i in 1 until BREAKS.size) {
        val jm = BREAKS[i]
        jump = jm - jp
        if (jy < jm) break
        leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
        jp = jm
    }
    var n = jy - jp
    leapJ += div(n, 33) * 8 + div(mod(n, 33) + 3, 4)
    if (mod(jump, 33) == 4 && jump - n == 4) leapJ += 1
    val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
    val march = 20 + leapJ - leapG
    if (jump - n < 6) n = n - jump + div(jump + 4, 33) * 33
    var leap = mod(mod(n + 1, 33) - 1, 4)
    if (leap == -1) leap = 4
    return JalaliCalendarInfo(leap, march)
}

/** شماره روز مطلق (Julian Day Number) از تاریخ میلادی */
private fun g2d(gy: Int, gm: Int, gd: Int): Int {
    var d = div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
        div(153 * mod(gm + 9, 12) + 2, 5) +
        gd - 34840408
    d = d - div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) + 752
    return d
}

/** تاریخ میلادی از شماره روز مطلق */
private fun d2g(jdn: Int): Triple<Int, Int, Int> {
    var j = 4 * jdn + 139361631
    j += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
    val i = div(mod(j, 1461), 4) * 5 + 308
    val gd = div(mod(i, 153), 5) + 1
    val gm = mod(div(i, 153), 12) + 1
    val gy = div(j, 1461) - 100100 + div(8 - gm, 6)
    return Triple(gy, gm, gd)
}
