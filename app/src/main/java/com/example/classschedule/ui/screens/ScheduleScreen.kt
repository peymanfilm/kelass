package com.example.classschedule.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.ClassSchedule
import com.example.classschedule.data.SettingsRepository.Companion.VIEW_MODE_DAY
import com.example.classschedule.data.SettingsRepository.Companion.VIEW_MODE_WEEK
import com.example.classschedule.ui.components.ClassCard
import com.example.classschedule.ui.components.ClassFormDialog
import com.example.classschedule.ui.components.ColorSettingsDialog
import com.example.classschedule.ui.theme.ClassColors
import com.example.classschedule.util.WeekDays
import com.example.classschedule.util.toPersianDigits
import com.example.classschedule.viewmodel.ScheduleViewModel
import kotlinx.coroutines.launch

/**
 * صفحه اصلی برنامه با دو حالت نمایش:
 * ۱) نمای کامل هفته (پیش‌فرض): همه روزهای هفته یک‌جا و زیر هم
 * ۲) نمای تک‌روز: تب‌های روزهای هفته + سوایپ افقی
 * کاربر با دکمه تغییر نما (نوار بالا) بین دو حالت جابه‌جا می‌شود
 * و با دکمه پالت، رنگ برنامه را شخصی‌سازی می‌کند.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel) {

    val allClasses by viewModel.allClasses.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val isWeekView = settings.viewMode == VIEW_MODE_WEEK

    // تاریخ شمسی روزهای هفته جاری (مثل ۱۴۰۵/۸/۲۴) برای نمایش زیر نام روزها
    val weekDateLabels = remember { WeekDays.weekJalaliDates().map { it.toCompactString() } }

    // صفحه‌گرد (Pager) که با تب‌ها هماهنگ است و روی روز «امروز» باز می‌شود
    val pagerState = rememberPagerState(initialPage = WeekDays.todayIndex()) {
        WeekDays.names.size
    }
    val scope = rememberCoroutineScope()

    var showFormDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<ClassSchedule?>(null) }
    var deletingClass by remember { mutableStateOf<ClassSchedule?>(null) }
    var formDay by remember { mutableIntStateOf(WeekDays.todayIndex()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "برنامه هفتگی کلاسی") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // دکمه تغییر نمای صفحه: کامل هفته یا تک‌روز
                navigationIcon = {
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
                },
                // دکمه شخصی‌سازی رنگ برنامه
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
                    formDay = if (isWeekView) {
                        WeekDays.todayIndex()
                    } else {
                        pagerState.currentPage
                    }
                    editingClass = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "افزودن کلاس جدید"
                )
            }
        }
    ) { innerPadding ->
        if (isWeekView) {
            // نمای کامل هفته: همه روزها زیر هم
            WeekScheduleView(
                allClasses = allClasses,
                contentPadding = innerPadding,
                weekDateLabels = weekDateLabels,
                onEditClick = { editingClass = it },
                onDeleteClick = { deletingClass = it },
                onAddDayClick = { day ->
                    formDay = day
                    editingClass = null
                    showFormDialog = true
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // تب‌های روزهای هفته (فقط در نمای تک‌روز)
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    WeekDays.names.forEachIndexed { index, dayName ->
                        val selected = pagerState.currentPage == index
                        Tab(
                            selected = selected,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayName,
                                        fontWeight = if (selected) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        }
                                    )
                                    // تاریخ شمسی زیر نام روز؛ روز جاری با برچسب «امروز»
                                    Text(
                                        text = if (index == WeekDays.todayIndex()) {
                                            "امروز · ${weekDateLabels[index]}"
                                        } else {
                                            weekDateLabels[index]
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (index == WeekDays.todayIndex()) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        }
                                    )
                                }
                            }
                        )
                    }
                }

                // صفحات روزها با قابلیت سوایپ افقی
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val dayClasses = allClasses
                        .filter { it.day == page }
                        .sortedBy { it.startTime }

                    DaySchedulePage(
                        dayIndex = page,
                        classes = dayClasses,
                        onEditClick = { editingClass = it },
                        onDeleteClick = { deletingClass = it }
                    )
                }
            }
        }
    }

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
 * نمای کامل هفته: هر ۷ روز به ترتیب با سربرگ مخصوص هر روز
 * و کلاس‌های همان روز زیر سربرگ آن
 */
@Composable
private fun WeekScheduleView(
    allClasses: List<ClassSchedule>,
    contentPadding: PaddingValues,
    weekDateLabels: List<String>,
    onEditClick: (ClassSchedule) -> Unit,
    onDeleteClick: (ClassSchedule) -> Unit,
    onAddDayClick: (Int) -> Unit
) {
    val today = WeekDays.todayIndex()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WeekDays.names.forEachIndexed { dayIndex, dayName ->
            val dayClasses = allClasses
                .filter { it.day == dayIndex }
                .sortedBy { it.startTime }

            item(key = "header_$dayIndex") {
                DayHeader(
                    dayName = dayName,
                    jalaliDate = weekDateLabels[dayIndex],
                    isToday = dayIndex == today,
                    classCount = dayClasses.size,
                    onAddClick = { onAddDayClick(dayIndex) }
                )
            }

            if (dayClasses.isEmpty()) {
                item(key = "empty_$dayIndex") {
                    EmptyDayRow()
                }
            } else {
                items(dayClasses, key = { it.id }) { classItem ->
                    ClassCard(
                        classSchedule = classItem,
                        onEditClick = { onEditClick(classItem) },
                        onDeleteClick = { onDeleteClick(classItem) }
                    )
                }
            }
        }
    }
}

/**
 * سربرگ هر روز در نمای هفته:
 * نام روز + تاریخ شمسی + برچسب «امروز» + تعداد کلاس + دکمه افزودن مخصوص همان روز
 */
@Composable
private fun DayHeader(
    dayName: String,
    jalaliDate: String,
    isToday: Boolean,
    classCount: Int,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = dayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isToday) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            // تاریخ شمسی این روز (مثل ۱۴۰۵/۸/۲۴)
            Text(
                text = jalaliDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        if (isToday) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "امروز",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${classCount.toString().toPersianDigits()} کلاس",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        // دکمه افزودن کلاس به این روز
        IconButton(
            onClick = onAddClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "افزودن کلاس به $dayName",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * ردیف فشرده حالت خالی برای هر روز در نمای هفته
 */
@Composable
private fun EmptyDayRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "کلاسی ثبت نشده است",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

/**
 * محتوای هر روز در نمای تک‌روز: لیست کلاس‌های آن روز یا حالت خالی
 */
@Composable
private fun DaySchedulePage(
    dayIndex: Int,
    classes: List<ClassSchedule>,
    onEditClick: (ClassSchedule) -> Unit,
    onDeleteClick: (ClassSchedule) -> Unit
) {
    if (classes.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = WeekDays.names[dayIndex],
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${classes.size.toString().toPersianDigits()} کلاس",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            items(classes, key = { it.id }) { classItem ->
                ClassCard(
                    classSchedule = classItem,
                    onEditClick = { onEditClick(classItem) },
                    onDeleteClick = { onDeleteClick(classItem) }
                )
            }
        }
    }
}

/**
 * حالت خالی: وقتی برای روز انتخابی کلاسی ثبت نشده باشد
 */
@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(88.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "برای این روز کلاسی ثبت نشده است",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "برای افزودن کلاس جدید، روی دکمه + پایین صفحه ضربه بزنید.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
