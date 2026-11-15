package com.example.classschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.ClassSchedule
import com.example.classschedule.ui.theme.ClassColors
import com.example.classschedule.util.WeekDays
import com.example.classschedule.util.formatHourMinute
import com.example.classschedule.util.toPersianDigits

/**
 * دیالوگ افزودن / ویرایش کلاس
 * شامل: نام درس، انتخاب روز، ساعت شروع و پایان (TimePicker)،
 * نام استاد (اختیاری)، شماره کلاس (اختیاری) و انتخاب رنگ کارت
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClassFormDialog(
    editingClass: ClassSchedule?,
    initialDay: Int,
    suggestedColorIndex: Int,
    onDismiss: () -> Unit,
    onSave: (ClassSchedule) -> Unit
) {
    var name by remember { mutableStateOf(editingClass?.name ?: "") }
    var teacher by remember { mutableStateOf(editingClass?.teacher ?: "") }
    var room by remember { mutableStateOf(editingClass?.room ?: "") }
    var selectedDay by remember { mutableIntStateOf(editingClass?.day ?: initialDay) }
    var startTime by remember { mutableStateOf(editingClass?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(editingClass?.endTime ?: "10:00") }
    var selectedColor by remember {
        mutableIntStateOf(editingClass?.colorIndex ?: suggestedColorIndex)
    }
    var nameError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // دیالوگ انتخاب ساعت شروع
    if (showStartPicker) {
        AppTimePickerDialog(
            title = "انتخاب ساعت شروع",
            initialHour = startTime.substringBefore(":").toIntOrNull() ?: 8,
            initialMinute = startTime.substringAfter(":", "0").toIntOrNull() ?: 0,
            onDismiss = { showStartPicker = false },
            onConfirm = { hour, minute ->
                startTime = formatHourMinute(hour, minute)
                timeError = null
                showStartPicker = false
            }
        )
    }

    // دیالوگ انتخاب ساعت پایان
    if (showEndPicker) {
        AppTimePickerDialog(
            title = "انتخاب ساعت پایان",
            initialHour = endTime.substringBefore(":").toIntOrNull() ?: 10,
            initialMinute = endTime.substringAfter(":", "0").toIntOrNull() ?: 0,
            onDismiss = { showEndPicker = false },
            onConfirm = { hour, minute ->
                endTime = formatHourMinute(hour, minute)
                timeError = null
                showEndPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editingClass == null) "افزودن کلاس جدید" else "ویرایش کلاس"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // نام درس (الزامی)
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text(text = "نام درس *") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(text = it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // انتخاب روز هفته
                Text(text = "روز هفته", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    WeekDays.names.forEachIndexed { index, dayName ->
                        FilterChip(
                            selected = selectedDay == index,
                            onClick = { selectedDay = index },
                            label = { Text(text = dayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ساعت شروع و پایان
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeField(
                        label = "ساعت شروع",
                        value = startTime,
                        onClick = { showStartPicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    TimeField(
                        label = "ساعت پایان",
                        value = endTime,
                        onClick = { showEndPicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                timeError?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // نام استاد (اختیاری)
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text(text = "نام استاد (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // شماره یا نام کلاس (اختیاری)
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text(text = "شماره یا نام کلاس (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // انتخاب رنگ کارت (با نمایش نام هر رنگ)
                Text(text = "رنگ کارت", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(ClassColors.count) { index ->
                        val scheme = ClassColors.forIndex(index)
                        val isSelected = selectedColor == index
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(scheme.accent)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = index },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                            // نام رنگ زیر دایره
                            Text(
                                text = scheme.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // اعتبارسنجی نام درس
                    val trimmedName = name.trim()
                    if (trimmedName.isEmpty()) {
                        nameError = "وارد کردن نام درس الزامی است"
                        return@Button
                    }
                    // اعتبارسنجی ساعت‌ها
                    val startMinutes = toMinutes(startTime)
                    val endMinutes = toMinutes(endTime)
                    if (endMinutes <= startMinutes) {
                        timeError = "ساعت پایان باید بعد از ساعت شروع باشد"
                        return@Button
                    }
                    onSave(
                        ClassSchedule(
                            id = editingClass?.id ?: 0,
                            name = trimmedName,
                            day = selectedDay,
                            startTime = startTime,
                            endTime = endTime,
                            teacher = teacher.trim(),
                            room = room.trim(),
                            colorIndex = selectedColor
                        )
                    )
                }
            ) {
                Text(text = "ذخیره")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "انصراف")
            }
        }
    )
}

/**
 * فیلد نمایش ساعت (فقط خواندنی) که با لمس، TimePicker را باز می‌کند
 */
@Composable
private fun TimeField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value.toPersianDigits(),
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        // لایه شفاف برای گرفتن لمس و باز کردن TimePicker
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick)
        )
    }
}

// ---------- توابع کمکی زمان ----------

private fun parseTime(time: String): Pair<Int, Int> {
    val hour = time.substringBefore(":").toIntOrNull() ?: 0
    val minute = time.substringAfter(":", "0").toIntOrNull() ?: 0
    return hour to minute
}

private fun toMinutes(time: String): Int {
    val (hour, minute) = parseTime(time)
    return hour * 60 + minute
}
