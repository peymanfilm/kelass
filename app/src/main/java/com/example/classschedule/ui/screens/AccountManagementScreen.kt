package com.example.classschedule.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.AppRoles
import com.example.classschedule.data.AuthRepository

/**
 * محتوای تب «حساب‌ها» — مدیریت نام کاربری و رمز عبور حساب‌های برنامه:
 *
 * - مدیر: می‌تواند حساب دانش‌آموزان و معلمان را تغییر دهد
 * - مالک برند MTM Kode: علاوه بر دانش‌آموزان و معلمان، حساب مدیر را هم تغییر می‌دهد
 *   (حساب خودِ مالک برند از تب تنظیمات و با تأیید رمز فعلی تغییر می‌کند)
 *
 * رمزها هرگز به صورت خام ذخیره نمی‌شوند؛ فقط هش SHA-256 روی خود گوشی نگه داشته می‌شود.
 */
@Composable
fun AccountsContent(
    callerRole: Int,
    authRepository: AuthRepository
) {
    val context = LocalContext.current
    // دیالوگ ویرایش هر نقش — نقش در حال ویرایش؛ null یعنی دیالوگی باز نیست
    var editingRole by remember { mutableStateOf<Int?>(null) }

    val editableRoles = remember(callerRole) {
        AuthRepository.ACCOUNT_ROLES.filter { authRepository.canEditAccount(callerRole, it) }
    }

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
        // ---------- راهنمای دسترسی ----------
        item(key = "accounts-guide") {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (callerRole == AppRoles.ROLE_OWNER) {
                            "شما مالک برند MTM Kode هستید؛ می‌توانید نام کاربری و رمز عبور دانش‌آموزان، معلمان و مدیر را عوض کنید."
                        } else {
                            "شما مدیر هستید؛ می‌توانید نام کاربری و رمز عبور دانش‌آموزان و معلمان را عوض کنید."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "پس از تغییر هر حساب، ورود با نام کاربری و رمز جدید انجام می‌شود. رمزها فقط هش‌شده روی همین گوشی ذخیره می‌شوند.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // ---------- یک کارت برای هر نقشِ قابل‌ویرایش ----------
        items(
            count = editableRoles.size,
            key = { index -> "account-role-${editableRoles[index]}" }
        ) { index ->
            val role = editableRoles[index]
            AccountRoleCard(
                role = role,
                username = authRepository.usernameOf(role),
                onEdit = { editingRole = role }
            )
        }
    }

    // ---------- دیالوگ ویرایش حساب ----------
    editingRole?.let { targetRole ->
        EditAccountDialog(
            targetRole = targetRole,
            currentUsername = authRepository.usernameOf(targetRole),
            onDismiss = { editingRole = null },
            onSave = { newUsername, newPassword ->
                val result = authRepository.updateAccount(
                    callerRole = callerRole,
                    targetRole = targetRole,
                    newUsername = newUsername,
                    newPassword = newPassword
                )
                editingRole = null
                val message = when (result) {
                    AuthRepository.UpdateAccountResult.SUCCESS ->
                        "حساب «${roleName(targetRole)}» به‌روزرسانی شد"
                    AuthRepository.UpdateAccountResult.NO_PERMISSION ->
                        "اجازه‌ی تغییر این حساب را ندارید"
                    AuthRepository.UpdateAccountResult.INVALID_USERNAME ->
                        "نام کاربری باید حداقل ۳ حرف و بدون فاصله باشد"
                    AuthRepository.UpdateAccountResult.USERNAME_TAKEN ->
                        "این نام کاربری برای حساب دیگری گرفته شده است"
                    AuthRepository.UpdateAccountResult.PASSWORD_TOO_SHORT ->
                        "رمز عبور باید حداقل ۴ حرف باشد"
                    AuthRepository.UpdateAccountResult.NOTHING_CHANGED ->
                        "تغییری برای ذخیره نبود"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * کارت یک حساب: آیکون رنگی نقش + نام نقش + نام کاربری فعلی + دکمه تغییر
 */
@Composable
private fun AccountRoleCard(
    role: Int,
    username: String,
    onEdit: () -> Unit
) {
    val accent = roleAccent(role)
    val container = roleContainer(role)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = roleIcon(role),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "حساب ${roleName(role)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "نام کاربری فعلی: $username",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // دکمه تغییر نام کاربری و رمز
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClickLabel = "تغییر حساب ${roleName(role)}",
                        onClick = onEdit
                    )
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تغییر نام کاربری و رمز عبور",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null,
                    tint = accent
                )
            }
        }
    }
}

/**
 * دیالوگ تغییر نام کاربری و رمز عبور یک حساب:
 * - نام کاربری با مقدار فعلی پر می‌شود
 * - رمز خالی یعنی رمز فعلی بدون تغییر می‌ماند
 * - رمز جدید باید دو بار یکسان وارد شود
 */
@Composable
private fun EditAccountDialog(
    targetRole: Int,
    currentUsername: String,
    onDismiss: () -> Unit,
    onSave: (newUsername: String, newPassword: String) -> Unit
) {
    var username by remember { mutableStateOf(currentUsername) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var dialogError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "حساب ${roleName(targetRole)}") },
        text = {
            Column {
                Text(
                    text = "نام کاربری و رمز عبور جدید این حساب را بنویسید. " +
                        "اگر رمز را خالی بگذارید، رمز فعلی بدون تغییر می‌ماند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        dialogError = null
                    },
                    label = { Text(text = "نام کاربری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        dialogError = null
                    },
                    label = { Text(text = "رمز عبور جدید") },
                    placeholder = { Text(text = "خالی = بدون تغییر") },
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
            TextButton(
                onClick = {
                    when {
                        password.isNotEmpty() && password != confirmPassword ->
                            dialogError = "تکرار رمز با رمز جدید یکی نیست"
                        else -> onSave(username, password)
                    }
                }
            ) {
                Text(text = "ذخیره", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "انصراف")
            }
        }
    )
}
