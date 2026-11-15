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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.Homework
import com.example.classschedule.ui.components.HomeworkCard
import com.example.classschedule.ui.components.OverdueBadgeContainer
import com.example.classschedule.ui.components.OverdueBadgeText
import com.example.classschedule.util.daysBetween
import com.example.classschedule.util.jalaliToday
import com.example.classschedule.util.toPersianDigits

/**
 * فیلتر نمایش تکالیف در تب «یادآوری تکالیف»
 */
private enum class HomeworkFilter(val label: String) {
    ALL("همه"),
    OPEN("باز"),
    DONE("انجام‌شده")
}

/**
 * محتوای تب «یادآوری تکالیف» — بدون Scaffold و نوار بالا
 * شامل: خلاصه وضعیت، فیلترها (همه / باز / انجام‌شده)
 * و فهرست تکالیف مرتب‌شده بر اساس نزدیک‌ترین مهلت
 */
@Composable
fun HomeworkContent(
    homeworkList: List<Homework>,
    onEditHomework: (Homework) -> Unit,
    onDeleteHomework: (Homework) -> Unit,
    onToggleDone: (Homework) -> Unit
) {
    val today = remember { jalaliToday() }
    var filter by remember { mutableStateOf(HomeworkFilter.ALL) }

    // مرتب‌سازی: تکالیف باز اول (نزدیک‌ترین مهلت جلوتر)، انجام‌شده‌ها آخر
    val sortedHomework = remember(homeworkList, today) {
        homeworkList.sortedWith(
            compareBy(
                { it.isDone },
                { daysBetween(today, it.dueJalali) },
                { it.dueTime }
            )
        )
    }

    // شمارش‌ها برای خلاصه وضعیت و برچسب فیلترها
    val openCount = homeworkList.count { !it.isDone }
    val doneCount = homeworkList.size - openCount
    val overdueCount = homeworkList.count {
        !it.isDone && daysBetween(today, it.dueJalali) < 0
    }

    val filteredHomework = when (filter) {
        HomeworkFilter.ALL -> sortedHomework
        HomeworkFilter.OPEN -> sortedHomework.filter { !it.isDone }
        HomeworkFilter.DONE -> sortedHomework.filter { it.isDone }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (homeworkList.isEmpty()) {
            // هیچ تکلیفی ثبت نشده است
            HomeworkEmptyState()
        } else {
            // خلاصه وضعیت تکالیف
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${openCount.toString().toPersianDigits()} تکلیف باز",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (overdueCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = OverdueBadgeContainer
                    ) {
                        Text(
                            text = "${overdueCount.toString().toPersianDigits()} مورد گذشته از مهلت",
                            style = MaterialTheme.typography.labelSmall,
                            color = OverdueBadgeText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // فیلترهای نمایش تکالیف
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filterCounts = mapOf(
                    HomeworkFilter.ALL to homeworkList.size,
                    HomeworkFilter.OPEN to openCount,
                    HomeworkFilter.DONE to doneCount
                )
                HomeworkFilter.entries.forEach { item ->
                    FilterChip(
                        selected = filter == item,
                        onClick = { filter = item },
                        label = {
                            Text(
                                text = "${item.label} · ${(filterCounts[item] ?: 0).toString().toPersianDigits()}"
                            )
                        }
                    )
                }
            }

            if (filteredHomework.isEmpty()) {
                // دسته انتخابی خالی است
                Text(
                    text = "در این دسته تکلیفی ثبت نشده است",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp)
                )
            } else {
                // فهرست تکالیف
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHomework, key = { it.id }) { item ->
                        HomeworkCard(
                            homework = item,
                            today = today,
                            onToggleDone = { onToggleDone(item) },
                            onEditClick = { onEditHomework(item) },
                            onDeleteClick = { onDeleteHomework(item) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * حالت خالی: وقتی هیچ تکلیفی ثبت نشده باشد
 */
@Composable
private fun HomeworkEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(88.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "تکلیفی ثبت نشده است",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "برای افزودن تکلیف جدید، روی دکمه + پایین صفحه ضربه بزنید.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
