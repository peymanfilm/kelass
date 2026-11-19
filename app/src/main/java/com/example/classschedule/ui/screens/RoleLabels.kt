package com.example.classschedule.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.classschedule.data.AppRoles

/**
 * برچسب‌ها و ظاهر مشترک هر نقش — در صفحه ورود، مدیریت حساب‌ها و نوار بالای پنل‌ها استفاده می‌شود.
 */

/** برچسب کوتاه فارسی هر نقش */
fun roleName(role: Int): String = when (role) {
    AppRoles.ROLE_TEACHER -> "معلم"
    AppRoles.ROLE_MANAGER -> "مدیر"
    AppRoles.ROLE_OWNER -> "مالک برند"
    else -> "دانش‌آموز"
}

/** برچسب فارسی پنل هر نقش برای نمایش در نوار بالای پنل‌ها */
fun roleTitle(role: Int): String = when (role) {
    AppRoles.ROLE_TEACHER -> "پنل معلم"
    AppRoles.ROLE_MANAGER -> "پنل مدیر"
    AppRoles.ROLE_OWNER -> "پنل مالک برند"
    else -> "پنل دانش‌آموز"
}

/** آیکون اختصاصی هر نقش */
fun roleIcon(role: Int): ImageVector = when (role) {
    AppRoles.ROLE_TEACHER -> Icons.Default.HistoryEdu
    AppRoles.ROLE_MANAGER -> Icons.Default.ManageAccounts
    AppRoles.ROLE_OWNER -> Icons.Default.WorkspacePremium
    else -> Icons.Default.School
}

/** رنگ اصلی هر نقش — خارج از تم تا هر نقش هویت بصری خودش را داشته باشد (بدون آبی و نیلی) */
fun roleAccent(role: Int): Color = when (role) {
    AppRoles.ROLE_TEACHER -> Color(0xFFE65100)   // نارنجی
    AppRoles.ROLE_MANAGER -> Color(0xFF00695C)   // سبز تیره (تیل)
    AppRoles.ROLE_OWNER -> Color(0xFFB45309)     // کهربایی تیره — برند MTM Kode
    else -> Color(0xFF2E7D32)                    // سبز
}

/** رنگ پس‌زمینه‌ی ملایم هر نقش */
fun roleContainer(role: Int): Color = when (role) {
    AppRoles.ROLE_TEACHER -> Color(0xFFFFF3E0)
    AppRoles.ROLE_MANAGER -> Color(0xFFE0F2F1)
    AppRoles.ROLE_OWNER -> Color(0xFFFFF7E6)
    else -> Color(0xFFE8F5E9)
}
