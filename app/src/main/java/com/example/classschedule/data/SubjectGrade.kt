package com.example.classschedule.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * مدل «کارنامه»: تنظیمات نمره و قانون عکس برای هر درس
 *
 * - continuousGrade: نمره مستمر (کلاسی) که معلم وارد می‌کند؛ null یعنی هنوز وارد نشده
 * - photoRequired: اگر روشن باشد، تکلیفِ مهلت‌گذشته‌ی بدون عکس برای این درس «منفی» می‌گیرد
 *   و اگر خاموش باشد، بی‌عکسی تکالیف این درس روی نمره اثری ندارد.
 *
 * قانون نمره: هر منفی، ۰٫۲۵ از نمره مستمر همان درس کم می‌کند
 * و نمره نهایی بین ۰ تا ۲۰ نگه داشته می‌شود.
 */
@Entity(tableName = "subject_grade")
data class SubjectGrade(
    @PrimaryKey val name: String,           // نام درس (شناسه یکتا در کارنامه)
    val continuousGrade: Float? = null,     // نمره مستمر — null یعنی هنوز وارد نشده
    @ColumnInfo(defaultValue = "1")
    val photoRequired: Boolean = true       // آیا عکس تکلیف اجباری است؟ (پیش‌فرض: بله)
) {
    companion object {
        /** نام گروه ویژه برای تکالیفی که هیچ درسی برایشان انتخاب نشده است */
        const val NO_SUBJECT = "بدون درس"

        /** مقدار کسر نمره به ازای هر منفی: ۰٫۲۵ */
        const val PENALTY_PER_NEGATIVE = 0.25f

        /** سقف نمره‌گذاری مدارس ایران */
        const val MAX_GRADE = 20f
    }
}
