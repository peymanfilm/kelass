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
import com.example.classschedule.ui.screens.RoleSelectionScreen
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
 * بعد از ورود: اگر هنوز پنلی انتخاب نشده باشد، صفحه انتخاب پنل (دانش‌آموز/معلم/مدیر/مالک) باز می‌شود
 * وگرنه پنل ذخیره‌شده‌ی کاربر باز می‌شود.
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
            if (!isLoggedIn) {
                // دروازه ورود — احراز هویت آفلاین
                LoginScreen(
                    onLogin = { username, password -> authRepository.login(username, password) }
                )
                return@Surface
            }

            // نقش انتخابی به صورت زنده خوانده می‌شود تا با تعویض پنل، صفحه عوض شود
            val settings by viewModel.settings.collectAsState()
            if (settings.role == AppRoles.ROLE_NONE) {
                // بار اول بعد از ورود: انتخاب پنل
                RoleSelectionScreen(
                    onSelectRole = { viewModel.setRole(it) }
                )
            } else {
                // پنل انتخاب‌شده
                MainScreen(viewModel = viewModel, role = settings.role, authRepository = authRepository)
            }
        }
    }
}
