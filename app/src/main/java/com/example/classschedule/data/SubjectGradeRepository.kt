package com.example.classschedule.data

import kotlinx.coroutines.flow.Flow

/**
 * لایه Repository کارنامه: واسط بین ViewModel و منبع داده (Room)
 */
class SubjectGradeRepository(private val subjectGradeDao: SubjectGradeDao) {

    val allGrades: Flow<List<SubjectGrade>> = subjectGradeDao.getAll()

    /**
     * اگر درس هنوز در کارنامه نباشد، با تنظیمات پیش‌فرض اضافه می‌شود
     * (نمره وارد نشده + عکس اجباری)؛ تنظیمات قبلی درس موجود حفظ می‌شود.
     */
    suspend fun ensureExists(name: String) {
        subjectGradeDao.insertIfMissing(SubjectGrade(name = name))
    }

    // ثبت یا پاک کردن نمره مستمر
    suspend fun setContinuousGrade(name: String, grade: Float?) =
        subjectGradeDao.setContinuousGrade(name, grade)

    // تغییر قانون عکس اجباری
    suspend fun setPhotoRequired(name: String, required: Boolean) =
        subjectGradeDao.setPhotoRequired(name, required)

    // حذف درس از کارنامه
    suspend fun delete(name: String) = subjectGradeDao.delete(name)
}
