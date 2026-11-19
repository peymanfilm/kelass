package com.example.classschedule.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * امضای برنامه «کلاس چی» — یک‌جا تعریف شده تا در همه‌ی صفحه‌ها یکسان نمایش داده شود:
 * «کلاس چی قدرت گرفته از MTM Kode»
 * نام برند MTM Kode برجسته (بولد سیاه + رنگ کهربایی) نمایش داده می‌شود چون اسم برند است.
 *
 * @param verticalPadding فاصله‌ی عمودی بالا و پایین امضا (در صفحه‌ی انتخاب پنل ۱۴dp است)
 */
@Composable
internal fun BrandSignature(
    modifier: Modifier = Modifier,
    verticalPadding: Dp = 6.dp
) {
    Text(
        text = buildAnnotatedString {
            append("کلاس چی قدرت گرفته از ")
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Black,
                    color = BrandColor
                )
            ) {
                append("MTM Kode")
            }
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding)
    )
}

/** رنگ کهربایی برند MTM Kode — همان رنگ تأکیدی وب‌سایت */
private val BrandColor = Color(0xFFB45309)
