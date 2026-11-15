package com.example.classschedule.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * مدل «کارنامه»: نمره‌ها، منفی/مثبت‌ها و قانون عکس برای هر درس
 *
 * - continuousGrade: نمره مستمر (کلاسی) که معلم وارد می‌کند؛ null یعنی هنوز وارد نشده
 * - examGrade: نمره امتحان همان درس؛ null یعنی هنوز وارد نشده
 * - manualNegatives: منفی‌هایی که معلم به صورت دستی ثبت می‌کند (جدا از منفی‌های خودکار تکالیف)
 * - manualPositives: مثبت‌هایی که معلم به صورت دستی ثبت می‌کند
 * - photoRequired: اگر روشن باشد، تکلیفِ مهلت‌گذشته‌ی بدون عکس برای این درس «منفی» می‌گیرد
 *   و اگر خاموش باشد، بی‌عکسی تکالیف این درس روی نمره اثری ندارد.
 *
 * قانون نمره:
 * - هر منفی (خودکار یا دستی) ۰٫۲۵ از نمره مستمر همان درس کم می‌کند.
 * - هر مثبت اگر نمره کمتر از ۲۰ باشد ۰٫۲۵ به آن اضافه می‌کند؛
 *   اگر نمره ۲۰ شده باشد، مثبت‌های اضافه کنار نمره شمرده می‌شوند.
 * - معدل درس = میانگین نمره مستمر نهایی و نمره امتحان (اگر امتحان وارد شده باشد).
 * - همه‌ی نمره‌ها بین ۰ تا ۲۰ نگه داشته می‌شوند.
 */
@Entity(tableName = "subject_grade")
data class SubjectGrade(
    @PrimaryKey val name: String,           // نام درس (شناسه یکتا در کارنامه)
    val continuousGrade: Float? = null,     // نمره مستمر — null یعنی هنوز وارد نشده
    val examGrade: Float? = null,           // نمره امتحان — null یعنی هنوز وارد نشده
    @ColumnInfo(defaultValue = "0")
    val manualNegatives: Int = 0,           // منفی‌های دستی معلم
    @ColumnInfo(defaultValue = "0")
    val manualPositives: Int = 0,           // مثبت‌های دستی معلم
    @ColumnInfo(defaultValue = "1")
    val photoRequired: Boolean = true       // آیا عکس تکلیف اجباری است؟ (پیش‌فرض: بله)
) {
    companion object {
        /** نام گروه ویژه برای تکالیفی که هیچ درسی برایشان انتخاب نشده است */
        const val NO_SUBJECT = "بدون درس"

        /** مقدار کسر/افزودن نمره به ازای هر منفی یا مثبت: ۰٫۲۵ */
        const val PENALTY_PER_NEGATIVE = 0.25f

        /** سقف نمره‌گذاری مدارس ایران */
        const val MAX_GRADE = 20f
    }
}
