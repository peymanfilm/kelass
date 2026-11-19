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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.classschedule.data.AppRoles
import com.example.classschedule.data.AuthRepository

/**
 * محتوای تب «تنظیمات» در پنل مالک برند MTM Kode:
 * ۱) نام مجموعه: مدرسه‌ای که در نوار بالای همه‌ی پنل‌ها نمایش داده می‌شود
 * ۲) امنیت حساب: تغییر نام کاربری و رمز عبور خودِ مالک برند (با تأیید رمز فعلی)
 *    — حساب‌های بقیه نقش‌ها از تب «حساب‌ها» مدیریت می‌شود
 * ۳) ظاهر برنامه: نمای هفته/تک‌روز + رنگ برنامه (دیالوگ رنگ از MainScreen باز می‌شود)
 * ۴) درباره برنامه: معرفی و نکته‌ی آفلاین بودن
 */
@Composable
fun OwnerSettingsContent(
    isWeekView: Boolean,
    onToggleWeekView: (Boolean) -> Unit,
    onOpenColorSettings: () -> Unit,
    schoolName: String,
    onSetSchoolName: (String) -> Unit,
    authRepository: AuthRepository
) {
    // دیالوگ ویرایش نام مجموعه
    var showSchoolNameDialog by remember { mutableStateOf(false) }
    // دیالوگ تغییر رمز عبور حساب مالک برند
    var showPasswordDialog by remember { mutableStateOf(false) }
    // دیالوگ تغییر نام کاربری حساب مالک برند
    var showUsernameDialog by remember { mutableStateOf(false) }

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

        // ---------- امنیت حساب مالک برند ----------
        item(key = "security-card") {
            // با باز/بسته شدن دیالوگ‌ها، نام کاربری تازه خوانده می‌شود
            val currentUsername = authRepository.usernameOf(AppRoles.ROLE_OWNER)
            SettingsCard(title = "امنیت حساب مالک برند") {
                // ردیف نام کاربری فعلی
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نام کاربری شما",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentUsername,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalThinDivider()

                // ردیف تغییر نام کاربری — با تأیید رمز فعلی
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClickLabel = "تغییر نام کاربری",
                            onClick = { showUsernameDialog = true }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تغییر نام کاربری",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "حساب ورود خودتان — با تأیید رمز فعلی",
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

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalThinDivider()

                // ردیف تغییر رمز عبور — با تأیید رمز فعلی
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            onClickLabel = "تغییر رمز عبور",
                            onClick = { showPasswordDialog = true }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Password,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تغییر رمز عبور",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "حساب ورود خودتان — هش‌شده روی خود گوشی",
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

        // ---------- درباره برنامه ----------
        item(key = "about-card") {
            SettingsCard(title = "درباره برنامه") {
                AboutRow(label = "نام برنامه", value = "کلاس چی")
                AboutRow(label = "نسخه", value = "۱٫۸")
                AboutRow(label = "سازنده و مالک برند", value = "MTM Kode")
                AboutRow(label = "ذخیره‌سازی", value = "آفلاین روی خود گوشی")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "همه‌ی اطلاعات فقط روی همین گوشی ذخیره می‌شود؛ " +
                        "بدون نیاز به اینترنت، بدون هاست و بدون هیچ هزینه‌ای. " +
                        "هر حساب پنل نقش خودش را دارد؛ برای پنل دیگر، با آن حساب وارد شوید.",
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

    // دیالوگ تغییر نام کاربری حساب مالک برند — با تأیید رمز فعلی
    if (showUsernameDialog) {
        val context = LocalContext.current
        var currentPassword by remember { mutableStateOf("") }
        var newUsername by remember { mutableStateOf(authRepository.usernameOf(AppRoles.ROLE_OWNER)) }
        var dialogError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showUsernameDialog = false },
            title = { Text(text = "تغییر نام کاربری") },
            text = {
                Column {
                    Text(
                        text = "برای امنیت، اول رمز فعلی را تأیید کنید؛ سپس نام کاربری جدید (حداقل ۳ حرف و بدون فاصله) را بنویسید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            dialogError = null
                        },
                        label = { Text(text = "رمز فعلی") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = {
                            newUsername = it
                            dialogError = null
                        },
                        label = { Text(text = "نام کاربری جدید") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (dialogError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dialogError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Text(
                    text = "ذخیره",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            when (authRepository.changeSelfUsername(currentPassword, newUsername)) {
                                AuthRepository.SelfUsernameResult.SUCCESS -> {
                                    showUsernameDialog = false
                                    Toast.makeText(
                                        context,
                                        "نام کاربری با موفقیت عوض شد",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                AuthRepository.SelfUsernameResult.WRONG_PASSWORD ->
                                    dialogError = "رمز فعلی درست نیست"
                                AuthRepository.SelfUsernameResult.INVALID_USERNAME ->
                                    dialogError = "نام کاربری باید حداقل ۳ حرف و بدون فاصله باشد"
                                AuthRepository.SelfUsernameResult.USERNAME_TAKEN ->
                                    dialogError = "این نام کاربری برای حساب دیگری گرفته شده است"
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "انصراف",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable { showUsernameDialog = false }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        )
    }

    // دیالوگ تغییر رمز عبور دروازه‌ی ورود — با تأیید رمز فعلی
    if (showPasswordDialog) {
        val context = LocalContext.current
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var dialogError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text(text = "تغییر رمز عبور") },
            text = {
                Column {
                    Text(
                        text = "برای امنیت، رمز جدید حداقل ۴ حرف باشد و با رمز فعلی تفاوت داشته باشد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            dialogError = null
                        },
                        label = { Text(text = "رمز فعلی") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            dialogError = null
                        },
                        label = { Text(text = "رمز جدید") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            dialogError = null
                        },
                        label = { Text(text = "تکرار رمز جدید") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (dialogError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = dialogError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Text(
                    text = "ذخیره رمز",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            when {
                                currentPassword.isEmpty() ||
                                    newPassword.isEmpty() ||
                                    confirmPassword.isEmpty() ->
                                    dialogError = "همه‌ی فیلدها را پر کنید"
                                newPassword != confirmPassword ->
                                    dialogError = "تکرار رمز جدید با رمز جدید یکی نیست"
                                else -> {
                                    when (authRepository.changePassword(currentPassword, newPassword)) {
                                        AuthRepository.ChangePasswordResult.SUCCESS -> {
                                            showPasswordDialog = false
                                            Toast.makeText(
                                                context,
                                                "رمز عبور با موفقیت عوض شد",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        AuthRepository.ChangePasswordResult.WRONG_CURRENT ->
                                            dialogError = "رمز فعلی درست نیست"
                                        AuthRepository.ChangePasswordResult.TOO_SHORT ->
                                            dialogError = "رمز جدید باید حداقل ۴ حرف باشد"
                                        AuthRepository.ChangePasswordResult.SAME_AS_CURRENT ->
                                            dialogError = "رمز جدید نباید با رمز فعلی یکی باشد"
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "انصراف",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clickable { showPasswordDialog = false }
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
