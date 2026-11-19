package com.example.classschedule.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.AppRoles

/**
 * محتوای تب «تنظیمات» در پنل مالک برند MTM Kode:
 * ۱) نام مجموعه: مدرسه‌ای که در نوار بالای همه‌ی پنل‌ها نمایش داده می‌شود
 * ۲) ظاهر برنامه: نمای هفته/تک‌روز + رنگ برنامه (دیالوگ رنگ از MainScreen باز می‌شود)
 * ۳) تعویض پنل: پرش سریع به پنل دانش‌آموز / معلم / مدیر
 * ۴) درباره برنامه: معرفی و نکته‌ی آفلاین بودن
 */
@Composable
fun OwnerSettingsContent(
    isWeekView: Boolean,
    onToggleWeekView: (Boolean) -> Unit,
    onOpenColorSettings: () -> Unit,
    onSelectRole: (Int) -> Unit,
    schoolName: String,
    onSetSchoolName: (String) -> Unit
) {
    // دیالوگ ویرایش نام مجموعه
    var showSchoolNameDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---------- نام مجموعه ----------
        item(key = "school-name-card") {
            SettingsCard(title = "نام مجموعه") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClickLabel = "ویرایش نام مجموعه",
                            onClick = { showSchoolNameDialog = true }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نام مدرسه / مجموعه",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = schoolName.ifBlank { "هنوز ثبت نشده — لمس کنید تا نام را بنویسید" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ---------- ظاهر برنامه ----------
        item(key = "appearance-card") {
            SettingsCard(title = "ظاهر برنامه") {
                // نمای صفحه: کامل هفته یا تک‌روز
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نمایش کامل هفته",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isWeekView) {
                                "همه‌ی روزهای هفته یک‌جا نمایش داده می‌شود"
                            } else {
                                "هر روز جداگانه با تب و سوایپ نمایش داده می‌شود"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isWeekView,
                        onCheckedChange = { onToggleWeekView(it) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalThinDivider()

                // رنگ برنامه
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClickLabel = "تغییر رنگ برنامه",
                            onClick = onOpenColorSettings
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "رنگ برنامه",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "انتخاب تم آماده یا رنگ دلخواه — روی همه‌ی پنل‌ها اثر می‌گذارد",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ---------- تعویض پنل ----------
        item(key = "switch-panel-card") {
            SettingsCard(title = "تعویض پنل") {
                Text(
                    text = "برای ورود به پنل دیگران، یکی از گزینه‌های زیر را لمس کنید:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                RoleSwitchRow(
                    icon = Icons.Default.School,
                    title = roleTitle(AppRoles.ROLE_STUDENT),
                    description = "برنامه، تکالیف، عکس تکالیف و دیدن کارنامه",
                    accent = Color(0xFF2E7D32),
                    container = Color(0xFFE8F5E9),
                    onClick = { onSelectRole(AppRoles.ROLE_STUDENT) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                RoleSwitchRow(
                    icon = Icons.Default.HistoryEdu,
                    title = roleTitle(AppRoles.ROLE_TEACHER),
                    description = "ثبت تکلیف، ثبت نمره و گالری تحویل‌شده‌ها",
                    accent = Color(0xFFE65100),
                    container = Color(0xFFFFF3E0),
                    onClick = { onSelectRole(AppRoles.ROLE_TEACHER) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                RoleSwitchRow(
                    icon = Icons.Default.ManageAccounts,
                    title = roleTitle(AppRoles.ROLE_MANAGER),
                    description = "خلاصه و آمار، مدیریت کلاس‌ها — بدون ثبت نمره",
                    accent = Color(0xFF00695C),
                    container = Color(0xFFE0F2F1),
                    onClick = { onSelectRole(AppRoles.ROLE_MANAGER) }
                )
            }
        }

        // ---------- درباره برنامه ----------
        item(key = "about-card") {
            SettingsCard(title = "درباره برنامه") {
                AboutRow(label = "نام برنامه", value = "کلاس چی")
                AboutRow(label = "نسخه", value = "۱٫۶")
                AboutRow(label = "سازنده و مالک برند", value = "MTM Kode")
                AboutRow(label = "ذخیره‌سازی", value = "آفلاین روی خود گوشی")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "همه‌ی اطلاعات فقط روی همین گوشی ذخیره می‌شود؛ " +
                        "بدون نیاز به اینترنت، بدون هاست و بدون هیچ هزینه‌ای. " +
                        "داده‌های همه‌ی پنل‌ها یکی است و با تعویض پنل چیزی از بین نمی‌رود.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // دیالوگ ویرایش نام مجموعه
    if (showSchoolNameDialog) {
        var draftName by remember { mutableStateOf(schoolName) }
        AlertDialog(
            onDismissRequest = { showSchoolNameDialog = false },
            title = { Text(text = "نام مجموعه") },
            text = {
                Column {
                    Text(
                        text = "این نام در نوار بالای همه‌ی پنل‌ها نمایش داده می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        singleLine = true,
                        placeholder = { Text(text = "مثلاً دبستان شهید مطهری") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Text(
                    text = "ذخیره",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            onSetSchoolName(draftName.trim())
                            showSchoolNameDialog = false
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "انصراف",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable { showSchoolNameDialog = false }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        )
    }
}

/**
 * کارت بخش‌بندی تنظیمات
 */
@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
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
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

/**
 * ردیف تعویض پنل: آیکون رنگی + عنوان + توضیح + فلش
 */
@Composable
private fun RoleSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    accent: Color,
    container: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = container,
        border = BorderStroke(width = 1.dp, color = accent.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = "رفتن به $title",
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    maxLines = 2
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = accent
            )
        }
    }
}

/**
 * یک ردیف ساده‌ی «برچسب : مقدار» در کارت درباره
 */
@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * خط جداکننده‌ی نازک میان ردیف‌های تنظیمات
 */
@Composable
private fun HorizontalThinDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(vertical = 0.dp)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
    )
}
