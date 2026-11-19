package com.example.classschedule.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.classschedule.data.AppRoles

/**
 * رنگ اختصاصی هر پنل — خارج از تم برنامه تا هر نقش هویت بصری خودش را داشته باشد.
 * از رنگ‌های آبی و نیلی استفاده نشده است.
 */
private data class RoleVisual(
    val role: Int,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accent: Color,
    val container: Color
)

private val ROLE_VISUALS = listOf(
    RoleVisual(
        role = AppRoles.ROLE_STUDENT,
        title = "دانش‌آموز",
        description = "برنامه هفتگی من، تکالیف، عکس تکالیف و دیدن کارنامه",
        icon = Icons.Default.School,
        accent = Color(0xFF2E7D32),        // سبز
        container = Color(0xFFE8F5E9)
    ),
    RoleVisual(
        role = AppRoles.ROLE_TEACHER,
        title = "معلم",
        description = "ثبت تکلیف، ثبت نمره، منفی و مثبت و گالری تحویل‌شده‌ها",
        icon = Icons.Default.HistoryEdu,
        accent = Color(0xFFE65100),        // نارنجی
        container = Color(0xFFFFF3E0)
    ),
    RoleVisual(
        role = AppRoles.ROLE_MANAGER,
        title = "مدیر",
        description = "خلاصه و آمار کلی مدرسه، برنامه و کارنامه — فقط مشاهده",
        icon = Icons.Default.ManageAccounts,
        accent = Color(0xFF00695C),        // سبز تیره (تیل)
        container = Color(0xFFE0F2F1)
    ),
    RoleVisual(
        role = AppRoles.ROLE_OWNER,
        title = "مالک",
        description = "همه‌ی امکانات به‌همراه تنظیمات برنامه و تعویض پنل‌ها",
        icon = Icons.Default.WorkspacePremium,
        accent = Color(0xFF6A1B9A),        // بنفش
        container = Color(0xFFF3E5F5)
    )
)

/** برچسب فارسی هر نقش برای نمایش در نوار بالای پنل‌ها */
fun roleTitle(role: Int): String = when (role) {
    AppRoles.ROLE_TEACHER -> "پنل معلم"
    AppRoles.ROLE_MANAGER -> "پنل مدیر"
    AppRoles.ROLE_OWNER -> "پنل مالک"
    else -> "پنل دانش‌آموز"
}

/**
 * صفحه انتخاب پنل «کلاس چی»:
 * هر شخص (دانش‌آموز، معلم، مدیر، مالک) کارت پنل خودش را لمس می‌کند.
 * انتخاب روی گوشی ذخیره می‌شود و دفعه‌ی بعد مستقیم همان پنل باز می‌شود.
 * هیچ حساب کاربری، رمز، اینترنت یا هزینه‌ای لازم نیست — همه‌چیز آفلاین است.
 *
 * @param canGoBack وقتی از داخل یک پنل باز شده، دکمه بازگشت نشان داده می‌شود
 */
@Composable
fun RoleSelectionScreen(
    onSelectRole: (Int) -> Unit,
    canGoBack: Boolean = false,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // پس‌زمینه‌ی کامل: وقتی به صورت صفحه‌ی روی‌گذاشتنی از داخل پنل باز می‌شود،
            // پنل قبلی از پشتش دیده نشود
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        // ردیف بازگشت (فقط وقتی از داخل پنل اینجا آمده‌ایم)
        if (canGoBack) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "بازگشت به پنل قبلی",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(28.dp))
        }

        // سربرگ معرفی برنامه
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "کلاس چی",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "پنل خودتان را انتخاب کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // چهار کارت پنل — دو ردیف دو‌تایی
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RoleCard(
                visual = ROLE_VISUALS[0],
                onClick = { onSelectRole(ROLE_VISUALS[0].role) },
                modifier = Modifier.weight(1f)
            )
            RoleCard(
                visual = ROLE_VISUALS[1],
                onClick = { onSelectRole(ROLE_VISUALS[1].role) },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RoleCard(
                visual = ROLE_VISUALS[2],
                onClick = { onSelectRole(ROLE_VISUALS[2].role) },
                modifier = Modifier.weight(1f)
            )
            RoleCard(
                visual = ROLE_VISUALS[3],
                onClick = { onSelectRole(ROLE_VISUALS[3].role) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // توضیح کوتاه برای کاربر
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Text(
                text = "همه‌ی اطلاعات فقط روی همین گوشی ذخیره می‌شود؛ " +
                    "بدون نیاز به اینترنت و بدون هیچ هزینه‌ای. " +
                    "هر وقت بخواهید می‌توانید از نوار بالای پنل، پنل را عوض کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // امضای برنامه
        Text(
            text = "کلاس چی محصولی از MTM Code",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
        )
    }
}

/**
 * کارت یک پنل: آیکون رنگی + نام نقش + توضیح کوتاه + شماره پنل
 */
@Composable
private fun RoleCard(
    visual: RoleVisual,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = visual.container),
        border = BorderStroke(width = 1.dp, color = visual.accent.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(visual.accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = visual.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = visual.accent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = visual.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                maxLines = 3
            )
        }
    }
}
