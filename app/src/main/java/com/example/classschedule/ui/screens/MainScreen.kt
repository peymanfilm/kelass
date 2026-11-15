package com.example.classschedule.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.ClassSchedule
import com.example.classschedule.data.Homework
import com.example.classschedule.data.HomeworkImageStore
import com.example.classschedule.data.SettingsRepository.Companion.VIEW_MODE_DAY
import com.example.classschedule.data.SettingsRepository.Companion.VIEW_MODE_WEEK
import com.example.classschedule.ui.components.ClassFormDialog
import com.example.classschedule.ui.components.ColorSettingsDialog
import com.example.classschedule.ui.components.HomeworkFormDialog
import com.example.classschedule.ui.components.HomeworkPhotoViewerDialog
import com.example.classschedule.ui.theme.ClassColors
import com.example.classschedule.util.WeekDays
import com.example.classschedule.viewmodel.ScheduleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * تعریف تب‌های اصلی برنامه — در آینده می‌توان تب‌های جدیدی به این فهرست افزود
 */
private data class MainTab(val title: String, val icon: ImageVector)

private val MAIN_TABS = listOf(
    MainTab(title = "برنامه هفتگی", icon = Icons.Default.MenuBook),
    MainTab(title = "یادآوری تکالیف", icon = Icons.Default.Assignment),
    MainTab(title = "عکس تکالیف", icon = Icons.Default.AddAPhoto),
    MainTab(title = "نمرات", icon = Icons.Default.School)
)

/**
 * صفحه اصلی برنامه با تب‌های بالای صفحه:
 * ۱) برنامه هفتگی: نمای کامل هفته یا تک‌روز (بر اساس انتخاب کاربر)
 * ۲) یادآوری تکالیف: فهرست تکالیف با تاریخ مهلت شمسی و وضعیت انجام
 * ۳) عکس تکالیف: گالری عکس‌های بارگذاری‌شده برای هر تکلیف
 * ۴) نمرات: شمارش منفی‌ها — تکلیفی که مهلتش گذشته و عکسی ندارد منفی می‌گیرد
 * نوار بالا، دکمه شناور افزودن و همه‌ی دیالوگ‌ها اینجا مدیریت می‌شوند.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ScheduleViewModel) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allClasses by viewModel.allClasses.collectAsState()
    val allHomework by viewModel.allHomework.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isWeekView = settings.viewMode == VIEW_MODE_WEEK

    // تب اصلی فعال: ۰ = برنامه هفتگی، ۱ = یادآوری تکالیف، ۲ = عکس تکالیف، ۳ = نمرات
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

    // وضعیت عکس‌های تکلیف:
    // تکلیفی که کاربر برای آن «افزودن عکس» را لمس کرده و منتظر انتخاب گالری است
    var photoPickTarget by remember { mutableStateOf<Homework?>(null) }
    // نمایشگر تمام‌صفحه عکس‌ها: تکلیف + شماره عکس آغازین
    var photoViewerTarget by remember { mutableStateOf<Pair<Homework, Int>?>(null) }
    // دیالوگ انتخاب تکلیف برای بارگذاری عکس (از تب گالری با دکمه شناور)
    var showPhotoTargetDialog by remember { mutableStateOf(false) }

    // انتخابگر گالری برای افزودن عکس از روی کارت تکلیف یا گالری:
    // عکس‌های انتخابی کوچک شده و در حافظه‌ی داخلی برنامه ذخیره می‌شوند
    val pickCardPhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val target = photoPickTarget
        photoPickTarget = null
        if (target == null || uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            val savedNames = uris.mapNotNull { uri ->
                withContext(Dispatchers.IO) {
                    HomeworkImageStore.saveImageFromUri(context, uri)
                }
            }
            viewModel.addPhotosToHomework(target, savedNames)
        }
    }

    // باز کردن انتخابگر گالری برای یک تکلیف مشخص
    val launchPhotoPickerFor = { homework: Homework ->
        photoPickTarget = homework
        pickCardPhotosLauncher.launch("image/*")
    }

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
                    when (selectedMainTab) {
                        0 -> {
                            // افزودن کلاس: در نمای تک‌روز روی روز انتخابی، در نمای هفته روی امروز
                            formDay = if (isWeekView) {
                                WeekDays.todayIndex()
                            } else {
                                dayPagerState.currentPage
                            }
                            editingClass = null
                            showFormDialog = true
                        }
                        1 -> {
                            // افزودن تکلیف جدید
                            editingHomework = null
                            showHomeworkDialog = true
                        }
                        2 -> {
                            // بارگذاری عکس از تب گالری:
                            // اگر تکلیفی نیست، فرم افزودن تکلیف باز می‌شود؛
                            // وگرنه کاربر تکلیف مورد نظر را انتخاب می‌کند
                            if (allHomework.isEmpty()) {
                                editingHomework = null
                                showHomeworkDialog = true
                            } else {
                                showPhotoTargetDialog = true
                            }
                        }
                        else -> {
                            // تب نمرات: افزودن تکلیف جدید
                            editingHomework = null
                            showHomeworkDialog = true
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = when (selectedMainTab) {
                        0 -> "افزودن کلاس جدید"
                        1 -> "افزودن تکلیف جدید"
                        2 -> "بارگذاری عکس تکلیف"
                        else -> "افزودن تکلیف جدید"
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
            // تب‌های اصلی برنامه — اسکرول‌پذیر تا در صفحه‌های باریک هم جا شوند
            ScrollableTabRow(
                selectedTabIndex = selectedMainTab,
                edgePadding = 12.dp,
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
                when (page) {
                    0 -> ScheduleContent(
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
                    1 -> HomeworkContent(
                        homeworkList = allHomework,
                        onEditHomework = { editingHomework = it },
                        onDeleteHomework = { deletingHomework = it },
                        onToggleDone = { viewModel.toggleHomeworkDone(it) },
                        onAddPhotos = { launchPhotoPickerFor(it) },
                        onOpenPhoto = { homework, index ->
                            photoViewerTarget = homework to index
                        },
                        onDoneBlocked = {
                            // قانون: تکلیف بدون عکس تیک نمی‌خورد — راهنمای کاربر
                            Toast.makeText(
                                context,
                                "برای تیک‌زدن تکلیف، اول یک عکس از آن بارگذاری کنید",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                    2 -> HomeworkPhotosContent(
                        homeworkList = allHomework,
                        onAddPhotos = { launchPhotoPickerFor(it) },
                        onOpenPhoto = { homework, index ->
                            photoViewerTarget = homework to index
                        }
                    )
                    else -> GradesContent(
                        homeworkList = allHomework,
                        onAddPhotos = { launchPhotoPickerFor(it) },
                        onOpenPhoto = { homework, index ->
                            photoViewerTarget = homework to index
                        }
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

    // دیالوگ تأیید حذف تکلیف — عکس‌های ذخیره‌شده‌ی آن هم از حافظه پاک می‌شوند
    deletingHomework?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingHomework = null },
            title = { Text(text = "حذف تکلیف") },
            text = {
                Text(
                    text = "آیا از حذف تکلیف «${target.title}» مطمئن هستید؟ عکس‌های آن هم حذف می‌شوند و این عمل قابل بازگشت نیست."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        HomeworkImageStore.deleteImages(context, target.imageList)
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

    // ---------- دیالوگ‌های عکس تکلیف ----------

    // نمایشگر تمام‌صفحه‌ی عکس‌های تکلیف — با کشیدن انگشت میان عکس‌ها جابه‌جا می‌شوید
    photoViewerTarget?.let { (target, startIndex) ->
        // همیشه آخرین وضعیت تکلیف از دیتابیس خوانده می‌شود تا عکس‌های تازه هم دیده شوند
        val fresh = allHomework.firstOrNull { it.id == target.id } ?: target
        HomeworkPhotoViewerDialog(
            photos = fresh.imageList,
            initialIndex = startIndex,
            onDismiss = { photoViewerTarget = null },
            onDeletePhoto = { index ->
                val name = fresh.imageList.getOrNull(index) ?: return@HomeworkPhotoViewerDialog
                // فایل عکس از حافظه‌ی برنامه پاک و فهرست تکلیف به‌روزرسانی می‌شود
                HomeworkImageStore.deleteImage(context, name)
                viewModel.removePhotoFromHomework(fresh, name)
            }
        )
    }

    // دیالوگ انتخاب تکلیف برای بارگذاری عکس (دکمه شناور در تب گالری)
    if (showPhotoTargetDialog) {
        HomeworkPickerForPhotosDialog(
            homeworkList = allHomework.sortedWith(
                compareBy(
                    { it.isEffectivelyDone },
                    { it.dueYear },
                    { it.dueMonth },
                    { it.dueDay }
                )
            ),
            onSelect = { homework ->
                showPhotoTargetDialog = false
                launchPhotoPickerFor(homework)
            },
            onDismiss = { showPhotoTargetDialog = false }
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
