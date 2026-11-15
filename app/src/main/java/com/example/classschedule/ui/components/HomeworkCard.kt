package com.example.classschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.Homework
import com.example.classschedule.ui.theme.ClassColorScheme
import com.example.classschedule.ui.theme.ClassColors
import com.example.classschedule.util.JalaliDate
import com.example.classschedule.util.daysBetween
import com.example.classschedule.util.toPersianDigits

/**
 * رنگ‌های ثابت برچسب‌های وضعیت مهلت — مستقل از تم برنامه تا معنای وضعیت همیشه یکسان بماند
 */
internal val OverdueBadgeContainer = Color(0xFFFFCDD2)
internal val OverdueBadgeText = Color(0xFF9A1B1B)
internal val TodayBadgeContainer = Color(0xFFFFE0B2)
internal val TodayBadgeText = Color(0xFF9A4B00)
internal val TomorrowBadgeContainer = Color(0xFFFFF9C4)
internal val TomorrowBadgeText = Color(0xFF827717)
internal val DoneBadgeContainer = Color(0xFFC8E6C9)
internal val DoneBadgeText = Color(0xFF1B5E20)

/** برچسب وضعیت مهلت تکلیف: متن + رنگ (مقدار null یعنی رنگ‌های خنثی تم) */
private data class DueBadge(val label: String, val containerColor: Color?, val textColor: Color?)

/**
 * کارت نمایش یک تکلیف در تب «یادآوری تکالیف»
 * - دایره وضعیت: لمس برای انجام‌شده/بازگشت به حالت باز
 * - برچسب رنگی وضعیت مهلت: گذشته از مهلت / امروز / فردا / روزهای مانده
 * - آیکون مداد: ویرایش / آیکون سطل زباله: حذف (با دیالوگ تأیید)
 */
@Composable
fun HomeworkCard(
    homework: Homework,
    today: JalaliDate,
    onToggleDone: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val scheme: ClassColorScheme = ClassColors.forIndex(homework.colorIndex)
    val diffDays = daysBetween(from = today, to = homework.dueJalali)

    // برچسب وضعیت مهلت بر اساس فاصله تا امروز
    val badge = when {
        homework.isDone -> DueBadge(
            label = "انجام شد",
            containerColor = DoneBadgeContainer,
            textColor = DoneBadgeText
        )
        diffDays < 0 -> DueBadge(
            label = "${(-diffDays).toString().toPersianDigits()} روز گذشته",
            containerColor = OverdueBadgeContainer,
            textColor = OverdueBadgeText
        )
        diffDays == 0 -> DueBadge(
            label = "امروز",
            containerColor = TodayBadgeContainer,
            textColor = TodayBadgeText
        )
        diffDays == 1 -> DueBadge(
            label = "فردا",
            containerColor = TomorrowBadgeContainer,
            textColor = TomorrowBadgeText
        )
        diffDays <= 6 -> DueBadge(
            label = "${diffDays.toString().toPersianDigits()} روز مانده",
            containerColor = null,
            textColor = null
        )
        else -> DueBadge(
            label = "${homework.dueDay.toString().toPersianDigits()} ${homework.dueJalali.monthName()}",
            containerColor = null,
            textColor = null
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.container),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 4.dp,
                top = 14.dp,
                bottom = 12.dp
            ),
            verticalAlignment = Alignment.Top
        ) {
            // دایره وضعیت انجام‌شدن: لمس برای تغییر وضعیت
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (homework.isDone) scheme.accent else Color.Transparent)
                    .border(width = 2.dp, color = scheme.accent, shape = CircleShape)
                    .clickable(onClickLabel = "تغییر وضعیت انجام تکلیف", onClick = onToggleDone),
                contentAlignment = Alignment.Center
            ) {
                if (homework.isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // اطلاعات اصلی تکلیف
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = homework.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onContainer.copy(alpha = if (homework.isDone) 0.65f else 1f),
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (homework.isDone) TextDecoration.LineThrough else null
                )

                Spacer(modifier = Modifier.height(6.dp))

                // ردیف مهلت + برچسب وضعیت
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = scheme.accent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مهلت: ${homework.dueJalali.toCompactString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badge.containerColor
                            ?: MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = badge.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = badge.textColor
                                ?: MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // ساعت مهلت (در صورت تعیین)
                if (homework.dueTime.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HomeworkInfoRow(
                        icon = Icons.Default.Schedule,
                        text = "ساعت: ${homework.dueTime.toPersianDigits()}",
                        accent = scheme.accent,
                        contentColor = scheme.onContainer
                    )
                }

                // نام درس (در صورت وارد شدن)
                if (homework.subject.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HomeworkInfoRow(
                        icon = Icons.Default.MenuBook,
                        text = homework.subject,
                        accent = scheme.accent,
                        contentColor = scheme.onContainer
                    )
                }

                // توضیحات (در صورت وارد شدن)
                if (homework.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HomeworkInfoRow(
                        icon = Icons.Default.Notes,
                        text = homework.note,
                        accent = scheme.accent,
                        contentColor = scheme.onContainer
                    )
                }
            }

            // دکمه‌های ویرایش و حذف
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "ویرایش تکلیف",
                    tint = scheme.accent
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف تکلیف",
                    tint = scheme.accent
                )
            }
        }
    }
}

/**
 * یک ردیف اطلاعات کوچک با آیکون (مخصوص کارت تکلیف)
 */
@Composable
private fun HomeworkInfoRow(
    icon: ImageVector,
    text: String,
    accent: Color,
    contentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = accent
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}
