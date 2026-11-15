package com.example.classschedule.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.ClassSchedule
import com.example.classschedule.data.Homework
import com.example.classschedule.data.SettingsRepository.Companion.VIEW_MODE_DAY
import com.example.classschedule.data.SettingsRepository.Companion.VIEW_MODE_WEEK
import com.example.classschedule.ui.components.ClassFormDialog
import com.example.classschedule.ui.components.ColorSettingsDialog
import com.example.classschedule.ui.components.HomeworkFormDialog
import com.example.classschedule.ui.theme.ClassColors
import com.example.classschedule.util.WeekDays
import com.example.classschedule.viewmodel.ScheduleViewModel

/**
 * تعریف تب‌های اصلی برنامه — در آینده می‌توان تب‌های جدیدی به این فهرست افزود
 */
private data class MainTab(val title: String, val icon: ImageVector)

private val MAIN_TABS = listOf(
    MainTab(title = "برنامه هفتگی", icon = Icons.Default.MenuBook),
    MainTab(title = "یادآوری تکالیف", icon = Icons.Default.Assignment)
)

/**
 * صفحه اصلی برنامه با تب‌های بالای صفحه:
 * ۱) برنامه هفتگی: نمای کامل هفته یا تک‌روز (بر اساس انتخاب کاربر)
 * ۲) یادآوری تکالیف: فهرست تکالیف با تاریخ مهلت شمسی و وضعیت انجام
 * نوار بالا، دکمه شناور افزودن و همه‌ی دیالوگ‌ها اینجا مدیریت می‌شوند.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ScheduleViewModel) {

    val allClasses by viewModel.allClasses.collectAsState()
    val allHomework by viewModel.allHomework.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isWeekView = settings.viewMode == VIEW_MODE_WEEK

    // تب اصلی فعال: ۰ = برنامه هفتگی، ۱ = یادآوری تکالیف
    var selectedMainTab by remember { mutableIntStateOf(0) }

    // صفحه‌گرد (Pager) روزهای هفته که با تب‌های روز هماهنگ است و روی «امروز» باز می‌شود
    val dayPagerState = rememberPagerState(initialPage = WeekDays.todayIndex()) {
        WeekDays.names.size
    }

    // وضعیت دیالوگ‌های کلاس
    var showFormDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<ClassSchedule?>(null) }
    var deletingClass by remember { mutableStateOf<ClassSchedule?>(null) }
    var formDay by remember { mutableIntStateOf(WeekDays.todayIndex()) }

    // وضعیت دیالوگ‌های تکلیف
    var showHomeworkDialog by remember { mutableStateOf(false) }
    var editingHomework by remember { mutableStateOf<Homework?>(null) }
    var deletingHomework by remember { mutableStateOf<Homework?>(null) }

    // دیالوگ شخصی‌سازی رنگ برنامه (روی همه‌ی تب‌ها اثر می‌گذارد)
    var showColorDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "برنامه هفتگی کلاسی") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // دکمه تغییر نمای صفحه: فقط در تب برنامه هفتگی نمایش داده می‌شود
                navigationIcon = {
                    if (selectedMainTab == 0) {
                        IconButton(onClick = {
                            viewModel.setViewMode(
                                if (isWeekView) VIEW_MODE_DAY else VIEW_MODE_WEEK
                            )
                        }) {
                            Icon(
                                imageVector = if (isWeekView) {
                                    Icons.Default.CalendarToday
                                } else {
                                    Icons.Default.CalendarMonth
                                },
                                contentDescription = if (isWeekView) {
                                    "نمایش تک‌روز"
                                } else {
                                    "نمایش کامل هفته"
                                },
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        // جای خالی هم‌اندازه‌ی دکمه تا عنوان وسط بماند
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                // دکمه شخصی‌سازی رنگ برنامه (مشترک در همه‌ی تب‌ها)
                actions = {
                    IconButton(onClick = { showColorDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "شخصی‌سازی رنگ برنامه",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedMainTab == 0) {
                        // افزودن کلاس: در نمای تک‌روز روی روز انتخابی، در نمای هفته روی امروز
                        formDay = if (isWeekView) {
                            WeekDays.todayIndex()
                        } else {
                            dayPagerState.currentPage
                        }
                        editingClass = null
                        showFormDialog = true
                    } else {
                        // افزودن تکلیف جدید
                        editingHomework = null
                        showHomeworkDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (selectedMainTab == 0) {
                        "افزودن کلاس جدید"
                    } else {
                        "افزودن تکلیف جدید"
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // تب‌های اصلی برنامه
            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                MAIN_TABS.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedMainTab == index,
                        onClick = { selectedMainTab = index },
                        text = { Text(text = tab.title) },
                        icon = { Icon(imageVector = tab.icon, contentDescription = null) }
                    )
                }
            }

            // محتوای تب فعال با انتقال نرم بین تب‌ها
            AnimatedContent(
                targetState = selectedMainTab,
                label = "mainTabContent",
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                },
                modifier = Modifier.fillMaxSize()
            ) { page ->
                if (page == 0) {
                    ScheduleContent(
                        allClasses = allClasses,
                        isWeekView = isWeekView,
                        dayPagerState = dayPagerState,
                        onEditClass = { editingClass = it },
                        onDeleteClass = { deletingClass = it },
                        onAddDayClick = { day ->
                            formDay = day
                            editingClass = null
                            showFormDialog = true
                        }
                    )
                } else {
                    HomeworkContent(
                        homeworkList = allHomework,
                        onEditHomework = { editingHomework = it },
                        onDeleteHomework = { deletingHomework = it },
                        onToggleDone = { viewModel.toggleHomeworkDone(it) }
                    )
                }
            }
        }
    }

    // ---------- دیالوگ‌های کلاس ----------

    // دیالوگ افزودن / ویرایش کلاس
    if (showFormDialog) {
        ClassFormDialog(
            editingClass = editingClass,
            initialDay = formDay,
            suggestedColorIndex = allClasses
                .filter { it.day == formDay }
                .size % ClassColors.count,
            onDismiss = { showFormDialog = false },
            onSave = { saved ->
                if (editingClass == null) {
                    viewModel.addClass(saved)
                } else {
                    viewModel.updateClass(saved)
                }
                showFormDialog = false
            }
        )
    }

    // دیالوگ تأیید حذف کلاس
    deletingClass?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingClass = null },
            title = { Text(text = "حذف کلاس") },
            text = {
                Text(
                    text = "آیا از حذف کلاس «${target.name}» مطمئن هستید؟ این عمل قابل بازگشت نیست."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteClass(target)
                        deletingClass = null
                    }
                ) {
                    Text(
                        text = "حذف",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingClass = null }) {
                    Text(text = "انصراف")
                }
            }
        )
    }

    // ---------- دیالوگ‌های تکلیف ----------

    // دیالوگ افزودن / ویرایش تکلیف
    if (showHomeworkDialog) {
        HomeworkFormDialog(
            editingHomework = editingHomework,
            subjectSuggestions = allClasses
                .map { it.name }
                .filter { it.isNotBlank() }
                .distinct()
                .take(8),
            suggestedColorIndex = allHomework.size % ClassColors.count,
            onDismiss = { showHomeworkDialog = false },
            onSave = { saved ->
                if (editingHomework == null) {
                    viewModel.addHomework(saved)
                } else {
                    viewModel.updateHomework(saved)
                }
                showHomeworkDialog = false
            }
        )
    }

    // دیالوگ تأیید حذف تکلیف
    deletingHomework?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingHomework = null },
            title = { Text(text = "حذف تکلیف") },
            text = {
                Text(
                    text = "آیا از حذف تکلیف «${target.title}» مطمئن هستید؟ این عمل قابل بازگشت نیست."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHomework(target)
                        deletingHomework = null
                    }
                ) {
                    Text(
                        text = "حذف",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingHomework = null }) {
                    Text(text = "انصراف")
                }
            }
        )
    }

    // ---------- دیالوگ رنگ (مشترک) ----------

    // دیالوگ شخصی‌سازی رنگ برنامه
    if (showColorDialog) {
        ColorSettingsDialog(
            selectedIndex = settings.themeIndex,
            customColorArgb = settings.customColor.takeIf { it != -1 },
            onSelectTheme = { viewModel.setThemeIndex(it) },
            onSelectCustomColor = { color ->
                viewModel.setCustomColor(color.toArgb())
            },
            onDismiss = { showColorDialog = false }
        )
    }
}
