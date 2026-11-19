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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.ClassSchedule
import com.example.classschedule.ui.components.ClassCard
import com.example.classschedule.util.WeekDays
import com.example.classschedule.util.toPersianDigits
import kotlinx.coroutines.launch

/**
 * محتوای تب «برنامه هفتگی» — بدون Scaffold و نوار بالا (که در MainScreen مشترک است)
 * دو حالت نمایش دارد:
 * ۱) نمای کامل هفته (پیش‌فرض): همه روزهای هفته یک‌جا و زیر هم
 * ۲) نمای تک‌روز: تب‌های روزهای هفته + سوایپ افقی
 *
 * readOnly: در پنل معلم و مدیر برنامه فقط قابل مشاهده است (بدون دکمه‌های ویرایش و حذف)
 */
@Composable
fun ScheduleContent(
    allClasses: List<ClassSchedule>,
    isWeekView: Boolean,
    dayPagerState: PagerState,
    readOnly: Boolean = false,
    onEditClass: (ClassSchedule) -> Unit,
    onDeleteClass: (ClassSchedule) -> Unit,
    onAddDayClick: (Int) -> Unit
) {
    // تاریخ شمسی روزهای هفته جاری (مثل ۱۴۰۵/۸/۲۴) برای نمایش زیر نام روزها
    val weekDateLabels = remember { WeekDays.weekJalaliDates().map { it.toCompactString() } }
    val scope = rememberCoroutineScope()

    if (isWeekView) {
        // نمای کامل هفته: همه روزها زیر هم
        WeekScheduleView(
            allClasses = allClasses,
            weekDateLabels = weekDateLabels,
            readOnly = readOnly,
            onEditClick = { onEditClass(it) },
            onDeleteClick = { onDeleteClass(it) },
            onAddDayClick = onAddDayClick
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // تب‌های روزهای هفته (فقط در نمای تک‌روز)
            ScrollableTabRow(
                selectedTabIndex = dayPagerState.currentPage,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                WeekDays.names.forEachIndexed { index, dayName ->
                    val selected = dayPagerState.currentPage == index
                    Tab(
                        selected = selected,
                        onClick = {
                            scope.launch {
                                dayPagerState.animateScrollToPage(index)
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
                state = dayPagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val dayClasses = allClasses
                    .filter { it.day == page }
                    .sortedBy { it.startTime }

                DaySchedulePage(
                    dayIndex = page,
                    classes = dayClasses,
                    readOnly = readOnly,
                    onEditClick = { onEditClass(it) },
                    onDeleteClick = { onDeleteClass(it) }
                )
            }
        }
    }
}

/**
 * نمای کامل هفته: هر ۷ روز به ترتیب با سربرگ مخصوص هر روز
 * و کلاس‌های همان روز زیر سربرگ آن
 */
@Composable
private fun WeekScheduleView(
    allClasses: List<ClassSchedule>,
    weekDateLabels: List<String>,
    readOnly: Boolean,
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
            top = 8.dp,
            bottom = 96.dp
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
                    readOnly = readOnly,
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
                        readOnly = readOnly,
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
 * در حالت فقط-مشاهده دکمه افزودن نشان داده نمی‌شود
 */
@Composable
private fun DayHeader(
    dayName: String,
    jalaliDate: String,
    isToday: Boolean,
    classCount: Int,
    readOnly: Boolean,
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
        // دکمه افزودن کلاس به این روز — فقط در حالت قابل ویرایش
        if (!readOnly) {
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
    readOnly: Boolean,
    onEditClick: (ClassSchedule) -> Unit,
    onDeleteClick: (ClassSchedule) -> Unit
) {
    if (classes.isEmpty()) {
        EmptyState(readOnly = readOnly)
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
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
                    readOnly = readOnly,
                    onEditClick = { onEditClick(classItem) },
                    onDeleteClick = { onDeleteClick(classItem) }
                )
            }
        }
    }
}

/**
 * حالت خالی: وقتی برای روز انتخابی کلاسی ثبت نشده باشد
 * در حالت فقط-مشاهده، راهنمای افزودن کلاس نشان داده نمی‌شود
 */
@Composable
private fun EmptyState(readOnly: Boolean = false) {
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
        if (!readOnly) {
            Text(
                text = "برای افزودن کلاس جدید، روی دکمه + پایین صفحه ضربه بزنید.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
