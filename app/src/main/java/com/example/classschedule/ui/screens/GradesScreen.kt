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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.Homework
import com.example.classschedule.data.SubjectGrade
import com.example.classschedule.ui.components.HomeworkPhotoStrip
import com.example.classschedule.ui.theme.ClassColors
import com.example.classschedule.util.JalaliDate
import com.example.classschedule.util.daysBetween
import com.example.classschedule.util.formatGradeLatin
import com.example.classschedule.util.formatGradePersian
import com.example.classschedule.util.jalaliToday
import com.example.classschedule.util.parseGradeInput
import com.example.classschedule.util.toPersianDigits
import kotlin.math.roundToInt

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
private val DeliveredBadgeText = Color(0xFF1B5E20)   // متن سبز نشان‌ها

/**
 * کلید گروه‌بندی تکلیف بر اساس نام درس؛ تکلیف بدون درس زیر گروه «بدون درس» می‌رود
 */
private fun subjectKey(homework: Homework): String =
    homework.subject.trim().ifBlank { SubjectGrade.NO_SUBJECT }

/**
 * نتیجه‌ی محاسبات نمره‌ی یک درس برای نمایش در کارنامه
 */
private data class SubjectCalc(
    val finalContinuous: Float?,   // نمره مستمر نهایی پس از منفی‌ها و مثبت‌ها
    val extraPositives: Int,       // مثبت‌هایی که چون نمره به ۲۰ رسیده بود اعمال نشدند
    val subjectAverage: Float?     // معدل درس: میانگین مستمر نهایی و نمره امتحان
)

/**
 * محاسبه‌ی نمره‌های یک درس طبق قانون کارنامه:
 * ۱) هر منفی (خودکار از تکالیفِ بی‌عکس + دستی معلم) ۰٫۲۵ از نمره مستمر کم می‌کند (کف ۰).
 * ۲) هر مثبت اگر نمره کمتر از ۲۰ باشد ۰٫۲۵ اضافه می‌کند؛
 *    مثبت‌هایی که نمره از قبل ۲۰ شده، کنار نمره شمرده می‌شوند.
 * ۳) معدل درس = میانگین نمره مستمر نهایی و نمره امتحان (اگر امتحان وارد شده باشد).
 */
private fun calcSubject(row: SubjectGrade, autoNegatives: Int): SubjectCalc {
    val continuous = row.continuousGrade ?: return SubjectCalc(null, 0, null)
    var value = continuous - SubjectGrade.PENALTY_PER_NEGATIVE * (autoNegatives + row.manualNegatives)
    value = value.coerceAtLeast(0f)
    var extraPositives = 0
    repeat(row.manualPositives) {
        if (value < SubjectGrade.MAX_GRADE) {
            value = (value + SubjectGrade.PENALTY_PER_NEGATIVE)
                .coerceAtMost(SubjectGrade.MAX_GRADE)
        } else {
            extraPositives++
        }
    }
    value = (value * 100f).roundToInt() / 100f
    val exam = row.examGrade
    val average = when {
        exam != null -> ((value + exam) / 2f * 100f).roundToInt() / 100f
        else -> value
    }
    return SubjectCalc(
        finalContinuous = value,
        extraPositives = extraPositives,
        subjectAverage = average
    )
}

/**
 * محتوای تب «نمرات» — کارنامه‌ی کامل کلاسی، بدون Scaffold و نوار بالا (که در MainScreen مشترک است)
 *
 * قانون نمرات:
 * - برای هر درس، معلم «نمره مستمر (کلاسی)» و «نمره امتحان» از ۰ تا ۲۰ وارد می‌کند.
 * - تکلیفی که مهلتش گذشته و هیچ عکسی برایش بارگذاری نشده، برای همان درس یک «منفی» حساب می‌شود
 *   — اما فقط وقتی معلم برای آن درس «عکس اجباری» را روشن کرده باشد.
 * - معلم می‌تواند برای هر درس به صورت دستی «منفی» یا «مثبت» ثبت کند.
 * - هر منفی ۰٫۲۵ از نمره مستمر همان درس کم می‌کند و هر مثبت اگر نمره کمتر از ۲۰ باشد
 *   ۰٫۲۵ به آن اضافه می‌کند؛ اگر نمره ۲۰ شده باشد، مثبت‌های اضافه کنار نمره نوشته می‌شوند.
 * - معدل هر درس = میانگین نمره مستمر نهایی و نمره امتحان.
 * - معدل کل = میانگین معدل درس‌هایی که نمره دارند.
 *
 * بخش‌های صفحه:
 * ۱) کارت خلاصه‌ی کارنامه: معدل کل، جمع منفی‌ها، جمع مثبت‌ها، تعداد تحویل‌شده‌ها + توضیح قانون
 * ۲) فهرست درس‌ها (کارنامه): نمره مستمر و نمره امتحان قابل ویرایش، منفی/مثبت دستی،
 *    نمره نهایی مستمر، معدل درس و کلید «عکس اجباری»
 * ۳) جزییات منفی‌ها: تکالیفِ مهلت‌گذشته‌ی بدون عکس با دکمه‌ی بارگذاری عکس برای جبران
 * ۴) فهرست تحویل‌شده‌ها: تکالیف دارای عکس با بندانگشتی و نمایشگر تمام‌صفحه
 *
 * readOnly:        فقط مشاهده (پنل دانش‌آموز و مدیر) — بدون ویرایش نمره و درس
 * allowPhotoUpload: بارگذاری عکس جبران (دانش‌آموز و مالک) — برای معلم و مدیر خاموش
 */
@Composable
fun GradesContent(
    subjectGrades: List<SubjectGrade>,
    homeworkList: List<Homework>,
    readOnly: Boolean = false,
    allowPhotoUpload: Boolean = true,
    onSetContinuousGrade: (String, Float?) -> Unit,
    onSetExamGrade: (String, Float?) -> Unit,
    onAdjustManualNegative: (String, Int) -> Unit,
    onAdjustManualPositive: (String, Int) -> Unit,
    onSetPhotoRequired: (String, Boolean) -> Unit,
    onAddSubject: (String) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onAddPhotos: (Homework) -> Unit,
    onOpenPhoto: (Homework, Int) -> Unit
) {
    val today = remember { jalaliToday() }

    // نگاشت نام درس به تنظیمات نمره؛ درسی که ردیف ندارد، عکسش اجباری حساب می‌شود
    val gradeByName = remember(subjectGrades) {
        subjectGrades.associateBy { it.name }
    }

    // همه‌ی تکالیف مهلت‌گذشته‌ی بدون عکس (بدون توجه به اجباری بودن عکس)
    val overdueWithoutPhoto = remember(homeworkList, today) {
        homeworkList
            .filter { homework ->
                homework.imageList.isEmpty() && daysBetween(today, homework.dueJalali) < 0
            }
            // عقب‌مانده‌ترین مهلت‌ها بالای فهرست
            .sortedBy { daysBetween(today, it.dueJalali) }
    }

    // منفی‌های خودکار مؤثر: فقط تکالیفِ درس‌هایی که «عکس اجباری» برایشان روشن است
    val effectiveNegatives = remember(overdueWithoutPhoto, gradeByName) {
        overdueWithoutPhoto.filter { homework ->
            gradeByName[subjectKey(homework)]?.photoRequired ?: true
        }
    }

    // شمارش منفی خودکار هر درس (از تکالیفِ بی‌عکس) برای کسر از نمره مستمر
    val autoNegativesBySubject = remember(effectiveNegatives) {
        effectiveNegatives.groupBy { subjectKey(it) }.mapValues { it.value.size }
    }

    // تکالیف تحویل‌شده: حداقل یک عکس دارند — تازه‌ترین مهلت‌ها بالای فهرست
    val delivered = remember(homeworkList) {
        homeworkList
            .filter { it.imageList.isNotEmpty() }
            .sortedByDescending { daysBetween(today, it.dueJalali) }
    }

    // محاسبات هر درس: نمره مستمر نهایی، مثبت‌های اضافه و معدل درس
    val calcs = remember(gradeByName, autoNegativesBySubject) {
        gradeByName.mapValues { (name, row) ->
            calcSubject(row, autoNegativesBySubject[name] ?: 0)
        }
    }

    // معدل کل: میانگین معدل درس‌هایی که نمره‌شان قابل محاسبه است
    val average = remember(calcs) {
        val averages = calcs.values.mapNotNull { it.subjectAverage }
        if (averages.isEmpty()) null else (averages.sum() / averages.size * 100f).roundToInt() / 100f
    }

    // جمع منفی‌ها (خودکار + دستی معلم) و جمع مثبت‌های دستی معلم
    val totalNegatives = autoNegativesBySubject.values.sum() +
        subjectGrades.sumOf { it.manualNegatives }
    val totalPositives = subjectGrades.sumOf { it.manualPositives }

    // وضعیت دیالوگ‌های کارنامه
    var gradeEditTarget by remember { mutableStateOf<SubjectGrade?>(null) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var deleteSubjectTarget by remember { mutableStateOf<SubjectGrade?>(null) }

    // کارنامه کاملا خالی: نه درسی هست و نه تکلیفی
    if (subjectGrades.isEmpty() && homeworkList.isEmpty()) {
        GradesEmptyState(
            readOnly = readOnly,
            onAddSubject = { showAddSubjectDialog = true }
        )
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
        // ---------- یادآوری حالت فقط-مشاهده ----------
        if (readOnly) {
            item(key = "readonly-note") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "کارنامه فقط برای مشاهده است؛ نمره‌ها را معلم ثبت می‌کند.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // ---------- کارت خلاصه‌ی کارنامه ----------
        item(key = "report-header") {
            ReportHeaderCard(
                average = average,
                totalNegatives = totalNegatives,
                totalPositives = totalPositives,
                deliveredCount = delivered.size
            )
        }

        // ---------- سربرگ فهرست درس‌ها + دکمه افزودن ----------
        item(key = "subjects-header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "درس‌ها",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${subjectGrades.size.toString().toPersianDigits()} درس",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                if (!readOnly) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.clickable(
                            onClickLabel = "افزودن درس به کارنامه",
                            onClick = { showAddSubjectDialog = true }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "افزودن درس",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // ---------- ردیف‌های کارنامه ----------
        items(subjectGrades, key = { "subject-${it.name}" }) { row ->
            SubjectReportCard(
                row = row,
                autoNegatives = autoNegativesBySubject[row.name] ?: 0,
                readOnly = readOnly,
                onEditGrades = { gradeEditTarget = row },
                onAdjustNegative = { delta -> onAdjustManualNegative(row.name, delta) },
                onAdjustPositive = { delta -> onAdjustManualPositive(row.name, delta) },
                onTogglePhotoRequired = { onSetPhotoRequired(row.name, !row.photoRequired) },
                onDelete = { deleteSubjectTarget = row }
            )
        }

        // ---------- جزییات منفی‌ها ----------
        if (effectiveNegatives.isNotEmpty()) {
            item(key = "negatives-header") {
                GradesSectionHeader(
                    title = "جزییات منفی‌ها",
                    count = effectiveNegatives.size,
                    accent = NegativeAccent
                )
            }
            items(effectiveNegatives, key = { "neg-${it.id}" }) { homework ->
                NegativeGradeCard(
                    homework = homework,
                    today = today,
                    allowPhotoUpload = allowPhotoUpload,
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
                    allowPhotoUpload = allowPhotoUpload,
                    onOpenPhoto = { index -> onOpenPhoto(homework, index) },
                    onAddPhotos = { onAddPhotos(homework) }
                )
            }
        }
    }

    // ---------- دیالوگ‌های کارنامه ----------

    // دیالوگ وارد کردن / ویرایش نمره مستمر و نمره امتحان
    gradeEditTarget?.let { target ->
        GradeEditDialog(
            target = target,
            onDismiss = { gradeEditTarget = null },
            onSave = { continuous, exam ->
                onSetContinuousGrade(target.name, continuous)
                onSetExamGrade(target.name, exam)
                gradeEditTarget = null
            }
        )
    }

    // دیالوگ افزودن درس جدید به کارنامه
    if (showAddSubjectDialog) {
        AddSubjectDialog(
            existingNames = gradeByName.keys,
            onDismiss = { showAddSubjectDialog = false },
            onConfirm = { name ->
                onAddSubject(name)
                showAddSubjectDialog = false
            }
        )
    }

    // دیالوگ تأیید حذف درس از کارنامه
    deleteSubjectTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteSubjectTarget = null },
            title = { Text(text = "حذف درس از کارنامه") },
            text = {
                Text(
                    text = "آیا از حذف «${target.name}» از کارنامه مطمئن هستید؟ " +
                        "نمره و تنظیمات آن پاک می‌شود؛ تکالیفش حذف نمی‌شوند و اگر این درس در " +
                        "برنامه هفتگی یا تکالیف باشد، دوباره به کارنامه اضافه می‌شود."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSubject(target.name)
                        deleteSubjectTarget = null
                    }
                ) {
                    Text(text = "حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteSubjectTarget = null }) {
                    Text(text = "انصراف")
                }
            }
        )
    }
}

/**
 * کارت خلاصه‌ی کارنامه: معدل کل + جمع منفی‌ها + جمع مثبت‌ها + تعداد تحویل‌شده‌ها + توضیح قانون نمره
 */
@Composable
private fun ReportHeaderCard(
    average: Float?,
    totalNegatives: Int,
    totalPositives: Int,
    deliveredCount: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "کارنامه کلاسی",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ReportStat(
                    value = average?.let { formatGradePersian(it) } ?: "—",
                    label = "معدل کل",
                    valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                ReportStat(
                    value = totalNegatives.toString().toPersianDigits(),
                    label = "منفی‌ها",
                    valueColor = if (totalNegatives > 0) NegativeAccent else DeliveredAccent,
                    modifier = Modifier.weight(1f)
                )
                ReportStat(
                    value = totalPositives.toString().toPersianDigits(),
                    label = "مثبت‌ها",
                    valueColor = if (totalPositives > 0) DeliveredAccent else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                ReportStat(
                    value = deliveredCount.toString().toPersianDigits(),
                    label = "تحویل‌شده",
                    valueColor = DeliveredAccent,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "هر منفی ۰٫۲۵ از نمره مستمر همان درس کم می‌کند و هر مثبت ۰٫۲۵ اضافه می‌کند " +
                    "(تا سقف ۲۰)؛ معدل هر درس از میانگین نمره مستمر و نمره امتحان ساخته می‌شود.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * یک ستون آماری در کارت خلاصه: عدد بزرگ + برچسب کوچک
 */
@Composable
private fun ReportStat(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
        )
    }
}

/**
 * کارت کارنامه یک درس:
 * نام درس + دکمه حذف | نمره مستمر و نمره امتحان (قابل ویرایش) |
 * منفی و مثبت با دکمه‌های + و − | نمره نهایی مستمر (با مثبت‌های اضافه) | معدل درس | کلید «عکس اجباری»
 * در حالت فقط-مشاهده دکمه‌های ویرایش، حذف و کلیدها نشان داده نمی‌شوند و فقط مقادیر دیده می‌شوند
 */
@Composable
private fun SubjectReportCard(
    row: SubjectGrade,
    autoNegatives: Int,
    readOnly: Boolean = false,
    onEditGrades: () -> Unit,
    onAdjustNegative: (Int) -> Unit,
    onAdjustPositive: (Int) -> Unit,
    onTogglePhotoRequired: () -> Unit,
    onDelete: () -> Unit
) {
    val calc = calcSubject(row, autoNegatives)
    val totalNegatives = autoNegatives + row.manualNegatives
    val hasAdjustment = totalNegatives > 0 || row.manualPositives > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // نام درس + دکمه حذف درس (فقط در حالت قابل ویرایش)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!readOnly) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف درس «${row.name}» از کارنامه",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // دو ستون: نمره مستمر | نمره امتحان — هر دو با دکمه ویرایش
            Row(verticalAlignment = Alignment.Top) {
                // نمره مستمر + دکمه ویرایش
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "نمره مستمر",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = row.continuousGrade?.let { formatGradePersian(it) } ?: "وارد نشده",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (row.continuousGrade == null) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (!readOnly) {
                            Spacer(modifier = Modifier.width(2.dp))
                            IconButton(
                                onClick = onEditGrades,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "ویرایش نمره‌های «${row.name}»",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // نمره امتحان + دکمه ویرایش
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "نمره امتحان",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = row.examGrade?.let { formatGradePersian(it) } ?: "وارد نشده",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (row.examGrade == null) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (!readOnly) {
                            Spacer(modifier = Modifier.width(2.dp))
                            IconButton(
                                onClick = onEditGrades,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "ویرایش نمره امتحان «${row.name}»",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // منفی و مثبت: در حالت قابل ویرایش شمارنده با دکمه + و − (ثبت دستی معلم)
            // در حالت فقط-مشاهده فقط مقدارها بدون دکمه نشان داده می‌شود
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    if (readOnly) {
                        ReadOnlyCounter(
                            label = "منفی",
                            count = totalNegatives,
                            countColor = if (totalNegatives > 0) {
                                NegativeAccent
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            subLabel = if (autoNegatives > 0) {
                                "خودکار از تکالیف: ${autoNegatives.toString().toPersianDigits()}"
                            } else {
                                "هر منفی: −۰٫۲۵"
                            }
                        )
                    } else {
                        CounterStepper(
                            label = "منفی",
                            count = totalNegatives,
                            countColor = if (totalNegatives > 0) {
                                NegativeAccent
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            subLabel = if (autoNegatives > 0) {
                                "خودکار از تکالیف: ${autoNegatives.toString().toPersianDigits()}"
                            } else {
                                "هر منفی: −۰٫۲۵"
                            },
                            onDecrement = { onAdjustNegative(-1) },
                            onIncrement = { onAdjustNegative(1) }
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (readOnly) {
                        ReadOnlyCounter(
                            label = "مثبت",
                            count = row.manualPositives,
                            countColor = if (row.manualPositives > 0) {
                                DeliveredAccent
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            subLabel = "هر مثبت: +۰٫۲۵"
                        )
                    } else {
                        CounterStepper(
                            label = "مثبت",
                            count = row.manualPositives,
                            countColor = if (row.manualPositives > 0) {
                                DeliveredAccent
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            subLabel = "هر مثبت: +۰٫۲۵",
                            onDecrement = { onAdjustPositive(-1) },
                            onIncrement = { onAdjustPositive(1) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // نمره نهایی مستمر (با نشان مثبت‌های اضافه) + معدل درس
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "نمره نهایی",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = calc.finalContinuous?.let { formatGradePersian(it) } ?: "—",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (calc.finalContinuous == null) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        // اگر نمره ۲۰ شده و مثبت اضافه مانده، کنار نمره نوشته می‌شود
                        if (calc.finalContinuous != null && calc.extraPositives > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFC8E6C9)
                            ) {
                                Text(
                                    text = "+${calc.extraPositives.toString().toPersianDigits()} مثبت",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DeliveredBadgeText,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "معدل درس",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = calc.subjectAverage?.let { formatGradePersian(it) } ?: "—",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // کلید «عکس اجباری» — معلم تعیین می‌کند بی‌عکسی در این درس منفی بگیرد یا نه
            // در حالت فقط-مشاهده فقط وضعیت آن نشان داده می‌شود
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "عکس اجباری است",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (row.photoRequired) {
                            "تکلیفِ مهلت‌گذشته بدون عکس منفی می‌گیرد"
                        } else {
                            "بی‌عکسی روی نمره این درس اثر ندارد"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!readOnly) {
                    Switch(
                        checked = row.photoRequired,
                        onCheckedChange = { onTogglePhotoRequired() }
                    )
                } else {
                    Text(
                        text = if (row.photoRequired) "روشن" else "خاموش",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (row.photoRequired) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * شمارنده‌ی فقط-مشاهده‌ی منفی/مثبت — بدون دکمه (برای پنل دانش‌آموز و مدیر)
 */
@Composable
private fun ReadOnlyCounter(
    label: String,
    count: Int,
    countColor: Color,
    subLabel: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = count.toString().toPersianDigits(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = countColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

/**
 * شمارنده‌ی منفی/مثبت با دکمه‌های کم و زیاد — برای ثبت دستی معلم
 */
@Composable
private fun CounterStepper(
    label: String,
    count: Int,
    countColor: Color,
    subLabel: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // دکمه کم کردن
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .clickable(
                        onClickLabel = "کم کردن $label",
                        onClick = onDecrement
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = count.toString().toPersianDigits(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = countColor
            )
            Spacer(modifier = Modifier.width(10.dp))
            // دکمه زیاد کردن
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .clickable(
                        onClickLabel = "افزودن $label",
                        onClick = onIncrement
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

/**
 * دیالوگ وارد کردن / ویرایش نمره مستمر و نمره امتحان — ارقام فارسی و لاتین پذیرفته می‌شود
 * ذخیره با فیلد خالی یعنی پاک کردن آن نمره.
 */
@Composable
private fun GradeEditDialog(
    target: SubjectGrade,
    onDismiss: () -> Unit,
    onSave: (Float?, Float?) -> Unit
) {
    var continuousText by remember(target) {
        mutableStateOf(target.continuousGrade?.let { formatGradeLatin(it) } ?: "")
    }
    var examText by remember(target) {
        mutableStateOf(target.examGrade?.let { formatGradeLatin(it) } ?: "")
    }
    val continuousParsed = parseGradeInput(continuousText)
    val examParsed = parseGradeInput(examText)
    val continuousError = continuousText.isNotBlank() && continuousParsed == null
    val examError = examText.isNotBlank() && examParsed == null
    val isError = continuousError || examError

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "نمره‌های «${target.name}»") },
        text = {
            Column {
                OutlinedTextField(
                    value = continuousText,
                    onValueChange = { continuousText = it },
                    label = { Text(text = "نمره مستمر از ۰ تا ۲۰") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = continuousError,
                    supportingText = {
                        if (continuousError) {
                            Text(text = "عددی بین ۰ تا ۲۰ وارد کنید")
                        }
                    }
                )
                TextButton(onClick = { continuousText = "" }) {
                    Text(text = "پاک کردن نمره مستمر")
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = examText,
                    onValueChange = { examText = it },
                    label = { Text(text = "نمره امتحان از ۰ تا ۲۰") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = examError,
                    supportingText = {
                        if (examError) {
                            Text(text = "عددی بین ۰ تا ۲۰ وارد کنید")
                        }
                    }
                )
                TextButton(onClick = { examText = "" }) {
                    Text(text = "پاک کردن نمره امتحان")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isError,
                onClick = { onSave(continuousParsed, examParsed) }
            ) {
                Text(text = "ذخیره")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "انصراف")
            }
        }
    )
}

/**
 * دیالوگ افزودن دستی یک درس جدید به کارنامه
 */
@Composable
private fun AddSubjectDialog(
    existingNames: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val trimmed = name.trim()
    val duplicate = trimmed.isNotEmpty() && trimmed in existingNames

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "افزودن درس به کارنامه") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "نام درس") },
                    singleLine = true,
                    isError = duplicate,
                    supportingText = {
                        if (duplicate) {
                            Text(text = "این درس از قبل در کارنامه هست")
                        }
                    }
                )
                Text(
                    text = "درس‌های برنامه هفتگی و تکالیف به‌طور خودکار به کارنامه اضافه می‌شوند؛ این‌جا برای درس‌های دیگر است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = trimmed.isNotEmpty() && !duplicate,
                onClick = { onConfirm(trimmed) }
            ) {
                Text(text = "افزودن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "انصراف")
            }
        }
    )
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
 * در حالت بدون اجازه‌ی بارگذاری عکس (معلم/مدیر)، دکمه‌ی بارگذاری نشان داده نمی‌شود.
 */
@Composable
private fun NegativeGradeCard(
    homework: Homework,
    today: JalaliDate,
    allowPhotoUpload: Boolean = true,
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
                // فقط برای نقش‌هایی که اجازه‌ی بارگذاری عکس دارند (دانش‌آموز و مالک)
                if (allowPhotoUpload) {
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
}

/**
 * کارت یک تکلیف تحویل‌شده: عنوان، درس، مهلت، تعداد عکس و بندانگشتی عکس‌ها
 * لمس هر بندانگشتی، نمایشگر تمام‌صفحه‌ی عکس‌ها را باز می‌کند.
 */
@Composable
private fun DeliveredGradeCard(
    homework: Homework,
    allowPhotoUpload: Boolean = true,
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
                            color = DeliveredBadgeText,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // بندانگشتی عکس‌های تحویل‌شده + کاشی افزودن عکس بیشتر (فقط با اجازه)
                Spacer(modifier = Modifier.height(8.dp))
                HomeworkPhotoStrip(
                    photos = homework.imageList,
                    accent = scheme.accent,
                    showAddTile = allowPhotoUpload,
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
 * حالت خالی: وقتی نه درسی در کارنامه هست و نه تکلیفی ثبت شده است
 * در حالت فقط-مشاهده، دکمه افزودن درس نشان داده نمی‌شود
 */
@Composable
private fun GradesEmptyState(
    readOnly: Boolean = false,
    onAddSubject: () -> Unit
) {
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
            text = "کارنامه هنوز خالی است",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "درس‌های برنامه هفتگی و تکالیف خودکار به کارنامه اضافه می‌شوند؛ " +
                "برای هر درس نمره مستمر و نمره امتحان را وارد کنید. " +
                "هر تکلیفِ مهلت‌گذشته‌ی بدون عکس ۰٫۲۵ کم می‌کند و هر مثبت ۰٫۲۵ اضافه می‌کند.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (!readOnly) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddSubject,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "افزودن درس")
            }
        }
    }
}
