package com.example.classschedule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.Homework
import com.example.classschedule.ui.components.HomeworkAddPhotoTile
import com.example.classschedule.ui.components.HomeworkPhotosEmptyState
import com.example.classschedule.ui.components.HomeworkThumbnail
import com.example.classschedule.ui.theme.ClassColors
import com.example.classschedule.util.toPersianDigits

/**
 * محتوای تب «انجام تکالیف» — گالری عکس‌های تکالیف انجام‌شده
 * عکس‌های هر تکلیف زیر نام آن نشان داده می‌شود و با لمس، تمام‌صفحه باز می‌شود.
 * دکمه‌ی افزودن عکس در سرصفحه‌ی هر تکلیف و کاشی افزودن در انتهای ردیف عکس‌ها هست.
 */
@Composable
fun HomeworkPhotosContent(
    homeworkList: List<Homework>,
    onAddPhotos: (Homework) -> Unit,
    onOpenPhoto: (Homework, Int) -> Unit
) {
    if (homeworkList.isEmpty()) {
        // هنوز تکلیفی ثبت نشده است
        HomeworkPhotosEmptyState(
            message = "هنوز تکلیفی ثبت نشده است\nابتدا در تب «یادآوری تکالیف» یک تکلیف بسازید."
        )
        return
    }

    // ترتیب تکالیف در گالری همان ترتیب تب تکالیف است (بازها با مهلت نزدیک‌تر جلوتر)
    // تکلیف بدون عکس همیشه «باز» حساب می‌شود
    val groups = remember(homeworkList) {
        homeworkList.sortedWith(
            compareBy(
                { it.isEffectivelyDone },
                { it.dueYear },
                { it.dueMonth },
                { it.dueDay }
            )
        )
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
        groups.forEach { homework ->
            val scheme = ClassColors.forIndex(homework.colorIndex)
            val photos = homework.imageList

            // سرصفحه‌ی هر تکلیف: رنگ درس + عنوان + تعداد عکس + دکمه افزودن عکس
            item(key = "header-${homework.id}") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(scheme.accent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = homework.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${photos.size.toString().toPersianDigits()} عکس",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = { onAddPhotos(homework) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "افزودن عکس برای «${homework.title}»",
                            tint = scheme.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            if (photos.isEmpty()) {
                // این تکلیف هنوز عکسی ندارد
                item(key = "empty-${homework.id}") {
                    Text(
                        text = "هنوز عکسی برای این تکلیف بارگذاری نشده است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 20.dp, top = 2.dp, bottom = 4.dp)
                    )
                }
            } else {
                // ردیف بندانگشتی عکس‌های این تکلیف + کاشی افزودن
                item(key = "photos-${homework.id}") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            itemsIndexed(photos, key = { _, name -> name }) { index, name ->
                                HomeworkThumbnail(
                                    name = name,
                                    onClick = { onOpenPhoto(homework, index) }
                                )
                            }
                            item(key = "add-${homework.id}") {
                                HomeworkAddPhotoTile(
                                    onClick = { onAddPhotos(homework) },
                                    accent = scheme.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * دیالوگ انتخاب تکلیف برای بارگذاری عکس — وقتی از تب گالری یا دکمه شناور،
 * کاربر می‌خواهد عکس اضافه کند و باید مشخص کند عکس مربوط به کدام تکلیف است.
 */
@Composable
fun HomeworkPickerForPhotosDialog(
    homeworkList: List<Homework>,
    onSelect: (Homework) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "بارگذاری عکس برای کدام تکلیف؟") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                homeworkList.forEach { homework ->
                    val scheme = ClassColors.forIndex(homework.colorIndex)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                onClickLabel = "انتخاب این تکلیف",
                                onClick = { onSelect(homework) }
                            )
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(scheme.accent)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = homework.title,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (homework.imageList.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${homework.imageList.size.toString().toPersianDigits()} عکس",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "انصراف")
            }
        }
    )
}
