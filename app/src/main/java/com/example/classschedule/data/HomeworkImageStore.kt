package com.example.classschedule.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * مدیریت فایل عکس‌های تکالیف
 * عکس‌ها با انتخابگر گالری گرفته می‌شوند، تا اندازه‌ی مناسب کوچک می‌شوند
 * و در پوشه‌ی اختصاصی برنامه (homework_images) در حافظه‌ی داخلی ذخیره می‌شوند.
 * نام فایل‌ها در ستون imageNames جدول homework نگه‌داری می‌شود.
 */
object HomeworkImageStore {

    /** نام پوشه‌ی عکس‌ها در حافظه‌ی داخلی برنامه */
    private const val DIR_NAME = "homework_images"

    /** بزرگ‌ترین ضلع مجاز عکس ذخیره‌شده (پیکسل) — برای صرفه‌جویی در فضا و حافظه */
    private const val MAX_DIMENSION = 1280

    /** کیفیت ذخیره‌ی JPEG (۰ تا ۱۰۰) */
    private const val JPEG_QUALITY = 82

    /** پوشه‌ی ذخیره‌ی عکس‌ها (در حافظه‌ی داخلی اختصاصی برنامه — بدون نیاز به مجوز) */
    fun imageDir(context: Context): File =
        File(context.filesDir, DIR_NAME).apply { mkdirs() }

    /** فایل یک عکس با نام مشخص */
    fun imageFile(context: Context, name: String): File = File(imageDir(context), name)

    /**
     * خواندن تصویر از uri گالری، کوچک‌کردن تا حداکثر MAX_DIMENSION
     * و ذخیره‌ی آن در حافظه‌ی برنامه به شکل JPEG
     * خروجی: نام فایل ذخیره‌شده (مثل hw_1699...jpg) یا null در صورت خطا
     */
    fun saveImageFromUri(context: Context, uri: Uri): String? {
        return try {
            val resolver = context.contentResolver

            // نخست فقط اندازه‌ی تصویر خوانده می‌شود (بدون بارگذاری کامل آن)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val boundsStream = resolver.openInputStream(uri) ?: return null
            boundsStream.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            // بارگذاری تصویر با نمونه‌برداری مناسب تا بزرگ‌ترین ضلع حدود MAX_DIMENSION شود
            val options = BitmapFactory.Options().apply {
                inSampleSize = computeInSampleSize(bounds.outWidth, bounds.outHeight)
            }
            val bitmap = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return null

            val name = "hw_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg"
            val target = imageFile(context, name)
            FileOutputStream(target).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            bitmap.recycle()
            name
        } catch (e: Exception) {
            null
        }
    }

    /** خواندن یک عکس ذخیره‌شده برای نمایش؛ بزرگ‌ترین ضلع حداکثر maxDim پیکسل */
    fun loadBitmap(context: Context, name: String, maxDim: Int): Bitmap? {
        return try {
            val file = imageFile(context, name)
            if (!file.exists()) return null

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val options = BitmapFactory.Options().apply {
                inSampleSize = computeInSampleSize(bounds.outWidth, bounds.outHeight, maxDim)
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            null
        }
    }

    /** حذف فایل عکس (اگر وجود داشته باشد) */
    fun deleteImage(context: Context, name: String) {
        try {
            imageFile(context, name).delete()
        } catch (e: Exception) {
            // حذف ناموفق اهمیتی ندارد
        }
    }

    /** حذف چند عکس با هم */
    fun deleteImages(context: Context, names: List<String>) {
        names.forEach { deleteImage(context, it) }
    }

    /**
     * محاسبه‌ی inSampleSize برای نمونه‌برداری Bitmap
     * تا بزرگ‌ترین ضلع تصویر به حدود maxDim برسد (پایین‌تر از maxDim نمی‌آید)
     */
    private fun computeInSampleSize(width: Int, height: Int, maxDim: Int = MAX_DIMENSION): Int {
        var sample = 1
        val largest = maxOf(width, height)
        while (largest / (sample * 2) >= maxDim) {
            sample *= 2
        }
        return sample
    }
}
