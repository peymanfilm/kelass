package com.example.classschedule.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * تنظیمات برنامه: نمای صفحه (تک‌روز یا کامل هفته)، رنگ تم انتخابی، رنگ دلخواه
 * و پنل انتخابی (نقش کاربر: دانش‌آموز / معلم / مدیر / مالک)
 */
data class AppSettings(
    val themeIndex: Int = 0,      // شماره تم رنگی (۰ تا ۲۳) — پیش‌فرض: بنفش
    val viewMode: Int = SettingsRepository.VIEW_MODE_WEEK, // پیش‌فرض: نمای کامل هفته
    val customColor: Int = -1,    // رنگ دلخواه (ARGB) — منفی یعنی بدون رنگ دلخواه
    val role: Int = AppRoles.ROLE_NONE // پنل انتخابی؛ تا وقتی انتخاب نشده صفحه انتخاب نقش باز است
)

/**
 * مخزن تنظیمات برنامه با SharedPreferences
 * انتخاب‌های کاربر (رنگ، رنگ دلخواه و نوع نمایش) حتی بعد از بستن برنامه ذخیره می‌ماند.
 */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(
        AppSettings(
            themeIndex = prefs.getInt(KEY_THEME, 0),
            viewMode = prefs.getInt(KEY_VIEW_MODE, VIEW_MODE_WEEK),
            customColor = prefs.getInt(KEY_CUSTOM_COLOR, -1),
            role = prefs.getInt(KEY_ROLE, AppRoles.ROLE_NONE)
        )
    )
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    /** ذخیره تم آماده انتخابی کاربر (رنگ دلخواه را پاک می‌کند) */
    fun setThemeIndex(index: Int) {
        prefs.edit()
            .putInt(KEY_THEME, index)
            .putInt(KEY_CUSTOM_COLOR, -1)
            .apply()
        _settings.update { it.copy(themeIndex = index, customColor = -1) }
    }

    /** ذخیره رنگ دلخواه انتخاب‌شده توسط کاربر */
    fun setCustomColor(argb: Int) {
        prefs.edit()
            .putInt(KEY_THEME, CUSTOM_THEME_INDEX)
            .putInt(KEY_CUSTOM_COLOR, argb)
            .apply()
        _settings.update { it.copy(themeIndex = CUSTOM_THEME_INDEX, customColor = argb) }
    }

    /** ذخیره نوع نمایش (تک‌روز یا کامل هفته) */
    fun setViewMode(mode: Int) {
        prefs.edit().putInt(KEY_VIEW_MODE, mode).apply()
        _settings.update { it.copy(viewMode = mode) }
    }

    /** ذخیره پنل انتخابی (نقش کاربر) — با باز شدن دوباره برنامه همان پنل باز می‌شود */
    fun setRole(role: Int) {
        prefs.edit().putInt(KEY_ROLE, role).apply()
        _settings.update { it.copy(role = role) }
    }

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_THEME = "theme_index"
        private const val KEY_VIEW_MODE = "view_mode"
        private const val KEY_CUSTOM_COLOR = "custom_color"
        private const val KEY_ROLE = "selected_role"

        const val VIEW_MODE_DAY = 0   // نمایش تک‌روز با تب‌ها
        const val VIEW_MODE_WEEK = 1  // نمایش کامل همه روزهای هفته
        const val CUSTOM_THEME_INDEX = 24 // شماره ویژه «رنگ دلخواه» (۲۴ تم آماده: ۰ تا ۲۳)
    }
}
