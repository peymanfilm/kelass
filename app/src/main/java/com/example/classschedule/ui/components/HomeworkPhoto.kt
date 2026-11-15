package com.example.classschedule.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.classschedule.data.HomeworkImageStore
import com.example.classschedule.util.toPersianDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** اندازه‌ی بندانگشتی عکس‌ها در کارت‌ها و گالری */
private val THUMB_SIZE = 56.dp

/**
 * خواندن بیت‌مپ یک عکس ذخیره‌شده به‌صورت غیرهم‌زمان (روی رشته‌ی پس‌زمینه)
 * تا آماده شدن، مقدار null است و جای‌نگهدار خاکستری نشان داده می‌شود
 */
@Composable
fun rememberHomeworkBitmap(name: String?, maxDim: Int): Bitmap? {
    val context = LocalContext.current
    return if (name.isNullOrBlank()) {
        null
    } else {
        produceState<Bitmap?>(initialValue = null, key1 = name, key2 = maxDim) {
            value = withContext(Dispatchers.IO) {
                HomeworkImageStore.loadBitmap(context, name, maxDim)
            }
        }.value
    }
}

/**
 * بندانگشتی یک عکس تکلیف — لمس برای نمایش تمام‌صفحه
 */
@Composable
fun HomeworkThumbnail(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = rememberHomeworkBitmap(name = name, maxDim = 256)
    Box(
        modifier = modifier
            .size(THUMB_SIZE)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick, onClickLabel = "نمایش عکس تکلیف"),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "عکس تکلیف",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // جای‌نگهدار تا آماده شدن عکس
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxSize()
            ) {}
        }
    }
}

/**
 * کاشی دکمه‌ی «افزودن عکس» — در کارت تکلیف، فرم و گالری استفاده می‌شود
 * در حالت busy (در حال ذخیره‌ی عکس‌ها) چرخاننده نشان داده می‌شود
 */
@Composable
fun HomeworkAddPhotoTile(
    onClick: () -> Unit,
    accent: Color,
    busy: Boolean = false,
    size: androidx.compose.ui.unit.Dp = THUMB_SIZE
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = accent.copy(alpha = 0.55f), shape = RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.12f))
            .clickable(onClick = onClick, onClickLabel = "افزودن عکس تکلیف"),
        contentAlignment = Alignment.Center
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = accent
            )
        } else {
            Icon(
                imageVector = Icons.Default.AddAPhoto,
                contentDescription = "افزودن عکس تکلیف",
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * نوار عکس‌های یک تکلیف در کارت: بندانگشتی‌ها + کاشی افزودن عکس
 */
@Composable
fun HomeworkPhotoStrip(
    photos: List<String>,
    accent: Color,
    busy: Boolean = false,
    onAddClick: () -> Unit,
    onPhotoClick: (Int) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(photos, key = { _, name -> name }) { index, name ->
            HomeworkThumbnail(name = name, onClick = { onPhotoClick(index) })
        }
        item(key = "add") {
            HomeworkAddPhotoTile(onClick = onAddClick, accent = accent, busy = busy)
        }
    }
}

/**
 * نوار ویرایش عکس‌ها در فرم تکلیف: بندانگشتی‌ها با دکمه‌ی حذف + کاشی افزودن
 */
@Composable
fun HomeworkPhotoEditStrip(
    photos: List<String>,
    accent: Color,
    busy: Boolean = false,
    onAddClick: () -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(photos, key = { _, name -> name }) { index, name ->
            Box(contentAlignment = Alignment.TopStart) {
                HomeworkThumbnail(name = name, onClick = {})
                // دکمه‌ی حذف کوچک در گوشه‌ی بالا
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp, start = 2.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFC62828))
                        .clickable(
                            onClickLabel = "حذف عکس",
                            onClick = { onRemoveClick(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "حذف این عکس",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
        item(key = "add") {
            HomeworkAddPhotoTile(onClick = onAddClick, accent = accent, busy = busy)
        }
    }
}

/**
 * نمایشگر تمام‌صفحه‌ی عکس‌های یک تکلیف
 * - کشیدن انگشت برای جابه‌جایی میان عکس‌ها (HorizontalPager)
 * - شماره‌ی عکس فعلی در بالا (مثل «۲ از ۳»)
 * - دکمه‌ی حذف عکس فعلی و دکمه‌ی بستن
 * اگر همه‌ی عکس‌ها حذف شوند، نمایشگر به‌طور خودکار بسته می‌شود
 */
@Composable
fun HomeworkPhotoViewerDialog(
    photos: List<String>,
    initialIndex: Int,
    onDeletePhoto: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // فهرست خالی → بستن نمایشگر
    LaunchedEffect(photos.size) {
        if (photos.isEmpty()) onDismiss()
    }
    if (photos.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, photos.lastIndex)
    ) { photos.size }

    // اگر عکسی حذف شود و شماره‌ی فعلی از فهرست بیرون بزند، به آخرین عکس برمی‌گردیم
    LaunchedEffect(photos.size) {
        if (pagerState.currentPage > photos.lastIndex) {
            pagerState.scrollToPage(photos.lastIndex)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // عکس‌ها با کشیدن انگشت
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val name = photos.getOrNull(page) ?: return@HorizontalPager
                val bitmap = rememberHomeworkBitmap(name = name, maxDim = 2048)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "عکس تکلیف شماره ${(page + 1).toString().toPersianDigits()}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }

            // نوار بالا: دکمه بستن + شماره عکس فعلی
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن نمایشگر عکس",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${(pagerState.currentPage + 1).toString().toPersianDigits()} از " +
                        photos.size.toString().toPersianDigits(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // نوار پایین: حذف عکس فعلی
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(
                                onClickLabel = "حذف این عکس",
                                onClick = { onDeletePhoto(pagerState.currentPage) }
                            )
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حذف این عکس",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * حالت خالی تب «انجام تکالیف» — وقتی هنوز تکلیفی ثبت نشده باشد
 */
@Composable
fun HomeworkPhotosEmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AddAPhoto,
            contentDescription = null,
            modifier = Modifier.size(88.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}
