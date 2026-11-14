package com.example.classschedule.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * پالت رنگ کارت‌های کلاس
 * هر کلاس بسته به colorIndex با یک رنگ متمایز نمایش داده می‌شود.
 * نام هر رنگ در فرم افزودن کلاس به کاربر نشان داده می‌شود.
 */
data class ClassColorScheme(
    val name: String,       // نام فارسی رنگ (نمایش در فرم کلاس)
    val container: Color,   // رنگ پس‌زمینه کارت
    val accent: Color,      // رنگ تأکیدی (آیکون‌ها و دایره حرف اول)
    val onContainer: Color  // رنگ متن روی کارت
)

object ClassColors {

    val count: Int
        get() = palette.size

    private val palette = listOf(
        ClassColorScheme(
            name = "نارنجی",
            container = Color(0xFFFFE0B2),
            accent = Color(0xFFEF6C00),
            onContainer = Color(0xFF4E2600)
        ),
        ClassColorScheme(
            name = "سبز",
            container = Color(0xFFC8E6C9),
            accent = Color(0xFF2E7D32),
            onContainer = Color(0xFF0B290D)
        ),
        ClassColorScheme(
            name = "قرمز",
            container = Color(0xFFFFCDD2),
            accent = Color(0xFFC62828),
            onContainer = Color(0xFF410002)
        ),
        ClassColorScheme(
            name = "فیروزه‌ای",
            container = Color(0xFFB2DFDB),
            accent = Color(0xFF00695C),
            onContainer = Color(0xFF00201C)
        ),
        ClassColorScheme(
            name = "بنفش",
            container = Color(0xFFE1BEE7),
            accent = Color(0xFF6A1B9A),
            onContainer = Color(0xFF280643)
        ),
        ClassColorScheme(
            name = "زرد",
            container = Color(0xFFFFF59D),
            accent = Color(0xFFF9A825),
            onContainer = Color(0xFF403000)
        ),
        ClassColorScheme(
            name = "آبی",
            container = Color(0xFFB3E5FC),
            accent = Color(0xFF0277BD),
            onContainer = Color(0xFF001F28)
        ),
        ClassColorScheme(
            name = "صورتی",
            container = Color(0xFFF8BBD0),
            accent = Color(0xFFAD1457),
            onContainer = Color(0xFF3E001E)
        )
    )

    fun forIndex(index: Int): ClassColorScheme =
        palette[index.coerceIn(0, palette.lastIndex)]
}
