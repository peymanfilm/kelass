package com.example.classschedule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.ClassSchedule
import com.example.classschedule.data.Homework
import com.example.classschedule.data.SubjectGrade
import com.example.classschedule.util.daysBetween
import com.example.classschedule.util.formatGradePersian
import com.example.classschedule.util.jalaliToday
import com.example.classschedule.util.toPersianDigits
import kotlin.math.roundToInt

/**
 * محتوای تب «خلاصه و آمار» در پنل مدیر — فقط مشاهده:
 * ۱) کارت خوش‌آمد با تاریخ شمسی امروز
 * ۲) شبکه آمار: تعداد درس‌ها، کلاس‌های هفته، تکالیف باز، تکالیف انجام‌شده
 * ۳) معدل کل + جدول معدل هر درس (بر اساس همان قانون کارنامه)
 * ۴) تکالیف مهلت‌گذشته‌ی بدون عکس (نکته‌ی قابل پیگیری برای مدیر)
 * ۵) یادآوری: در پنل مدیر همه‌چیز فقط برای مشاهده است
 */

/** رنگ‌های آمار — هماهنگ با سبک کلی برنامه و بدون رنگ آبی/نیلی */
private val StatTeal = Color(0xFF00695C)
private val StatTealContainer = Color(0xFFE0F2F1)
private val StatGreen = Color(0xFF2E7D32)
private val StatGreenContainer = Color(0xFFE8F5E9)
private val StatOrange = Color(0xFFE65100)
private val StatOrangeContainer = Color(0xFFFFF3E0)
private val StatRed = Color(0xFFC62828)
private val StatRedContainer = Color(0xFFFFEBEE)

/** محاسبه ساده معدل یک درس بر اساس قانون کارنامه (نمایشی، بدون دیالوگ ویرایش) */
private data class SubjectStat(
    val name: String,
    val average: Float?
)

@Composable
fun ManagerSummaryContent(
    allClasses: List<ClassSchedule>,
    homeworkList: List<Homework>,
    subjectGrades: List<SubjectGrade>
) {
    val today = remember { jalaliToday() }

    // تکالیف مهلت‌گذشته‌ی بدون عکس — برای شمارش منفی خودکار هر درس
    val overdueWithoutPhoto = remember(homeworkList, today) {
        homeworkList.filter {
            it.imageList.isEmpty() && daysBetween(today, it.dueJalali) < 0
        }
    }

    // آمار کلی
    val openCount = homeworkList.count { !it.isEffectivelyDone }
    val doneCount = homeworkList.count { it.isEffectivelyDone }
    val subjectCount = subjectGrades.count { it.name != SubjectGrade.NO_SUBJECT }

    // معدل هر درس: نمره مستمر نهایی (با منفی‌های خودکار و دستی) + نمره امتحان
    val subjectStats = remember(subjectGrades, overdueWithoutPhoto, today) {
        val gradeByName = subjectGrades.associateBy { it.name }
        val autoNegativesBySubject = overdueWithoutPhoto
            .filter { gradeByName[it.subject.trim()]?.photoRequired ?: true }
            .groupBy { it.subject.trim() }
            .mapValues { it.value.size }

        subjectGrades
            .filter { it.name != SubjectGrade.NO_SUBJECT }
            .map { row ->
                val negatives = row.manualNegatives + (autoNegativesBySubject[row.name] ?: 0)
                val finalContinuous = row.continuousGrade?.let {
                    (it - negatives * SubjectGrade.PENALTY_PER_NEGATIVE +
                        row.manualPositives * SubjectGrade.PENALTY_PER_NEGATIVE)
                        .coerceIn(0f, SubjectGrade.MAX_GRADE)
                }
                val average = when {
                    finalContinuous != null && row.examGrade != null ->
                        (finalContinuous + row.examGrade) / 2f
                    finalContinuous != null -> finalContinuous
                    row.examGrade != null -> row.examGrade
                    else -> null
                }
                SubjectStat(name = row.name, average = average)
            }
    }

    // معدل کل: میانگین معدل درس‌هایی که نمره‌شان قابل محاسبه است
    val overallAverage = remember(subjectStats) {
        val averages = subjectStats.mapNotNull { it.average }
        if (averages.isEmpty()) null else averages.sum() / averages.size
    }
    val sortedSubjects = remember(subjectStats) {
        subjectStats.filter { it.average != null }.sortedByDescending { it.average }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ---------- کارت خوش‌آمد ----------
        item(key = "welcome") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(StatTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "خلاصه‌ی کلی مدرسه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "امروز: ${today.year.toString().toPersianDigits()}/" +
                                "${today.month.toString().toPersianDigits()}/" +
                                "${today.day.toString().toPersianDigits()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---------- شبکه آمار ----------
        item(key = "stats-grid") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    icon = Icons.Default.MenuBook,
                    value = subjectCount,
                    label = "درس در کارنامه",
                    accent = StatTeal,
                    container = StatTealContainer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.CalendarMonth,
                    value = allClasses.size,
                    label = "کلاس در برنامه هفتگی",
                    accent = StatOrange,
                    container = StatOrangeContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item(key = "stats-grid-2") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    icon = Icons.Default.Assignment,
                    value = openCount,
                    label = "تکلیف باز",
                    accent = StatRed,
                    container = StatRedContainer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.CheckCircle,
                    value = doneCount,
                    label = "تکلیف انجام‌شده",
                    accent = StatGreen,
                    container = StatGreenContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ---------- معدل کل ----------
        item(key = "overall-average") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "معدل کل مدرسه",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "میانگین معدل درس‌هایی که نمره‌شان ثبت شده است",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = overallAverage?.let { formatGradePersian(it) } ?: "—",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ---------- معدل هر درس ----------
        if (sortedSubjects.isNotEmpty()) {
            item(key = "subject-averages-header") {
                Text(
                    text = "معدل هر درس",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            items(
                sortedSubjects.size,
                key = { "stat-$it" }
            ) { index ->
                val stat = sortedSubjects[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (index + 1).toString().toPersianDigits(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stat.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = stat.average?.let { formatGradePersian(it) } ?: "—",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // ---------- تکالیف مهلت‌گذشته بدون عکس ----------
        if (overdueWithoutPhoto.isNotEmpty()) {
            item(key = "overdue-header") {
                Text(
                    text = "نیاز به پیگیری",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            item(key = "overdue-note") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatRedContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${overdueWithoutPhoto.size.toString().toPersianDigits()} " +
                                "تکلیف از مهلت گذشته و هنوز عکسی برایش بارگذاری نشده است. " +
                                "این موضوع در کارنامه «منفی» ثبت می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB71C1C)
                        )
                    }
                }
            }
        }

        // ---------- یادآوری فقط-مشاهده ----------
        item(key = "view-only-note") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "در پنل مدیر همه‌چیز فقط برای مشاهده است. " +
                            "برای ثبت تکلیف و نمره، پنل معلم را انتخاب کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * کارت آمار کوچک: آیکون + عدد بزرگ + برچسب
 */
@Composable
private fun StatCard(
    icon: ImageVector,
    value: Int,
    label: String,
    accent: Color,
    container: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value.toString().toPersianDigits(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
