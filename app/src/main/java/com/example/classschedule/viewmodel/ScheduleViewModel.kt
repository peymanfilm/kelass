package com.example.classschedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.classschedule.data.AppSettings
import com.example.classschedule.data.ClassRepository
import com.example.classschedule.data.ClassSchedule
import com.example.classschedule.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel اصلی برنامه (لایه MVVM)
 * وظیفه: نگهداری وضعیت کلاس‌ها و تنظیمات (رنگ و نوع نمایش)
 * و ارتباط بین UI و Repository
 */
class ScheduleViewModel(
    private val repository: ClassRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // لیست همه کلاس‌ها به صورت StateFlow؛ هر تغییر دیتابیس به صورت خودکار در UI اعمال می‌شود
    val allClasses: StateFlow<List<ClassSchedule>> = repository.allClasses.stateIn(
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
            repository.insert(classSchedule)
        }
    }

    // ویرایش کلاس موجود
    fun updateClass(classSchedule: ClassSchedule) {
        viewModelScope.launch {
            repository.update(classSchedule)
        }
    }

    // حذف کلاس
    fun deleteClass(classSchedule: ClassSchedule) {
        viewModelScope.launch {
            repository.delete(classSchedule)
        }
    }
}

/**
 * کارخانه ساخت ViewModel برای تزریق Repository ها
 */
class ScheduleViewModelFactory(
    private val repository: ClassRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            return ScheduleViewModel(repository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
