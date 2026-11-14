package com.example.classschedule.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.example.classschedule.data.SettingsRepository

/**
 * مجموعه تم‌های رنگی برنامه
 * ۲۴ تم از پیش طراحی‌شده + امکان «رنگ دلخواه» با انتخاب دستی از طیف رنگی
 *
 * رنگ انتخابی روی نوار بالا، دکمه افزودن، تب‌ها و همه رنگ‌های تأکیدی برنامه اعمال می‌شود.
 * رنگ‌های فرعی (پس‌زمینه کارت‌مانند و متن روی آن‌ها) به صورت خودکار از رنگ اصلی ساخته می‌شوند.
 */
data class AppColorTheme(
    val name: String,       // نام فارسی تم برای نمایش در دیالوگ انتخاب رنگ
    val accent: Color,      // رنگ اصلی (نمونه دایره‌ای در دیالوگ)
    val light: ColorScheme, // پالت تم روشن
    val dark: ColorScheme   // پالت تم تیره
)

// رنگ‌های ثابت مکمل (مستقل از تم انتخابی)
private val ComplementarySecondary = Color(0xFF8D6E00)
private val ComplementarySecondaryContainer = Color(0xFFFFE082)
private val ComplementaryTertiary = Color(0xFF8E2F4C)
private val ComplementaryTertiaryContainer = Color(0xFFFFD9E1)

private val BackgroundDark = Color(0xFF191C1B)
private val SurfaceDark = Color(0xFF191C1B)
private val TextOnDark = Color(0xFFE1E3E1)

/** رنگ متن مناسب روی رنگ اصلی: روی رنگ‌های روشن مشکی و روی تیره‌ها سفید */
private fun readableOn(primary: Color): Color =
    if (primary.luminance() > 0.5f) Color.Black else Color.White

/** ساخت پالت تم روشن بر اساس رنگ اصلی انتخابی */
private fun buildLight(
    primary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color
): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = readableOn(primary),
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = ComplementarySecondary,
    secondaryContainer = ComplementarySecondaryContainer,
    tertiary = ComplementaryTertiary,
    tertiaryContainer = ComplementaryTertiaryContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

/** ساخت پالت تم تیره بر اساس رنگ اصلی انتخابی */
private fun buildDark(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color
): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = Color(0xFFFFD54F),
    secondaryContainer = Color(0xFF5C4900),
    tertiary = Color(0xFFFFB1C8),
    tertiaryContainer = Color(0xFF6E1B37),
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextOnDark,
    onSurface = TextOnDark
)

/**
 * ساخت کامل یک تم از رنگ اصلی:
 * رنگ‌های فرعی به صورت خودکار محاسبه می‌شوند.
 */
private fun buildTheme(name: String, primary: Color): AppColorTheme {
    // تم روشن
    val lightContainer = lerp(primary, Color.White, 0.75f)
    val lightOnContainer = lerp(primary, Color.Black, 0.82f)
    // تم تیره
    val darkPrimary = lerp(primary, Color.White, 0.50f)
    val darkOnPrimary = lerp(primary, Color.Black, 0.80f)
    val darkContainer = lerp(primary, Color.Black, 0.40f)
    val darkOnContainer = lerp(primary, Color.White, 0.75f)

    return AppColorTheme(
        name = name,
        accent = primary,
        light = buildLight(primary, lightContainer, lightOnContainer),
        dark = buildDark(darkPrimary, darkOnPrimary, darkContainer, darkOnContainer)
    )
}

object ColorThemes {

    /** شماره ویژه «رنگ دلخواه» — رنگ دستی کاربر */
    const val CUSTOM_INDEX = SettingsRepository.CUSTOM_THEME_INDEX

    val count: Int
        get() = list.size

    /** ۲۴ تم آماده برنامه */
    val list: List<AppColorTheme> = listOf(
        buildTheme("بنفش", Color(0xFF6A1B9A)),
        buildTheme("یاسی", Color(0xFF5E35B1)),
        buildTheme("بادمجانی", Color(0xFF4A148C)),
        buildTheme("ارغوانی", Color(0xFF8E24AA)),
        buildTheme("نیلی", Color(0xFF303F9F)),
        buildTheme("سرمه‌ای", Color(0xFF1A237E)),
        buildTheme("آبی", Color(0xFF1565C0)),
        buildTheme("آسمانی", Color(0xFF0277BD)),
        buildTheme("فیروزه‌ای", Color(0xFF00838F)),
        buildTheme("سبز مریم‌گلی", Color(0xFF00695C)),
        buildTheme("زمردی", Color(0xFF00897B)),
        buildTheme("یشمی", Color(0xFF2E7D32)),
        buildTheme("سبز جنگلی", Color(0xFF1B5E20)),
        buildTheme("سبز روشن", Color(0xFF558B2F)),
        buildTheme("زیتونی", Color(0xFF827717)),
        buildTheme("کهربایی", Color(0xFF9A7B00)),
        buildTheme("نارنجی", Color(0xFFEF6C00)),
        buildTheme("نارنجی سوخته", Color(0xFFD84315)),
        buildTheme("قهوه‌ای", Color(0xFF5D4037)),
        buildTheme("قرمز", Color(0xFFC62828)),
        buildTheme("زرشکی", Color(0xFFB71C1C)),
        buildTheme("سرخابی", Color(0xFFAD1457)),
        buildTheme("صورتی", Color(0xFFC2185B)),
        buildTheme("خاکستری", Color(0xFF455A64))
    )

    fun forIndex(index: Int): AppColorTheme =
        list[index.coerceIn(0, list.lastIndex)]

    /**
     * انتخاب تم نهایی بر اساس تنظیمات کاربر
     * اگر «رنگ دلخواه» فعال باشد، تم از رنگ دستی کاربر ساخته می‌شود.
     */
    fun themeFor(index: Int, customColorArgb: Int?): AppColorTheme =
        if (index == CUSTOM_INDEX && customColorArgb != null) {
            buildTheme("رنگ دلخواه", Color(customColorArgb))
        } else {
            forIndex(index)
        }
}
