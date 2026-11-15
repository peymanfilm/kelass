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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.Homework
import com.example.classschedule.ui.components.HomeworkPhotoStrip
import com.example.classschedule.ui.theme.ClassColors
import com.example.classschedule.util.JalaliDate
import com.example.classschedule.util.daysBetween
import com.example.classschedule.util.jalaliToday
import com.example.classschedule.util.toPersianDigits

/**
 * رنگ‌های ثابت تب «نمرات» — مستقل از تم برنامه تا معنای منفی و تحویل‌شده همیشه یکسان بماند
 */
private val NegativeContainer = Color(0xFFFFEBEE)   // پس‌زمینه‌ی کارت منفی (قرمز خیلی روشن)
private val NegativeAccent = Color(0xFFC62828)      // قرمز اصلی منفی
private val NegativeOnContainer = Color(0xFF4A0010) // متن تیره روی کارت منفی
private val NegativeBadgeText = Color(0xFF9A1B1B)   // متن نشان‌های قرمز
private val DeliveredContainer = Color(0xFFE8F5E9)  // پس‌زمینه‌ی کارت تحویل‌شده (سبز روشن)
private val DeliveredAccent = Color(0xFF2E7D32)     // سبز اصلی تحویل‌شده
private val DeliveredOnContainer = Color(0xFF0B290D) // متن تیره روی کارت تحویل‌شده

/**
 * محتوای تب «نمرات» — بدون Scaffold و نوار بالا (که در MainScreen مشترک است)
 *
 * قانون نمرات:
 * - تکلیفی که تاریخ مهلتش گذشته و هنوز هیچ عکسی برایش بارگذاری نشده باشد، یک «منفی» می‌گیرد.
 * - تکلیفی که برایش عکس بارگذاری شده باشد، «تحویل‌شده» حساب می‌شود.
 *
 * بخش‌های صفحه:
 * ۱) دو کارت آماری: تعداد منفی‌ها و تعداد تحویل‌شده‌ها
 * ۲) توضیح یک‌خطی قانون منفی
 * ۳) تفکیک منفی‌ها بر اساس نام درس (چیپ‌های قابل اسکرول افقی)
 * ۴) فهرست منفی‌ها — مرتب از عقب‌مانده‌ترین مهلت؛ هر کارت دکمه‌ی بارگذاری عکس دارد
 * ۵) فهرست تحویل‌شده‌ها — با بندانگشتی عکس‌ها و باز کردن نمایشگر تمام‌صفحه
 */
@Composable
fun GradesContent(
    homeworkList: List<Homework>,
    onAddPhotos: (Homework) -> Unit,
    onOpenPhoto: (Homework, Int) -> Unit
) {
    val today = remember { jalaliToday() }

    // محاسبه‌ی وضعیت نمره‌ی هر تکلیف بر اساس قانون منفی
    val negatives = remember(homeworkList, today) {
        homeworkList
            .filter { homework ->
                homework.imageList.isEmpty() && daysBetween(today, homework.dueJalali) < 0
            }
            // عقب‌مانده‌ترین مهلت‌ها بالای فهرست
            .sortedBy { daysBetween(today, it.dueJalali) }
    }
    val delivered = remember(homeworkList) {
        homeworkList
            .filter { it.imageList.isNotEmpty() }
            // تازه‌ترین مهلت‌ها بالای فهرست
            .sortedByDescending { daysBetween(today, it.dueJalali) }
    }

    // تفکیک منفی‌ها بر اساس نام درس؛ تکلیف بدون درس زیر گروه «بدون درس» می‌رود
    val negativeBySubject = remember(negatives) {
        negatives
            .groupBy { it.subject.trim().ifBlank { "بدون درس" } }
            .map { (subject, list) -> subject to list.size }
            .sortedByDescending { it.second }
    }

    if (homeworkList.isEmpty()) {
        GradesEmptyState()
        return
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
        // ---------- دو کارت آماری خلاصه ----------
        item(key = "summary") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GradeStatCard(
                    count = negatives.size,
                    label = "منفی",
                    container = NegativeContainer,
                    numberColor = NegativeAccent,
                    labelColor = NegativeBadgeText,
                    modifier = Modifier.weight(1f)
                )
                GradeStatCard(
                    count = delivered.size,
                    label = "تحویل‌شده",
                    container = DeliveredContainer,
                    numberColor = DeliveredAccent,
                    labelColor = Color(0xFF1B5E20),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ---------- توضیح قانون منفی ----------
        item(key = "rule") {
            Text(
                text = "تکلیفی که مهلتش گذشته و عکسی برایش بارگذاری نشده باشد، این‌جا یک منفی می‌گیرد.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // ---------- حالت خالی منفی ----------
        if (negatives.isEmpty()) {
            item(key = "no-negative") {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DeliveredContainer
                ) {
                    Text(
                        text = if (delivered.isEmpty()) {
                            "تا امروز هیچ منفی نداری؛ عکس تکالیف انجام‌شده را پیش از گذشتن مهلت بارگذاری کن."
                        } else {
                            "تا امروز هیچ منفی نداری؛ آفرین!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                }
            }
        }

        // ---------- تفکیک منفی‌ها بر اساس درس ----------
        if (negatives.isNotEmpty() && negativeBySubject.isNotEmpty()) {
            item(key = "subject-chips") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(negativeBySubject, key = { it.first }) { (subject, count) ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = NegativeContainer
                        ) {
                            Text(
                                text = "$subject · ${count.toString().toPersianDigits()} منفی",
                                style = MaterialTheme.typography.labelSmall,
                                color = NegativeBadgeText,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // ---------- فهرست منفی‌ها ----------
        if (negatives.isNotEmpty()) {
            item(key = "negatives-header") {
                GradesSectionHeader(
                    title = "منفی‌ها",
                    count = negatives.size,
                    accent = NegativeAccent
                )
            }

            items(negatives, key = { "neg-${it.id}" }) { homework ->
                NegativeGradeCard(
                    homework = homework,
                    today = today,
                    onAddPhotos = { onAddPhotos(homework) }
                )
            }
        }

        // ---------- فهرست تحویل‌شده‌ها ----------
        if (delivered.isNotEmpty()) {
            item(key = "delivered-header") {
                GradesSectionHeader(
                    title = "تحویل‌شده‌ها",
                    count = delivered.size,
                    accent = DeliveredAccent
                )
            }

            items(delivered, key = { "del-${it.id}" }) { homework ->
                DeliveredGradeCard(
                    homework = homework,
                    onOpenPhoto = { index -> onOpenPhoto(homework, index) },
                    onAddPhotos = { onAddPhotos(homework) }
                )
            }
        }
    }
}

/**
 * کارت آماری خلاصه: عدد بزرگ + برچسب (منفی یا تحویل‌شده)
 */
@Composable
private fun GradeStatCard(
    count: Int,
    label: String,
    container: Color,
    numberColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = container
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString().toPersianDigits(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = numberColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = labelColor
            )
        }
    }
}

/**
 * سربرگ بخش‌ها: عنوان + شمارنده
 */
@Composable
private fun GradesSectionHeader(
    title: String,
    count: Int,
    accent: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = accent
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${count.toString().toPersianDigits()} مورد",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * کارت یک منفی: عنوان تکلیف، درس، مهلت گذشته و دکمه‌ی بارگذاری عکس
 * بارگذاری عکس باعث می‌شود تکلیف از فهرست منفی‌ها خارج و تحویل‌شده شود.
 */
@Composable
private fun NegativeGradeCard(
    homework: Homework,
    today: JalaliDate,
    onAddPhotos: () -> Unit
) {
    val overdueDays = -daysBetween(today, homework.dueJalali)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NegativeContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 14.dp
            ),
            verticalAlignment = Alignment.Top
        ) {
            // دایره‌ی قرمز با علامت منفی
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(NegativeAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = homework.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NegativeOnContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // نام درس (در صورت وارد شدن)
                if (homework.subject.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    GradeInfoRow(
                        icon = { tint ->
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = tint
                            )
                        },
                        text = homework.subject,
                        contentColor = NegativeOnContainer
                    )
                }

                // مهلت گذشته + تعداد روز عقب‌ماندگی
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = NegativeAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مهلت: ${homework.dueJalali.toCompactString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NegativeOnContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFFCDD2)
                    ) {
                        Text(
                            text = "${overdueDays.toString().toPersianDigits()} روز گذشته",
                            style = MaterialTheme.typography.labelSmall,
                            color = NegativeBadgeText,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // دکمه‌ی بارگذاری عکس — رفع منفی با بارگذاری عکس تکلیف انجام‌شده
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NegativeAccent,
                    modifier = Modifier.clickable(
                        onClickLabel = "بارگذاری عکس برای رفع منفی",
                        onClick = onAddPhotos
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بارگذاری عکس",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * کارت یک تکلیف تحویل‌شده: عنوان، درس، مهلت، تعداد عکس و بندانگشتی عکس‌ها
 * لمس هر بندانگشتی، نمایشگر تمام‌صفحه‌ی عکس‌ها را باز می‌کند.
 */
@Composable
private fun DeliveredGradeCard(
    homework: Homework,
    onOpenPhoto: (Int) -> Unit,
    onAddPhotos: () -> Unit
) {
    val scheme = ClassColors.forIndex(homework.colorIndex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DeliveredContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 14.dp
            ),
            verticalAlignment = Alignment.Top
        ) {
            // دایره‌ی سبز با تیک
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(DeliveredAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = homework.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeliveredOnContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // نام درس (در صورت وارد شدن)
                if (homework.subject.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    GradeInfoRow(
                        icon = { tint ->
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = tint
                            )
                        },
                        text = homework.subject,
                        contentColor = DeliveredOnContainer
                    )
                }

                // مهلت + تعداد عکس
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = DeliveredAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مهلت: ${homework.dueJalali.toCompactString()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeliveredOnContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFC8E6C9)
                    ) {
                        Text(
                            text = "${homework.imageList.size.toString().toPersianDigits()} عکس",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // بندانگشتی عکس‌های تحویل‌شده + کاشی افزودن عکس بیشتر
                Spacer(modifier = Modifier.height(8.dp))
                HomeworkPhotoStrip(
                    photos = homework.imageList,
                    accent = scheme.accent,
                    onAddClick = onAddPhotos,
                    onPhotoClick = onOpenPhoto
                )
            }
        }
    }
}

/**
 * یک ردیف اطلاعات کوچک با آیکون (مخصوص کارت‌های نمرات)
 */
@Composable
private fun GradeInfoRow(
    icon: @Composable (Color) -> Unit,
    text: String,
    contentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon(contentColor)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}

/**
 * حالت خالی: وقتی هیچ تکلیفی ثبت نشده باشد
 */
@Composable
private fun GradesEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.School,
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
            text = "ابتدا در تب «یادآوری تکالیف» یک تکلیف بسازید؛ تکالیفی که مهلت‌شان بگذرد و عکسی نداشته باشند این‌جا منفی می‌گیرند.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
