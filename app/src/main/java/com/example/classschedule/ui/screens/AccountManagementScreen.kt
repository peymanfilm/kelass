package com.example.classschedule.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.AppRoles
import com.example.classschedule.data.AppUser
import com.example.classschedule.data.AuthRepository
import com.example.classschedule.util.toPersianDigits

/**
 * محتوای تب «حساب‌ها» — مدیریت کامل حساب‌ها، هم‌تراز با تب «کاربران» وب‌سایت:
 *
 * - مدیر: ساختن، ویرایش (نام، نام کاربری و رمز) و حذف حساب دانش‌آموزان و معلمان
 * - مالک برند MTM Kode: علاوه بر دانش‌آموزان و معلمان، حساب مدیرها را هم می‌سازد و عوض می‌کند
 *   (حساب خودِ مالک برند از تب تنظیمات و با تأیید رمز فعلی تغییر می‌کند)
 *
 * رمزها هرگز به صورت خام ذخیره نمی‌شوند؛ فقط هش SHA-256 روی خود گوشی نگه داشته می‌شود.
 */
@Composable
fun AccountsContent(
    callerRole: Int,
    authRepository: AuthRepository,
    users: List<AppUser>
) {
    val context = LocalContext.current
    // دیالوگ‌ها: ویرایش حساب، ساخت حساب تازه (نقش مقصد) و تأیید حذف
    var editingUser by remember { mutableStateOf<AppUser?>(null) }
    var addingRole by remember { mutableIntStateOf(-1) }
    var deletingUser by remember { mutableStateOf<AppUser?>(null) }

    // نقش‌هایی که این کاربر اجازه‌ی مدیریت آن‌ها را دارد (به ترتیب نمایش)
    val manageableRoles = remember(callerRole) {
        listOf(
            AppRoles.ROLE_STUDENT,
            AppRoles.ROLE_TEACHER,
            AppRoles.ROLE_MANAGER
        ).filter { authRepository.canEditAccount(callerRole, it) }
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
                            "شما مالک برند MTM Kode هستید؛ می‌توانید حساب دانش‌آموزان و معلمان و مدیرها را بسازید، ویرایش کنید یا حذف کنید."
                        } else {
                            "شما مدیر هستید؛ می‌توانید حساب دانش‌آموزان و معلمان را بسازید، ویرایش کنید یا حذف کنید."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "پس از ساخت هر حساب، ورود با نام کاربری و رمز آن انجام می‌شود. رمزها فقط هش‌شده روی همین گوشی ذخیره می‌شوند.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // ---------- یک بخش برای هر نقشِ قابل‌مدیریت ----------
        manageableRoles.forEach { role ->
            val roleUsers = users.filter { it.role == role }

            // سرصفحه‌ی بخش: آیکون و نام نقش + تعداد + دکمه افزودن
            item(key = "section-title-$role") {
                RoleSectionHeader(
                    role = role,
                    count = roleUsers.size,
                    onAdd = { addingRole = role }
                )
            }

            if (roleUsers.isEmpty()) {
                item(key = "section-empty-$role") {
                    Text(
                        text = "هنوز هیچ حسابی برای ${roleName(role)} ساخته نشده است؛ با دکمه «حساب جدید» بسازید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                items(roleUsers, key = { "user-${it.id}" }) { user ->
                    UserRow(
                        user = user,
                        onEdit = { editingUser = user },
                        onDelete = { deletingUser = user }
                    )
                }
            }
        }
    }

    // ---------- دیالوگ ویرایش حساب ----------
    editingUser?.let { target ->
        UserFormDialog(
            title = "ویرایش حساب ${accountTitle(target)}",
            initialName = target.name,
            initialUsername = target.username,
            saveLabel = "ذخیره تغییرات",
            onDismiss = { editingUser = null },
            onSave = { newName, newUsername, newPassword ->
                val result = authRepository.updateUser(
                    callerRole = callerRole,
                    targetUserId = target.id,
                    newName = newName,
                    newUsername = newUsername,
                    newPassword = newPassword
                )
                editingUser = null
                val message = when (result) {
                    AuthRepository.UpdateAccountResult.SUCCESS ->
                        "حساب «${accountTitle(target)}» به‌روزرسانی شد"
                    AuthRepository.UpdateAccountResult.NO_PERMISSION ->
                        "اجازه‌ی تغییر این حساب را ندارید"
                    AuthRepository.UpdateAccountResult.NOT_FOUND ->
                        "این حساب پیدا نشد؛ شاید قبلاً حذف شده باشد"
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

    // ---------- دیالوگ ساخت حساب تازه ----------
    if (addingRole != -1) {
        UserFormDialog(
            title = "حساب جدید ${roleName(addingRole)}",
            initialName = "",
            initialUsername = "",
            saveLabel = "ساخت حساب",
            onDismiss = { addingRole = -1 },
            onSave = { newName, newUsername, newPassword ->
                val result = authRepository.addUser(
                    callerRole = callerRole,
                    role = addingRole,
                    name = newName,
                    username = newUsername,
                    password = newPassword
                )
                addingRole = -1
                val message = when (result) {
                    AuthRepository.AddUserResult.SUCCESS ->
                        "حساب جدید ساخته شد؛ می‌توانید با آن وارد شوید"
                    AuthRepository.AddUserResult.NO_PERMISSION ->
                        "اجازه‌ی ساخت این نوع حساب را ندارید"
                    AuthRepository.AddUserResult.INVALID_USERNAME ->
                        "نام کاربری باید حداقل ۳ حرف و بدون فاصله باشد"
                    AuthRepository.AddUserResult.USERNAME_TAKEN ->
                        "این نام کاربری قبلاً گرفته شده است"
                    AuthRepository.AddUserResult.PASSWORD_TOO_SHORT ->
                        "رمز عبور باید حداقل ۴ حرف باشد"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ---------- دیالوگ تأیید حذف حساب ----------
    deletingUser?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingUser = null },
            title = { Text(text = "حذف حساب") },
            text = {
                Text(
                    text = "آیا از حذف حساب «${accountTitle(target)}» (نام کاربری: ${target.username}) مطمئن هستید؟ " +
                        "بعد از حذف، ورود با این حساب دیگر ممکن نیست."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val result = authRepository.deleteUser(
                            callerRole = callerRole,
                            targetUserId = target.id
                        )
                        deletingUser = null
                        val message = when (result) {
                            AuthRepository.DeleteUserResult.SUCCESS ->
                                "حساب «${accountTitle(target)}» حذف شد"
                            AuthRepository.DeleteUserResult.NO_PERMISSION ->
                                "اجازه‌ی حذف این حساب را ندارید"
                            AuthRepository.DeleteUserResult.NOT_FOUND ->
                                "این حساب پیدا نشد؛ شاید قبلاً حذف شده باشد"
                            AuthRepository.DeleteUserResult.CANNOT_DELETE_SELF ->
                                "نمی‌توان حسابی را حذف کرد که با آن وارد شده‌اید"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = "حذف",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingUser = null }) {
                    Text(text = "انصراف")
                }
            }
        )
    }
}

/**
 * سرصفحه‌ی بخش هر نقش: آیکون رنگی + نام نقش + تعداد حساب‌ها + دکمه «حساب جدید»
 */
@Composable
private fun RoleSectionHeader(
    role: Int,
    count: Int,
    onAdd: () -> Unit
) {
    val accent = roleAccent(role)
    val container = roleContainer(role)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = container.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = roleIcon(role),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "حساب‌های ${roleName(role)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Text(
                    text = "تعداد: ${count.toString().toPersianDigits()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "حساب جدید", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * ردیف یک حساب: آیکون + نام نمایشی + نام کاربری + دکمه‌های ویرایش و حذف
 */
@Composable
private fun UserRow(
    user: AppUser,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = roleAccent(user.role)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = roleIcon(user.role),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = accountTitle(user),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "نام کاربری: ${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "ویرایش حساب ${accountTitle(user)}",
                    tint = accent
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف حساب ${accountTitle(user)}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * فرم ساخت/ویرایش حساب:
 * - نام نمایشی (اختیاری؛ خالی باشد برای حساب جدید، برچسب نقش ثبت می‌شود)
 * - نام کاربری (الزامی و یکتا)
 * - رمز عبور: برای ویرایش خالی یعنی رمز فعلی دست نمی‌خورد؛ باید دو بار یکسان وارد شود
 */
@Composable
private fun UserFormDialog(
    title: String,
    initialName: String,
    initialUsername: String,
    saveLabel: String,
    onDismiss: () -> Unit,
    onSave: (newName: String, newUsername: String, newPassword: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var username by remember { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val isNewAccount = initialUsername.isBlank()
    var dialogError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                Text(
                    text = if (isNewAccount) {
                        "نام کاربری و رمز عبور حساب جدید را بنویسید؛ با این حساب می‌شود وارد برنامه شد."
                    } else {
                        "نام، نام کاربری و رمز عبور این حساب را عوض کنید. " +
                            "اگر رمز را خالی بگذارید، رمز فعلی بدون تغییر می‌ماند."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        dialogError = null
                    },
                    label = { Text(text = "نام نمایشی (اختیاری)") },
                    placeholder = { Text(text = "مثلاً علی محمدی") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        dialogError = null
                    },
                    label = { Text(text = "نام کاربری *") },
                    placeholder = { Text(text = "مثلاً student2") },
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
                    label = { Text(text = if (isNewAccount) "رمز عبور *" else "رمز عبور جدید") },
                    placeholder = { Text(text = if (isNewAccount) "حداقل ۴ حرف" else "خالی = بدون تغییر") },
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
                    label = { Text(text = "تکرار رمز *") },
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
                        password.isEmpty() && isNewAccount ->
                            dialogError = "برای حساب جدید، رمز عبور الزامی است"
                        password.isNotEmpty() && password != confirmPassword ->
                            dialogError = "تکرار رمز با رمز جدید یکی نیست"
                        else -> onSave(name, username, password)
                    }
                }
            ) {
                Text(text = saveLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "انصراف")
            }
        }
    )
}
