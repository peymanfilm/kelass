package com.example.classschedule.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * تم اصلی برنامه بر اساس Material Design 3
 * رنگ برنامه از میان ۲۴ تم آماده یا «رنگ دلخواه» کاربر تعیین می‌شود
 * و به صورت خودکار روی تمام بخش‌های برنامه اعمال می‌شود.
 * اگر گوشی در حالت تیره باشد، پالت تیره همان تم استفاده می‌شود.
 */
@Composable
fun ClassScheduleTheme(
    themeIndex: Int = 0,
    customColor: Int = -1,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appTheme = ColorThemes.themeFor(themeIndex, customColor.takeIf { it != -1 })
    val colorScheme = if (darkTheme) appTheme.dark else appTheme.light

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
