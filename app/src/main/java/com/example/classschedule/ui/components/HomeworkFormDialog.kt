package com.example.classschedule.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.Homework
import com.example.classschedule.data.HomeworkImageStore
import com.example.classschedule.ui.theme.ClassColors
import com.example.classschedule.util.JALALI_MONTHS
import com.example.classschedule.util.JalaliDate
import com.example.classschedule.util.formatHourMinute
import com.example.classschedule.util.jalaliMonthLength
import com.example.classschedule.util.jalaliToday
import com.example.classschedule.util.jalaliTodayPlus
import com.example.classschedule.util.toPersianDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * دیالوگ افزودن / ویرایش تکلیف
 * شامل: عنوان تکلیف، درس (با انتخاب سریع از کلاس‌های برنامه)،
 * تاریخ مهلت شمسی (روز / ماه / سال + میان‌بر «امروز» و «فردا»)،
 * ساعت مهلت (اختیاری)، توضیحات (اختیاری)،
 * عکس تکلیف (انتخاب از گالری) و انتخاب رنگ کارت
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeworkFormDialog(
    editingHomework: Homework?,
    subjectSuggestions: List<String>,
    suggestedColorIndex: Int,
    onDismiss: () -> Unit,
    onSave: (Homework) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = remember { jalaliToday() }
    val tomorrow = remember { jalaliTodayPlus(1) }

    var title by remember { mutableStateOf(editingHomework?.title ?: "") }
    var subject by remember { mutableStateOf(editingHomework?.subject ?: "") }
    var dueYear by remember { mutableIntStateOf(editingHomework?.dueYear ?: today.year) }
    var dueMonth by remember { mutableIntStateOf(editingHomework?.dueMonth ?: today.month) }
    var dueDay by remember { mutableIntStateOf(editingHomework?.dueDay ?: today.day) }
    var dueTime by remember { mutableStateOf(editingHomework?.dueTime ?: "") }
    var note by remember { mutableStateOf(editingHomework?.note ?: "") }
    var selectedColor by remember {
        mutableIntStateOf(editingHomework?.colorIndex ?: suggestedColorIndex)
    }
    // عکس‌های تکلیف: نام فایل‌های ذخیره‌شده در حافظه‌ی برنامه
    var photos by remember { mutableStateOf(editingHomework?.imageList ?: emptyList()) }
    // عکس‌های تازه‌افزوده‌شده در همین باز شدن دیالوگ — در صورت انصراف فایل‌شان پاک می‌شود
    val sessionAddedPhotos = remember { mutableListOf<String>() }
    var isLoadingPhotos by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    // انتخابگر گالری: چند عکس با هم انتخاب می‌شود، کوچک و در حافظه‌ی برنامه ذخیره می‌شود
    val pickPhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        scope.launch {
            isLoadingPhotos = true
            val savedNames = uris.mapNotNull { uri ->
                withContext(Dispatchers.IO) {
                    HomeworkImageStore.saveImageFromUri(context, uri)
                }
            }
            sessionAddedPhotos.addAll(savedNames)
            photos = photos + savedNames
            isLoadingPhotos = false
        }
    }

    // حذف یک عکس از فهرست: فایل آن بلافاصله از حافظه پاک می‌شود
    val removePhoto = { name: String ->
        HomeworkImageStore.deleteImage(context, name)
        sessionAddedPhotos.remove(name)
        photos = photos - name
    }

    // انصراف: عکس‌هایی که در همین جلسه اضافه شده‌اند ولی ذخیره نشده‌اند پاک می‌شوند
    val dismissAndCleanup = {
        HomeworkImageStore.deleteImages(context, sessionAddedPhotos.toList())
        onDismiss()
    }

    // تعداد روزهای ماه انتخابی؛ اگر روز بزرگ‌تر از آن باشد به آخرین روز ماه منتقل می‌شود
    val clampDay = {
        val max = jalaliMonthLength(dueYear, dueMonth)
        if (dueDay > max) dueDay = max
    }

    // انتخاب سریع تاریخ با یک لمس
    val setDueDate = { target: JalaliDate ->
        dueYear = target.year
        dueMonth = target.month
        dueDay = target.day
    }
    val isToday = dueYear == today.year && dueMonth == today.month && dueDay == today.day
    val isTomorrow =
        dueYear == tomorrow.year && dueMonth == tomorrow.month && dueDay == tomorrow.day

    // دیالوگ انتخاب ساعت مهلت
    if (showTimePicker) {
        AppTimePickerDialog(
            title = "انتخاب ساعت مهلت",
            initialHour = dueTime.substringBefore(":").toIntOrNull() ?: 8,
            initialMinute = dueTime.substringAfter(":", "0").toIntOrNull() ?: 0,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                dueTime = formatHourMinute(hour, minute)
                showTimePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = dismissAndCleanup,
        title = {
            Text(
                text = if (editingHomework == null) "افزودن تکلیف جدید" else "ویرایش تکلیف"
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // عنوان تکلیف (الزامی)
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = { Text(text = "عنوان تکلیف *") },
                    isError = titleError != null,
                    supportingText = { titleError?.let { Text(text = it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // انتخاب سریع درس از کلاس‌های ثبت‌شده‌ی برنامه
                if (subjectSuggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "درس از کلاس‌های برنامه",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        subjectSuggestions.forEach { name ->
                            FilterChip(
                                selected = subject == name,
                                onClick = { subject = name },
                                label = { Text(text = name) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // نام درس (اختیاری)
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(text = "نام درس (اختیاری)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // تاریخ مهلت شمسی
                Text(text = "تاریخ مهلت", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = isToday,
                        onClick = { setDueDate(today) },
                        label = { Text(text = "امروز") }
                    )
                    FilterChip(
                        selected = isTomorrow,
                        onClick = { setDueDate(tomorrow) },
                        label = { Text(text = "فردا") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateDropdownField(
                        label = "روز",
                        options = (1..jalaliMonthLength(dueYear, dueMonth)).map { it.toString().toPersianDigits() },
                        selectedIndex = dueDay - 1,
                        onSelect = { index -> dueDay = index + 1 },
                        modifier = Modifier.weight(1f)
                    )
                    DateDropdownField(
                        label = "ماه",
                        options = JALALI_MONTHS,
                        selectedIndex = dueMonth - 1,
                        onSelect = { index ->
                            dueMonth = index + 1
                            clampDay()
                        },
                        modifier = Modifier.weight(1.4f)
                    )
                    DateDropdownField(
                        label = "سال",
                        options = (today.year..today.year + 1).map { it.toString().toPersianDigits() },
                        selectedIndex = dueYear - today.year,
                        onSelect = { index ->
                            dueYear = index + today.year
                            clampDay()
                        },
                        modifier = Modifier.weight(1.1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ساعت مهلت (اختیاری)
                DueTimeField(
                    value = dueTime,
                    onOpenPicker = { showTimePicker = true },
                    onClear = { dueTime = "" }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // توضیحات (اختیاری)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(text = "توضیحات (اختیاری)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // عکس تکلیف (اختیاری): انتخاب از گالری — مثلا عکس تکلیف انجام‌شده
                Text(text = "عکس تکلیف (اختیاری)", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "می‌توانید عکس تکلیف انجام‌شده را از گالری اضافه کنید. تکلیف فقط پس از بارگذاری حداقل یک عکس قابل تیک‌زدن است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                HomeworkPhotoEditStrip(
                    photos = photos,
                    accent = ClassColors.forIndex(selectedColor).accent,
                    busy = isLoadingPhotos,
                    onAddClick = { pickPhotosLauncher.launch("image/*") },
                    onRemoveClick = { index -> photos.getOrNull(index)?.let { removePhoto(it) } }
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
                    // اعتبارسنجی عنوان تکلیف
                    val trimmedTitle = title.trim()
                    if (trimmedTitle.isEmpty()) {
                        titleError = "وارد کردن عنوان تکلیف الزامی است"
                        return@Button
                    }
                    onSave(
                        Homework(
                            id = editingHomework?.id ?: 0,
                            title = trimmedTitle,
                            subject = subject.trim(),
                            dueYear = dueYear,
                            dueMonth = dueMonth,
                            dueDay = dueDay,
                            dueTime = dueTime,
                            note = note.trim(),
                            isDone = editingHomework?.isDone ?: false,
                            colorIndex = selectedColor,
                            imageNames = photos.joinToString(";"),
                            createdAt = editingHomework?.createdAt
                                ?: System.currentTimeMillis()
                        )
                    )
                }
            ) {
                Text(text = "ذخیره")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = dismissAndCleanup) {
                Text(text = "انصراف")
            }
        }
    )
}

/**
 * فیلد انتخاب تاریخ شمسی با فهرست بازشو
 */
@Composable
private fun DateDropdownField(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val safeIndex = selectedIndex.coerceIn(0, options.lastIndex)
    Box(modifier = modifier) {
        OutlinedTextField(
            value = options.getOrElse(safeIndex) { "" },
            onValueChange = {},
            readOnly = true,
            label = { Text(text = label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        // لایه شفاف برای گرفتن لمس و باز کردن فهرست
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * فیلد ساعت مهلت اختیاری:
 * خالی = «بدون ساعت» و لمس، انتخابگر ساعت را باز می‌کند
 * پر = ساعت نمایش داده می‌شود و لمس، آن را پاک می‌کند
 */
@Composable
private fun DueTimeField(
    value: String,
    onOpenPicker: () -> Unit,
    onClear: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value.ifBlank { "بدون ساعت" },
            onValueChange = {},
            readOnly = true,
            label = { Text(text = "ساعت مهلت (اختیاری)") },
            trailingIcon = {
                Icon(
                    imageVector = if (value.isNotBlank()) {
                        Icons.Default.Clear
                    } else {
                        Icons.Default.AccessTime
                    },
                    contentDescription = null
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        // لایه شفاف: با ساعت تعیین‌شده لمس = پاک‌کردن، بدون ساعت لمس = باز کردن انتخابگر
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    if (value.isNotBlank()) onClear() else onOpenPicker()
                }
        )
    }
}
