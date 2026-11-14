package com.example.classschedule.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.classschedule.R

/**
 * فونت وزیرمتن (Vazirmatn)
 * فایل‌های TTF در مسیر app/src/main/res/font قرار دارند:
 * vazirmatn_regular.ttf ، vazirmatn_medium.ttf ، vazirmatn_semibold.ttf ، vazirmatn_bold.ttf
 */
val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

// استایل پایه با فونت وزیرمتن برای همه متن‌های برنامه
private val BaseStyle = TextStyle(fontFamily = VazirmatnFontFamily)

// تایپوگرافی برنامه (اندازه‌ها برای متن فارسی بهینه شده‌اند)
val Typography = Typography(
    headlineMedium = BaseStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp
    ),
    titleLarge = BaseStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    ),
    titleMedium = BaseStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp
    ),
    bodyLarge = BaseStyle.copy(
        fontSize = 15.sp
    ),
    bodyMedium = BaseStyle.copy(
        fontSize = 13.sp
    ),
    labelLarge = BaseStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelSmall = BaseStyle.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
)
