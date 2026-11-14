package com.example.classschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.SettingsRepository
import com.example.classschedule.ui.theme.ColorThemes

/**
 * دیالوگ شخصی‌سازی رنگ برنامه
 * شامل ۲۴ رنگ آماده با نام فارسی + گزینه «رنگ دلخواه» برای انتخاب دستی از طیف رنگی
 * رنگ انتخابی بلافاصله روی کل برنامه اعمال و ذخیره می‌شود.
 */
@Composable
fun ColorSettingsDialog(
    selectedIndex: Int,
    customColorArgb: Int?,
    onSelectTheme: (Int) -> Unit,
    onSelectCustomColor: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    // نام رنگ فعال برای نمایش بالای دیالوگ
    val currentColorName = if (selectedIndex == SettingsRepository.CUSTOM_THEME_INDEX) {
        "رنگ دلخواه"
    } else {
        ColorThemes.forIndex(selectedIndex).name
    }

    // باز بودن دیالوگ انتخاب رنگ دستی
    var showCustomPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "شخصی‌سازی رنگ برنامه")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "رنگ فعلی: $currentColorName",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "رنگ نوار بالا، دکمه افزودن و رنگ‌های تأکیدی برنامه است. انتخاب شما ذخیره می‌شود.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Right
                )

                // شبکه نمونه رنگ‌ها: ۲۴ رنگ در ۶ ردیف ۴تایی (قابل اسکرول)
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    ColorThemes.list.chunked(4).forEach { rowThemes ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            rowThemes.forEach { theme ->
                                val index = ColorThemes.list.indexOf(theme)
                                val selected = index == selectedIndex
                                ThemeSwatch(
                                    color = theme.accent,
                                    name = theme.name,
                                    selected = selected,
                                    onClick = { onSelectTheme(index) }
                                )
                            }
                        }
                    }

                    // گزینه رنگ دلخواه (انتخاب دستی)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ThemeSwatch(
                            color = customColorArgb?.let { Color(it) },
                            name = "رنگ دلخواه",
                            selected = selectedIndex == SettingsRepository.CUSTOM_THEME_INDEX,
                            rainbow = customColorArgb == null,
                            onClick = { showCustomPicker = true }
                        )
                        // برای هم‌تراز شدن با ردیف‌های ۴تایی
                        Box(modifier = Modifier.size(64.dp))
                        Box(modifier = Modifier.size(64.dp))
                        Box(modifier = Modifier.size(64.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "تأیید")
            }
        }
    )

    // دیالوگ انتخاب دستی رنگ از طیف رنگی
    if (showCustomPicker) {
        val initialCustom = customColorArgb?.let { Color(it) }
            ?: ColorThemes.forIndex(selectedIndex.coerceIn(0, ColorThemes.list.lastIndex)).accent
        ColorPickerDialog(
            initialColor = initialCustom,
            onConfirm = { color ->
                onSelectCustomColor(color)
                showCustomPicker = false
            },
            onDismiss = { showCustomPicker = false }
        )
    }
}

/**
 * نمونه رنگ در دیالوگ انتخاب تم: دایره رنگی + نام رنگ زیر آن
 * اگر rainbow=true باشد، دایره به شکل رنگین‌کمان (برای «رنگ دلخواه») نمایش داده می‌شود.
 */
@Composable
private fun ThemeSwatch(
    color: Color?,
    name: String,
    selected: Boolean,
    rainbow: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    brush = if (rainbow) {
                        Brush.sweepGradient(
                            listOf(
                                Color(0xFFF44336),
                                Color(0xFFFFEB3B),
                                Color(0xFF4CAF50),
                                Color(0xFF00BCD4),
                                Color(0xFF3F51B5),
                                Color(0xFF9C27B0),
                                Color(0xFFF44336)
                            )
                        )
                    } else {
                        Brush.linearGradient(listOf(color ?: Color.Transparent, color ?: Color.Transparent))
                    }
                )
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = if (rainbow) Icons.Default.Edit else Icons.Default.Check,
                    contentDescription = "انتخاب شده",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
