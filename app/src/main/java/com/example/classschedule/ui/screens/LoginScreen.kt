package com.example.classschedule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.AppRoles
import com.example.classschedule.data.AuthRepository

/**
 * دروازه‌ی ورود برنامه «کلاس چی» — احراز هویت آفلاین با نام کاربری و رمز عبور:
 * - چهار حساب نقش‌محور: student1 (دانش‌آموز) / teacher1 (معلم) / manager (مدیر) / owner (مالک برند)
 * - رمز پیش‌فرض همه‌ی حساب‌ها «۱۲۳۴» است و بعد از ورود، پنل همان نقش باز می‌شود
 * - هیچ اینترنت، سرور یا هزینه‌ای در کار نیست؛ رمزها هش‌شده روی خود گوشی ذخیره می‌شوند
 *
 * نکته‌ی ظاهری: برچسب «نام کاربری» و «رمز عبور» بیرون کادر و بالای آن قرار گرفته
 * تا با آیکون داخل کادر هیچ‌وقت هم‌پوشان نشود (مشکل هم‌پوشانی در حالت راست‌به‌چپ).
 */
@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLogin: (username: String, password: String) -> Int
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // حساب‌های فعلی روی این گوشی — اگر مدیر یا مالک نام کاربری را عوض کرده باشد، همین‌جا دیده می‌شود
    val accounts = remember { authRepository.accounts() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // ---------- لوگو و عنوان برند ----------
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "کلاس چی",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "دروازه‌ی ورود — احراز هویت آفلاین",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        // ---------- فرم ورود ----------
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // نام کاربری — برچسب بیرون کادر تا با آیکون داخل کادر هم‌پوشان نشود
                FieldLabel(text = "نام کاربری")
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = null
                    },
                    placeholder = { Text(text = "مثلاً student1") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // رمز عبور با دکمه‌ی نمایش/پنهان‌کردن — برچسب بیرون کادر
                FieldLabel(text = "رمز عبور")
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    placeholder = { Text(text = "رمز را وارد کنید") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (showPassword) {
                                    "پنهان کردن رمز"
                                } else {
                                    "نمایش رمز"
                                },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // پیام خطا — نام کاربری یا رمز اشتباه
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // دکمه ورود — بعد از ورود، پنل همان نقش باز می‌شود
                Button(
                    onClick = {
                        when {
                            username.trim().isEmpty() -> errorMessage = "نام کاربری را وارد کنید"
                            password.isEmpty() -> errorMessage = "رمز عبور را وارد کنید"
                            else -> {
                                val role = onLogin(username, password)
                                if (role == AppRoles.ROLE_NONE) {
                                    errorMessage = "نام کاربری یا رمز عبور اشتباه است"
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "ورود به برنامه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ---------- چیپ‌های حساب‌ها: لمس کنید تا نام کاربری پر شود ----------
                Text(
                    text = "حساب‌های برنامه (لمس کنید تا نام کاربری نوشته شود):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                accounts.forEach { account ->
                    AccountChip(
                        role = account.role,
                        username = account.username,
                        onPick = {
                            username = account.username
                            errorMessage = null
                        }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = "رمز پیش‌فرض همه‌ی حساب‌ها: ۱۲۳۴ — اگر مدیر یا مالک برند رمز را عوض کرده باشد، رمز جدید را وارد کنید.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ---------- نکته‌ی آفلاین بودن ----------
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        ) {
            Text(
                text = "احراز هویت کاملاً روی همین گوشی انجام می‌شود؛ " +
                    "بدون اینترنت، بدون سرور و بدون هیچ هزینه‌ای. " +
                    "مدیر می‌تواند حساب دانش‌آموزان و معلمان را از تب «حساب‌ها» عوض کند " +
                    "و مالک برند MTM Kode علاوه بر آن‌ها، حساب مدیر را هم تغییر می‌دهد.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // امضای برنامه — نام برند برجسته می‌شود
        BrandSignature(verticalPadding = 14.dp)
    }
}

/**
 * برچسب فیلد — بیرون و بالای کادر نوشته می‌شود تا با آیکون داخل کادر هم‌پوشانی نداشته باشد
 */
@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    )
}

/**
 * چیپ حساب: آیکون رنگی نقش + برچسب فارسی + نام کاربری فعلی؛
 * لمس آن نام کاربری را در فیلد ورود می‌نویسد.
 */
@Composable
private fun AccountChip(
    role: Int,
    username: String,
    onPick: () -> Unit
) {
    val accent = roleAccent(role)
    val container = roleContainer(role)
    val icon = roleIcon(role)
    val title = roleName(role)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = container,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "پرکردن نام کاربری $title", onClick = onPick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = username,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "انتخاب",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier
                    .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}
