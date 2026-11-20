package com.example.classschedule.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * عملیات دیتابیسی حساب‌های کاربران — همه‌ی کوئری‌ها suspend یا Flow هستند
 * تا از رشته‌ی اصلی برنامه دور انجام شوند و رابط کاربری هیچ‌وقت قفل نشود.
 */
@Dao
interface UserDao {

    /** فهرست زنده‌ی همه‌ی حساب‌ها به ترتیب نقش و زمان ساخت */
    @Query("SELECT * FROM app_user ORDER BY role ASC, id ASC")
    fun observeAll(): Flow<List<AppUser>>

    /** شمارش حساب‌ها — برای تشخیص اولین اجرا و seed شدن حساب‌های پیش‌فرض */
    @Query("SELECT COUNT(*) FROM app_user")
    suspend fun count(): Int

    /** یافتن حساب با شناسه */
    @Query("SELECT * FROM app_user WHERE id = :id LIMIT 1")
    suspend fun byId(id: Int): AppUser?

    /** یافتن حساب با نام کاربری (برای ورود و بررسی تکراری‌نبودن نام کاربری) */
    @Query("SELECT * FROM app_user WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): AppUser?

    /** اولین حسابِ یک نقش — حساب پیش‌فرض هر نقش (مثلاً حساب مالک برند) */
    @Query("SELECT * FROM app_user WHERE role = :role ORDER BY id ASC LIMIT 1")
    suspend fun firstByRole(role: Int): AppUser?

    /** ساخت حساب جدید — شناسه‌ی تازه برمی‌گرداند */
    @Insert
    suspend fun insert(user: AppUser): Long

    /** ویرایش حساب (نام، نام کاربری یا هش رمز) */
    @Update
    suspend fun update(user: AppUser)

    /** حذف حساب */
    @Delete
    suspend fun delete(user: AppUser)
}
