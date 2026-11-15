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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
}

/**
 * کارخانه ساخت ViewModel برای تزریق Repository ها
 */
class ScheduleViewModelFactory(
    private val classRepository: ClassRepository,
    private val homeworkRepository: HomeworkRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            return ScheduleViewModel(classRepository, homeworkRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
