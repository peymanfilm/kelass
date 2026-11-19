package com.example.classschedule.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.ClassSchedule
import com.example.classschedule.ui.theme.ClassColorScheme
import com.example.classschedule.ui.theme.ClassColors
import com.example.classschedule.util.toPersianDigits

/**
 * کارت نمایش یک کلاس
 * - لمس کوتاه: ویرایش
 * - لمس طولانی: حذف (با دیالوگ تأیید)
 * - آیکون سطل زباله: حذف / آیکون مداد: ویرایش
 * در حالت فقط-مشاهده (پنل معلم و مدیر) دکمه‌های ویرایش و حذف نشان داده نمی‌شوند
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClassCard(
    classSchedule: ClassSchedule,
    readOnly: Boolean = false,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val scheme: ClassColorScheme = ClassColors.forIndex(classSchedule.colorIndex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                // در حالت فقط-مشاهده کارت کلیک‌پذیر نیست
                if (readOnly) {
                    Modifier
                } else {
                    Modifier.combinedClickable(
                        onClick = onEditClick,
                        onLongClick = onDeleteClick
                    )
                }
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.container),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 4.dp,
                    top = 14.dp,
                    bottom = 12.dp
                ),
                verticalAlignment = Alignment.Top
            ) {
                // دایره رنگی با حرف اول درس
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(scheme.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = classSchedule.name.take(1),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // اطلاعات اصلی کلاس
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = classSchedule.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onContainer,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    InfoRow(
                        icon = Icons.Default.Schedule,
                        text = "${classSchedule.startTime.toPersianDigits()} تا ${classSchedule.endTime.toPersianDigits()}",
                        accent = scheme.accent,
                        contentColor = scheme.onContainer
                    )
                }

                // دکمه‌های ویرایش و حذف — فقط در حالت قابل ویرایش
                if (!readOnly) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش کلاس",
                            tint = scheme.accent
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف کلاس",
                            tint = scheme.accent
                        )
                    }
                }
            }

            // نام استاد و شماره کلاس (در صورت وارد شدن)
            if (classSchedule.teacher.isNotBlank() || classSchedule.room.isNotBlank()) {
                Column(
                    modifier = Modifier.padding(
                        start = 72.dp,
                        end = 16.dp,
                        bottom = 14.dp
                    )
                ) {
                    if (classSchedule.teacher.isNotBlank()) {
                        InfoRow(
                            icon = Icons.Default.Person,
                            text = classSchedule.teacher,
                            accent = scheme.accent,
                            contentColor = scheme.onContainer
                        )
                    }
                    if (classSchedule.room.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoRow(
                            icon = Icons.Default.LocationOn,
                            text = classSchedule.room,
                            accent = scheme.accent,
                            contentColor = scheme.onContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * یک ردیف اطلاعات کوچک با آیکون
 */
@Composable
private fun InfoRow(
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
