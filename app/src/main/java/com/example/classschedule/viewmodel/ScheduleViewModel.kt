package com.example.classschedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.classschedule.data.AppSettings
import com.example.classschedule.data.ClassRepository
import com.example.classschedule.data.ClassSchedule
import com.example.classschedule.data.Homework
import com.example.classschedule.data.HomeworkRepository
import com.example.classschedule.data.SettingsRepository
import com.example.classschedule.data.SubjectGrade
import com.example.classschedule.data.SubjectGradeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel اصلی برنامه (لایه MVVM)
 * وظیفه: نگهداری وضعیت کلاس‌ها، تکالیف و تنظیمات (رنگ و نوع نمایش)
 * و ارتباط بین UI و Repository ها
 */
class ScheduleViewModel(
    private val classRepository: ClassRepository,
    private val homeworkRepository: HomeworkRepository,
    private val subjectGradeRepository: SubjectGradeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // لیست همه کلاس‌ها به صورت StateFlow؛ هر تغییر دیتابیس به صورت خودکار در UI اعمال می‌شود
    val allClasses: StateFlow<List<ClassSchedule>> = classRepository.allClasses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // لیست همه‌ی تکالیف به صورت StateFlow
    val allHomework: StateFlow<List<Homework>> = homeworkRepository.allHomework.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // کارنامه: نمره مستمر و قانون عکس اجباری هر درس
    val allSubjectGrades: StateFlow<List<SubjectGrade>> = subjectGradeRepository.allGrades.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    init {
        // همگام‌سازی خودکار درس‌های کارنامه:
        // هر درسِ برنامه‌ی هفتگی و هر درسِ تکالیف، یک ردیف در کارنامه دارد؛
        // تکالیف بدون درس هم زیر گروه «بدون درس» در کارنامه حساب می‌شوند.
        viewModelScope.launch {
            combine(classRepository.allClasses, homeworkRepository.allHomework) { classes, homework ->
                val names = classes.map { it.name.trim() }
                    .filter { it.isNotBlank() } +
                    homework.map { it.subject.trim() }
                        .filter { it.isNotBlank() }
                val hasNoSubjectHomework = homework.any { it.subject.trim().isBlank() }
                Pair(names.distinct(), hasNoSubjectHomework)
            }.collect { (names, hasNoSubjectHomework) ->
                names.forEach { subjectGradeRepository.ensureExists(it) }
                if (hasNoSubjectHomework) {
                    subjectGradeRepository.ensureExists(SubjectGrade.NO_SUBJECT)
                }
            }
        }
    }

    // تنظیمات برنامه: رنگ تم و نوع نمایش (تک‌روز / کامل هفته)
    val settings: StateFlow<AppSettings> = settingsRepository.settings

    // تغییر رنگ آماده توسط کاربر
    fun setThemeIndex(index: Int) {
        settingsRepository.setThemeIndex(index)
    }

    // انتخاب رنگ دلخواه توسط کاربر (انتخاب دستی از طیف رنگی)
    fun setCustomColor(argb: Int) {
        settingsRepository.setCustomColor(argb)
    }

    // تغییر نوع نمایش بین تک‌روز و کامل هفته
    fun setViewMode(mode: Int) {
        settingsRepository.setViewMode(mode)
    }

    // تغییر پنل (نقش کاربر: دانش‌آموز / معلم / مدیر / مالک برند MTM Kode)
    // نقش روی گوشی ذخیره می‌شود و UI به صورت زنده عوض می‌شود
    fun setRole(role: Int) {
        settingsRepository.setRole(role)
    }

    // ذخیره نام مجموعه (مدرسه) — فقط از تب تنظیمات مالک برند MTM Kode
    fun setSchoolName(name: String) {
        settingsRepository.setSchoolName(name)
    }

    // افزودن کلاس جدید
    fun addClass(classSchedule: ClassSchedule) {
        viewModelScope.launch {
            classRepository.insert(classSchedule)
        }
    }

    // ویرایش کلاس موجود
    fun updateClass(classSchedule: ClassSchedule) {
        viewModelScope.launch {
            classRepository.update(classSchedule)
        }
    }

    // حذف کلاس
    fun deleteClass(classSchedule: ClassSchedule) {
        viewModelScope.launch {
            classRepository.delete(classSchedule)
        }
    }

    // افزودن تکلیف جدید
    fun addHomework(homework: Homework) {
        viewModelScope.launch {
            homeworkRepository.insert(homework)
        }
    }

    // ویرایش تکلیف موجود
    fun updateHomework(homework: Homework) {
        viewModelScope.launch {
            homeworkRepository.update(homework)
        }
    }

    // حذف تکلیف
    fun deleteHomework(homework: Homework) {
        viewModelScope.launch {
            homeworkRepository.delete(homework)
        }
    }

    // تغییر وضعیت انجام‌شدن تکلیف (انجام شد / بازگشت به حالت باز)
    fun toggleHomeworkDone(homework: Homework) {
        viewModelScope.launch {
            homeworkRepository.update(homework.copy(isDone = !homework.isDone))
        }
    }

    // افزودن عکس‌های جدید به یک تکلیف (نام فایل‌های تازه ذخیره‌شده در حافظه‌ی برنامه)
    fun addPhotosToHomework(homework: Homework, newNames: List<String>) {
        if (newNames.isEmpty()) return
        viewModelScope.launch {
            val updatedNames = (homework.imageList + newNames).joinToString(";")
            homeworkRepository.update(homework.copy(imageNames = updatedNames))
        }
    }

    // حذف یک عکس از تکلیف (فایل آن باید جداگانه از حافظه پاک شود)
    fun removePhotoFromHomework(homework: Homework, name: String) {
        viewModelScope.launch {
            val updatedNames = (homework.imageList - name).joinToString(";")
            homeworkRepository.update(homework.copy(imageNames = updatedNames))
        }
    }

    // ثبت یا پاک کردن نمره مستمر یک درس در کارنامه (grade = null یعنی پاک کردن)
    fun setContinuousGrade(name: String, grade: Float?) {
        viewModelScope.launch {
            subjectGradeRepository.setContinuousGrade(name, grade)
        }
    }

    // ثبت یا پاک کردن نمره امتحان یک درس در کارنامه (grade = null یعنی پاک کردن)
    fun setExamGrade(name: String, grade: Float?) {
        viewModelScope.launch {
            subjectGradeRepository.setExamGrade(name, grade)
        }
    }

    // افزودن یا کم کردن منفی دستی معلم برای یک درس (delta مثبت = افزودن، منفی = کم کردن)
    fun adjustManualNegative(name: String, delta: Int) {
        viewModelScope.launch {
            subjectGradeRepository.adjustManualNegatives(name, delta)
        }
    }

    // افزودن یا کم کردن مثبت دستی معلم برای یک درس (delta مثبت = افزودن، منفی = کم کردن)
    fun adjustManualPositive(name: String, delta: Int) {
        viewModelScope.launch {
            subjectGradeRepository.adjustManualPositives(name, delta)
        }
    }

    // تعیین اینکه عکس تکلیف برای یک درس اجباری است یا نه
    fun setPhotoRequired(name: String, required: Boolean) {
        viewModelScope.launch {
            subjectGradeRepository.setPhotoRequired(name, required)
        }
    }

    // افزودن دستی یک درس به کارنامه (اگر از قبل باشد، تغییری نمی‌کند)
    fun addSubject(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            subjectGradeRepository.ensureExists(trimmed)
        }
    }

    // حذف یک درس از کارنامه (تکالیف آن درس حذف نمی‌شوند)
    fun deleteSubject(name: String) {
        viewModelScope.launch {
            subjectGradeRepository.delete(name)
        }
    }
}

/**
 * کارخانه ساخت ViewModel برای تزریق Repository ها
 */
class ScheduleViewModelFactory(
    private val classRepository: ClassRepository,
    private val homeworkRepository: HomeworkRepository,
    private val subjectGradeRepository: SubjectGradeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            return ScheduleViewModel(
                classRepository,
                homeworkRepository,
                subjectGradeRepository,
                settingsRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
