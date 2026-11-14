package com.example.classschedule.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.classschedule.util.toPersianDigits
import java.util.Locale

/**
 * دیالوگ انتخاب رنگ دلخواه (انتخاب دستی)
 * کاربر با سه کشویی «فام»، «شدت رنگ» و «روشنایی» هر رنگی می‌سازد.
 * پیش‌نمایش زنده رنگ + کد رنگ نمایش داده می‌شود.
 */
@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onConfirm: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    // تبدیل رنگ اولیه به مقادیر HSV
    val initialHsv = FloatArray(3).also { hsv ->
        AndroidColor.colorToHSV(initialColor.toArgb(), hsv)
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }          // ۰ تا ۳۶۰
    var saturation by remember { mutableFloatStateOf(initialHsv[1] * 100f) } // ۰ تا ۱۰۰
    var value by remember { mutableFloatStateOf(initialHsv[2] * 100f) }      // ۰ تا ۱۰۰

    // رنگ جاری از روی سه کشو ساخته می‌شود
    val currentColor = Color(
        AndroidColor.HSVToColor(floatArrayOf(hue, saturation / 100f, value / 100f))
    )
    val hexCode = String.format(Locale.US, "#%06X", currentColor.toArgb() and 0xFFFFFF)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "انتخاب رنگ دلخواه")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "با کشیدن سه کشو زیر، هر رنگی که دوست دارید بسازید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Right
                )

                // پیش‌نمایش زنده رنگ + کد رنگ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(currentColor)
                    )
                    Column {
                        Text(
                            text = "رنگ انتخابی",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = hexCode,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // کشو فام (طیف رنگین‌کمانی)
                GradientSlider(
                    label = "فام",
                    valueText = hue.toInt().toString().toPersianDigits(),
                    value = hue,
                    valueRange = 0f..360f,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF0000),
                            Color(0xFFFFFF00),
                            Color(0xFF00FF00),
                            Color(0xFF00FFFF),
                            Color(0xFF0000FF),
                            Color(0xFFFF00FF),
                            Color(0xFFFF0000)
                        )
                    ),
                    onValueChange = { hue = it }
                )

                // کشو شدت رنگ (از خاکستری تا رنگ کامل)
                GradientSlider(
                    label = "شدت رنگ",
                    valueText = saturation.toInt().toString().toPersianDigits(),
                    value = saturation,
                    valueRange = 0f..100f,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(
                                AndroidColor.HSVToColor(floatArrayOf(hue, 0f, value / 100f))
                            ),
                            Color(
                                AndroidColor.HSVToColor(floatArrayOf(hue, 1f, value / 100f))
                            )
                        )
                    ),
                    onValueChange = { saturation = it }
                )

                // کشو روشنایی (از سیاه تا رنگ کامل)
                GradientSlider(
                    label = "روشنایی",
                    valueText = value.toInt().toString().toPersianDigits(),
                    value = value,
                    valueRange = 0f..100f,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Black,
                            Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation / 100f, 1f)))
                        )
                    ),
                    onValueChange = { value = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(currentColor) }) {
                Text(text = "انتخاب این رنگ")
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
 * کشویی با نوار رنگین (گرادیانی) — برای انتخاب رنگ
 */
@Composable
private fun GradientSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    brush: Brush,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(modifier = Modifier.weight(1f))
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // نوار گرادیانی زیر کشو
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(brush)
            )
            // کشوی واقعی با نوار شفاف
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent
                )
            )
        }
    }
}
