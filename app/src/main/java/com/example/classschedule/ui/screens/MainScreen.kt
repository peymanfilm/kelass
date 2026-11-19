package com.example.classschedule.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.AppRoles
import com.example.classschedule.data.AuthRepository
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
 * انواع تب‌ها — برای اینکه منطق دکمه‌ها و دسترسی‌ها بر اساس نقش درست کار کند
 */
private enum class TabKind { SCHEDULE, HOMEWORK, PHOTOS, GRADES, SUMMARY, ACCOUNTS, SETTINGS }

private data class MainTab(val title: String, val icon: ImageVector, val kind: TabKind)

/**
 * تب‌های هر پنل:
 * - دانش‌آموز: برنامه هفتگی، یادآوری تکالیف، انجام تکالیف، کارنامه (فقط دیدن نمره)
 * - معلم: برنامه هفتگی (دیدن)، ثبت تکالیف، ثبت نمرات، گالری تکالیف (دیدن)
 * - مدیر: خلاصه و آمار، مدیریت کلاس‌ها و برنامه هفتگی، کارنامه (دیدن)، گالری تکالیف (دیدن) + مدیریت حساب‌ها
 * - مالک برند MTM Kode: همه‌چیز + مدیریت حساب‌ها + تنظیمات
 */
private val STUDENT_TABS = listOf(
    MainTab(title = "برنامه هفتگی", icon = Icons.Default.MenuBook, kind = TabKind.SCHEDULE),
    MainTab(title = "یادآوری تکالیف", icon = Icons.Default.Assignment, kind = TabKind.HOMEWORK),
    MainTab(title = "انجام تکالیف", icon = Icons.Default.AddAPhoto, kind = TabKind.PHOTOS),
    MainTab(title = "کارنامه", icon = Icons.Default.School, kind = TabKind.GRADES)
)

private val TEACHER_TABS = listOf(
    MainTab(title = "برنامه هفتگی", icon = Icons.Default.MenuBook, kind = TabKind.SCHEDULE),
    MainTab(title = "ثبت تکالیف", icon = Icons.Default.Assignment, kind = TabKind.HOMEWORK),
    MainTab(title = "ثبت نمرات", icon = Icons.Default.School, kind = TabKind.GRADES),
    MainTab(title = "گالری تکالیف", icon = Icons.Default.AddAPhoto, kind = TabKind.PHOTOS)
)

private val MANAGER_TABS = listOf(
    MainTab(title = "خلاصه و آمار", icon = Icons.Default.Insights, kind = TabKind.SUMMARY),
    MainTab(title = "برنامه هفتگی", icon = Icons.Default.MenuBook, kind = TabKind.SCHEDULE),
    MainTab(title = "کارنامه", icon = Icons.Default.School, kind = TabKind.GRADES),
    MainTab(title = "گالری تکالیف", icon = Icons.Default.AddAPhoto, kind = TabKind.PHOTOS),
    MainTab(title = "حساب‌ها", icon = Icons.Default.ManageAccounts, kind = TabKind.ACCOUNTS)
)

private val OWNER_TABS = listOf(
    MainTab(title = "برنامه هفتگی", icon = Icons.Default.MenuBook, kind = TabKind.SCHEDULE),
    MainTab(title = "یادآوری تکالیف", icon = Icons.Default.Assignment, kind = TabKind.HOMEWORK),
    MainTab(title = "گالری تکالیف", icon = Icons.Default.AddAPhoto, kind = TabKind.PHOTOS),
    MainTab(title = "کارنامه", icon = Icons.Default.School, kind = TabKind.GRADES),
    MainTab(title = "حساب‌ها", icon = Icons.Default.ManageAccounts, kind = TabKind.ACCOUNTS),
    MainTab(title = "تنظیمات", icon = Icons.Default.Settings, kind = TabKind.SETTINGS)
)

private fun tabsForRole(role: Int): List<MainTab> = when (role) {
    AppRoles.ROLE_TEACHER -> TEACHER_TABS
    AppRoles.ROLE_MANAGER -> MANAGER_TABS
    AppRoles.ROLE_OWNER -> OWNER_TABS
    else -> STUDENT_TABS
}

/**
 * صفحه اصلی برنامه «کلاس چی» با پنل نقش‌محور و نوار منو در پایین صفحه (سبک اپ‌های موبایل):
 *
 * نقش هر کاربر با حساب ورودش تعیین می‌شود (student1 / teacher1 / manager / owner)
 * و پنل همان نقش باز می‌شود؛ تغییر حساب با «خروج از حساب» و ورود دوباره انجام می‌شود.
 *
 * دسترسی هر نقش (همه‌ی داده‌ها یکی است و آفلاین روی گوشی ذخیره می‌شود):
 * - دانش‌آموز: مدیریت کامل برنامه و تکالیف + بارگذاری عکس + فقط دیدن کارنامه
 * - معلم: دیدن برنامه + ثبت/ویرایش/حذف تکالیف + ثبت نمره و منفی/مثبت + دیدن گالری
 * - مدیر: مدیریت کلاس‌ها و برنامه هفتگی + بقیه فقط برای مشاهده + خلاصه و آمار کلی + تغییر حساب دانش‌آموزان و معلمان
 * - مالک برند MTM Kode: همه‌ی دسترسی‌ها + تغییر حساب مدیر و بقیه + تب تنظیمات و نام مجموعه
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: ScheduleViewModel, role: Int, authRepository: AuthRepository) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allClasses by viewModel.allClasses.collectAsState()
    val allHomework by viewModel.allHomework.collectAsState()
    val allSubjectGrades by viewModel.allSubjectGrades.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isWeekView = settings.viewMode == VIEW_MODE_WEEK

    // تب‌های مخصوص این نقش + تب فعال (با عوض شدن نقش، به تب اول برمی‌گردد)
    val mainTabs = remember(role) { tabsForRole(role) }
    var selectedMainTab by remember(role) { mutableIntStateOf(0) }
    val activeTab = mainTabs[selectedMainTab]

    // دسترسی‌های این نقش — هم‌تراز با پنل‌های وب‌سایت:
    // مدیر هم مثل سایت می‌تواند کلاس‌ها را بسازد و ویرایش کند، ولی نمره ثبت نمی‌کند
    val canEditSchedule = role == AppRoles.ROLE_STUDENT ||
        role == AppRoles.ROLE_MANAGER || role == AppRoles.ROLE_OWNER
    val canEditHomework = role == AppRoles.ROLE_STUDENT ||
        role == AppRoles.ROLE_TEACHER || role == AppRoles.ROLE_OWNER
    val canEditGrades = role == AppRoles.ROLE_TEACHER || role == AppRoles.ROLE_OWNER
    val canAddPhotos = role == AppRoles.ROLE_STUDENT || role == AppRoles.ROLE_OWNER

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
        // نوار منوی اصلی در پایین صفحه — تب‌های مخصوص پنل فعال
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                mainTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedMainTab == index,
                        onClick = { selectedMainTab = index },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                        label = { Text(text = tab.title, maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                // نام مجموعه (اگر مالک برند ثبت کرده باشد) زیر عنوان پنل نمایش داده می‌شود
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "کلاس چی — ${roleTitle(role)}")
                        if (settings.schoolName.isNotBlank()) {
                            Text(
                                text = settings.schoolName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // دکمه تغییر نمای صفحه: فقط در تب برنامه هفتگی نمایش داده می‌شود
                navigationIcon = {
                    if (activeTab.kind == TabKind.SCHEDULE) {
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
                actions = {
                    // خروج از حساب — برگشت به دروازه‌ی ورود (داده‌ها دست‌نخورده می‌مانند)
                    // نقش پنل هم پاک می‌شود تا با حساب بعدی، پنلِ همان حساب باز شود
                    IconButton(onClick = {
                        viewModel.setRole(AppRoles.ROLE_NONE)
                        authRepository.logout()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "خروج از حساب",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    // شخصی‌سازی رنگ برنامه (مشترک در همه‌ی تب‌ها)
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
        // دکمه شناور فقط برای تب‌هایی نشان داده می‌شود که این نقش اجازه‌ی افزودن دارد
        floatingActionButton = {
            if (showFabFor(activeTab.kind, role)) {
                FloatingActionButton(
                    onClick = {
                        when (activeTab.kind) {
                            TabKind.SCHEDULE -> {
                                // افزودن کلاس: در نمای تک‌روز روی روز انتخابی، در نمای هفته روی امروز
                                formDay = if (isWeekView) {
                                    WeekDays.todayIndex()
                                } else {
                                    dayPagerState.currentPage
                                }
                                editingClass = null
                                showFormDialog = true
                            }
                            TabKind.HOMEWORK -> {
                                editingHomework = null
                                showHomeworkDialog = true
                            }
                            TabKind.PHOTOS -> {
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
                                // در تب‌های دیگر دکمه‌ای نشان داده نمی‌شود
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = when (activeTab.kind) {
                            TabKind.SCHEDULE -> "افزودن کلاس جدید"
                            TabKind.HOMEWORK -> "افزودن تکلیف جدید"
                            TabKind.PHOTOS -> "بارگذاری عکس تکلیف"
                            else -> "افزودن"
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // محتوای تب فعال با انتقال نرم بین تب‌ها
            AnimatedContent(
                targetState = selectedMainTab,
                label = "mainTabContent",
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                },
                modifier = Modifier.weight(1f)
            ) { page ->
                when (mainTabs[page].kind) {
                    TabKind.SCHEDULE -> ScheduleContent(
                        allClasses = allClasses,
                        isWeekView = isWeekView,
                        dayPagerState = dayPagerState,
                        readOnly = !canEditSchedule,
                        onEditClass = { editingClass = it },
                        onDeleteClass = { deletingClass = it },
                        onAddDayClick = { day ->
                            formDay = day
                            editingClass = null
                            showFormDialog = true
                        }
                    )
                    TabKind.HOMEWORK -> HomeworkContent(
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
                    TabKind.PHOTOS -> HomeworkPhotosContent(
                        homeworkList = allHomework,
                        readOnly = !canAddPhotos,
                        onAddPhotos = { launchPhotoPickerFor(it) },
                        onOpenPhoto = { homework, index ->
                            photoViewerTarget = homework to index
                        }
                    )
                    TabKind.GRADES -> GradesContent(
                        subjectGrades = allSubjectGrades,
                        homeworkList = allHomework,
                        readOnly = !canEditGrades,
                        allowPhotoUpload = canAddPhotos,
                        onSetContinuousGrade = { name, grade ->
                            viewModel.setContinuousGrade(name, grade)
                        },
                        onSetExamGrade = { name, grade ->
                            viewModel.setExamGrade(name, grade)
                        },
                        onAdjustManualNegative = { name, delta ->
                            viewModel.adjustManualNegative(name, delta)
                        },
                        onAdjustManualPositive = { name, delta ->
                            viewModel.adjustManualPositive(name, delta)
                        },
                        onSetPhotoRequired = { name, required ->
                            viewModel.setPhotoRequired(name, required)
                        },
                        onAddSubject = { viewModel.addSubject(it) },
                        onDeleteSubject = { viewModel.deleteSubject(it) },
                        onAddPhotos = { launchPhotoPickerFor(it) },
                        onOpenPhoto = { homework, index ->
                            photoViewerTarget = homework to index
                        }
                    )
                    TabKind.SUMMARY -> ManagerSummaryContent(
                        allClasses = allClasses,
                        homeworkList = allHomework,
                        subjectGrades = allSubjectGrades
                    )
                    TabKind.ACCOUNTS -> AccountsContent(
                        callerRole = role,
                        authRepository = authRepository
                    )
                    TabKind.SETTINGS -> OwnerSettingsContent(
                        isWeekView = isWeekView,
                        onToggleWeekView = { viewModel.setViewMode(if (it) VIEW_MODE_WEEK else VIEW_MODE_DAY) },
                        onOpenColorSettings = { showColorDialog = true },
                        schoolName = settings.schoolName,
                        onSetSchoolName = { viewModel.setSchoolName(it) },
                        authRepository = authRepository
                    )
                }
            }

            // امضای برنامه — بالای نوار منوی پایین صفحه؛ نام برند برجسته می‌شود
            BrandSignature()
        }
    }

    // ---------- دیالوگ‌های کلاس ----------

    // دیالوگ افزودن / ویرایش کلاس — فقط نقش‌های با اجازه‌ی ویرایش می‌توانند بازش کنند
    if (showFormDialog && canEditSchedule) {
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

    // دیالوگ افزودن / ویرایش تکلیف — فقط نقش‌های با اجازه‌ی ویرایش تکلیف
    if (showHomeworkDialog && canEditHomework) {
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

/**
 * آیا برای این تب و این نقش، دکمه شناور «افزودن» نشان داده شود؟
 */
private fun showFabFor(kind: TabKind, role: Int): Boolean = when (kind) {
    TabKind.SCHEDULE ->
        role == AppRoles.ROLE_STUDENT || role == AppRoles.ROLE_OWNER
    TabKind.HOMEWORK ->
        role == AppRoles.ROLE_STUDENT ||
        role == AppRoles.ROLE_TEACHER || role == AppRoles.ROLE_OWNER
    TabKind.PHOTOS ->
        role == AppRoles.ROLE_STUDENT || role == AppRoles.ROLE_OWNER
    else -> false
}
