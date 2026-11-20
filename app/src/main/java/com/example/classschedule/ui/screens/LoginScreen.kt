package com.example.classschedule.ui.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.AppRoles
import com.example.classschedule.data.AppUser
import com.example.classschedule.data.AuthRepository

/**
 * دروازه‌ی ورود برنامه «کلاس چی» — احراز هویت آفلاین با نام کاربری و رمز عبور:
 * - حساب‌ها مثل وب‌سایت در جدول کاربران هستند؛ چیپ هر حساب با نام نمایشی او دیده می‌شود
 *   و لمس چیپ، مستقیم وارد پنل همان حساب می‌شود
 * - رمز پیش‌فرض همه‌ی حساب‌ها «۱۲۳۴» است؛ مدیر یا مالک برند می‌توانند رمزها را عوض کنند
 * - هیچ اینترنت، سرور یا هزینه‌ای در کار نیست؛ رمزها هش‌شده روی خود گوشی ذخیره می‌شوند
 *
 * نکته‌ی ظاهری مهم:
 * - برچسب «نام کاربری» و «رمز عبور» بیرون کادر و بالای آن است
 * - آیکون شخص و قفل هم بیرون کادر (سمت راست) قرار گرفته است، نه داخل فیلد متنی؛
 *   به این ترتیب در حالت راست‌به‌چپ هیچ‌وقت آیکون روی نوشته نمی‌افتد (رفع باگ هم‌پوشانی)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLogin: (username: String, password: String) -> Int
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // فهرست زنده‌ی حساب‌های این گوشی — اگر مدیر یا مالک حسابی بسازد یا عوض کند، همین‌جا دیده می‌شود
    val users by authRepository.usersFlow.collectAsState(initial = emptyList())

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
                // نام کاربری — برچسب و آیکون بیرون کادر تا هیچ هم‌پوشانی رخ ندهد
                FieldLabel(text = "نام کاربری")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FieldIconBox(icon = Icons.Default.Person)
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        placeholder = { Text(text = "مثلاً student1") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // رمز عبور — آیکون قفل بیرون کادر و دکمه‌ی نمایش/پنهان هم بیرون کادر
                FieldLabel(text = "رمز عبور")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FieldIconBox(icon = Icons.Default.Lock)
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        placeholder = { Text(text = "رمز را وارد کنید") },
                        visualTransformation = if (showPassword) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    // دکمه نمایش/پنهان‌کردن رمز — بیرون کادر، جدا از متن
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
                }

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

                // ---------- چیپ‌های حساب‌ها: لمس کنید تا مستقیم وارد شوید ----------
                Text(
                    text = "حساب‌های برنامه (لمس کنید تا مستقیم وارد شوید):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    users.forEach { user ->
                        AccountChip(
                            user = user,
                            onPick = {
                                username = user.username
                                errorMessage = null
                                // ورود مستقیم با رمز پیش‌فرض — مثل دکمه‌های ورود سریع وب‌سایت؛
                                // اگر رمز این حساب عوض شده باشد، پیام راهنما نمایش داده می‌شود
                                val picked = onLogin(user.username, AuthRepository.DEFAULT_PASSWORD)
                                if (picked == AppRoles.ROLE_NONE) {
                                    errorMessage = "رمز حساب «${accountTitle(user)}» عوض شده است؛ " +
                                        "نام کاربری پر شد، رمز جدید را بنویسید و «ورود به برنامه» را بزنید."
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
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
                    "مدیر می‌تواند حساب دانش‌آموزان و معلمان را بسازد و عوض کند " +
                    "و مالک برند MTM Kode علاوه بر آن‌ها، حساب مدیر را هم می‌سازد و تغییر می‌دهد.",
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

/** عنوان نمایشی یک حساب: نام کاربر؛ اگر خالی بود، برچسب نقش */
internal fun accountTitle(user: AppUser): String =
    user.name.trim().ifBlank { roleName(user.role) }

/**
 * برچسب فیلد — بیرون و بالای کادر نوشته می‌شود
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
 * جعبه‌ی آیکون فیلد — بیرون از فیلد متنی و در سمت راست آن؛
 * با این طراحی، آیکون هیچ‌وقت روی نوشته‌ی داخل کادر نمی‌افتد.
 */
@Composable
private fun FieldIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * چیپ حساب: آیکون رنگی نقش + نام نمایشی کاربر + نام کاربری؛
 * لمس آن مستقیم وارد حساب می‌شود (ورود سریع مثل وب‌سایت).
 */
@Composable
private fun AccountChip(
    user: AppUser,
    onPick: () -> Unit
) {
    val accent = roleAccent(user.role)
    val container = roleContainer(user.role)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = container,
        modifier = Modifier
            .clickable(onClickLabel = "ورود با حساب ${accountTitle(user)}", onClick = onPick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = roleIcon(user.role),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = accountTitle(user),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ورود",
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
