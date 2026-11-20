package com.example.classschedule.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * مدل «حساب کاربر» — هم‌تراز با پنل‌های وب‌سایت:
 *
 * در نسخه‌های قبل فقط چهار حساب ثابت (یک حساب برای هر نقش) وجود داشت؛
 * حالا مثل وب‌سایت، مدیر می‌تواند هر تعداد دانش‌آموز و معلم بسازد و
 * مالک برند MTM Kode علاوه بر آن‌ها، هر تعداد مدیر هم می‌تواند بسازد.
 *
 * - name: نام نمایشی (مثلاً «علی محمدی») که در صفحه ورود، نوار بالای پنل و
 *   کارت تکالیف (ثبت‌کننده) دیده می‌شود
 * - username: نام کاربری یکتا برای ورود (ایمیل‌مانند ولی آزاد)
 * - passwordHash: هش SHA-256 رمز عبور — رمز خام هرگز ذخیره نمی‌شود
 * - role: نقش کاربر (دانش‌آموز / معلم / مدیر / مالک برند)
 */
@Entity(
    tableName = "app_user",
    indices = [Index(value = ["username"], unique = true)]
)
data class AppUser(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val username: String,
    val passwordHash: String,
    val role: Int
)
