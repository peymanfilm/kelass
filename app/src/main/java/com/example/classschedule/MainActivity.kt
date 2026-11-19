package com.example.classschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.classschedule.data.AppDatabase
import com.example.classschedule.data.AppRoles
import com.example.classschedule.data.AuthRepository
import com.example.classschedule.data.ClassRepository
import com.example.classschedule.data.HomeworkRepository
import com.example.classschedule.data.SettingsRepository
import com.example.classschedule.data.SubjectGradeRepository
import com.example.classschedule.ui.screens.LoginScreen
import com.example.classschedule.ui.screens.MainScreen
import com.example.classschedule.ui.theme.ClassScheduleTheme
import com.example.classschedule.viewmodel.ScheduleViewModel
import com.example.classschedule.viewmodel.ScheduleViewModelFactory

/**
 * اکتیویتی اصلی برنامه
 * - اتصال ViewModel به لایه داده (Room + Repository)
 * - اعمال رنگ انتخابی کاربر روی کل برنامه
 * - فعال‌سازی چیدمان راست‌به‌چپ (RTL) برای پشتیبانی کامل از زبان فارسی
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsRepository = SettingsRepository(applicationContext)
        val authRepository = AuthRepository(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)

        val viewModel: ScheduleViewModel by viewModels {
            ScheduleViewModelFactory(
                ClassRepository(database.classDao()),
                HomeworkRepository(database.homeworkDao()),
                SubjectGradeRepository(database.subjectGradeDao()),
                settingsRepository
            )
        }

        setContent {
            // رنگ انتخابی کاربر به صورت زنده روی تم کل برنامه اعمال می‌شود
            val settings by settingsRepository.settings.collectAsState()
            ClassScheduleTheme(
                themeIndex = settings.themeIndex,
                customColor = settings.customColor
            ) {
                RtlAppContent(viewModel = viewModel, authRepository = authRepository)
            }
        }
    }
}

/**
 * کل برنامه در چیدمان راست‌به‌چپ نمایش داده می‌شود
 * دروازه‌ی احراز هویت: تا کاربر با نام کاربری و رمز عبور وارد نشده باشد، صفحه ورود باز می‌شود.
 * هر حساب نقش خودش را دارد (student1 دانش‌آموز / teacher1 معلم / manager مدیر / owner مالک برند)
 * و بعد از ورود، مستقیم پنل همان نقش باز می‌شود؛ خروج با دکمه‌ی «خروج از حساب» در نوار بالای پنل.
 */
@Composable
private fun RtlAppContent(viewModel: ScheduleViewModel, authRepository: AuthRepository) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // وضعیت ورود به صورت زنده خوانده می‌شود؛ با خروج، صفحه ورود دوباره باز می‌شود
            val isLoggedIn by authRepository.isLoggedIn.collectAsState()
            val settings by viewModel.settings.collectAsState()

            if (isLoggedIn && settings.role != AppRoles.ROLE_NONE) {
                // پنل نقشِ حساب واردشده — نقش با ورود تعیین شده است
                MainScreen(viewModel = viewModel, role = settings.role, authRepository = authRepository)
            } else {
                // دروازه ورود — احراز هویت آفلاین؛ نقش از حساب انتخابی تعیین می‌شود
                if (isLoggedIn) {
                    // حالت خاص: وارد شده ولی نقشی ثبت نشده — دوباره ورود خواسته می‌شود
                    authRepository.logout()
                }
                LoginScreen(
                    authRepository = authRepository,
                    onLogin = { username, password ->
                        val role = authRepository.login(username, password)
                        if (role != AppRoles.ROLE_NONE) {
                            // پنلِ همان نقش باز می‌شود
                            viewModel.setRole(role)
                        }
                        role
                    }
                )
            }
        }
    }
}
