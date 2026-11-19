package com.example.classschedule.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.security.MessageDigest

/**
 * دروازه‌ی احراز هویت برنامه «کلاس چی» — کاملاً آفلاین و روی خود گوشی:
 * - حساب پیش‌فرض: نام کاربری «admin» و رمز «۱۲۳۴» که در اولین اجرا ساخته می‌شود
 * - رمز عبور هرگز به صورت خام ذخیره نمی‌شود؛ فقط هش SHA-256 آن نگه داشته می‌شود
 * - وضعیت ورود روی گوشی می‌ماند؛ تا وقتی کاربر «خروج» نزده، برنامه مستقیم باز می‌شود
 * - تغییر رمز از تب تنظیمات پنل مالک برند انجام می‌شود
 */
class AuthRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** نام کاربری حساب فعال — خالی یعنی هنوز هیچ حسابی ساخته نشده */
    private val _username = MutableStateFlow(prefs.getString(KEY_USERNAME, DEFAULT_USERNAME) ?: DEFAULT_USERNAME)
    val username: StateFlow<String> = _username.asStateFlow()

    /** آیا کاربر همین الان وارد شده است؟ (با خروج، صفحه ورود دوباره باز می‌شود) */
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_LOGGED_IN, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        // اولین اجرا: ساختن حساب پیش‌فرض اگر قبلاً ساخته نشده باشد
        if (!prefs.contains(KEY_PASSWORD_HASH)) {
            prefs.edit().putString(KEY_PASSWORD_HASH, hash(DEFAULT_PASSWORD)).apply()
        }
    }

    /**
     * بررسی نام کاربری و رمز عبور:
     * اگر درست بود، وضعیت «واردشده» روی گوشی ذخیره و true برگردانده می‌شود.
     */
    fun login(username: String, password: String): Boolean {
        val cleanUser = username.trim()
        if (cleanUser.isEmpty() || password.isEmpty()) return false

        val savedUser = prefs.getString(KEY_USERNAME, DEFAULT_USERNAME) ?: DEFAULT_USERNAME
        val savedHash = prefs.getString(KEY_PASSWORD_HASH, hash(DEFAULT_PASSWORD)) ?: hash(DEFAULT_PASSWORD)

        val userOk = cleanUser.equals(savedUser, ignoreCase = false)
        val passOk = hash(password) == savedHash
        if (!userOk || !passOk) return false

        prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
        _isLoggedIn.update { true }
        _username.update { savedUser }
        return true
    }

    /** خروج از حساب: وضعیت ورود پاک می‌شود و صفحه ورود دوباره باز می‌شود (داده‌ها دست‌نخورده می‌مانند) */
    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
        _isLoggedIn.update { false }
    }

    /**
     * تغییر رمز عبور — با تأیید رمز فعلی.
     * نتیجه به شکل کد بازگردانده می‌شود تا پیام فارسی دقیق در UI نمایش داده شود.
     */
    fun changePassword(currentPassword: String, newPassword: String): ChangePasswordResult {
        val savedHash = prefs.getString(KEY_PASSWORD_HASH, hash(DEFAULT_PASSWORD)) ?: hash(DEFAULT_PASSWORD)
        if (hash(currentPassword) != savedHash) {
            return ChangePasswordResult.WRONG_CURRENT
        }
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            return ChangePasswordResult.TOO_SHORT
        }
        if (newPassword == currentPassword) {
            return ChangePasswordResult.SAME_AS_CURRENT
        }
        prefs.edit().putString(KEY_PASSWORD_HASH, hash(newPassword)).apply()
        return ChangePasswordResult.SUCCESS
    }

    enum class ChangePasswordResult {
        SUCCESS,          // رمز با موفقیت عوض شد
        WRONG_CURRENT,    // رمز فعلی درست نیست
        TOO_SHORT,        // رمز جدید خیلی کوتاه است
        SAME_AS_CURRENT   // رمز جدید با رمز فعلی یکی است
    }

    companion object {
        private const val PREFS_NAME = "app_auth"
        private const val KEY_USERNAME = "auth_username"
        private const val KEY_PASSWORD_HASH = "auth_password_hash"
        private const val KEY_LOGGED_IN = "auth_logged_in"

        /** حساب پیش‌فرض برنامه — کاربر می‌تواند رمز را از تنظیمات مالک برند عوض کند */
        const val DEFAULT_USERNAME = "admin"
        const val DEFAULT_PASSWORD = "1234"
        const val MIN_PASSWORD_LENGTH = 4

        /**
         * هش SHA-256 رشته با نمک ثابت برنامه؛
         * رمز عبور خام هیچ‌وقت در حافظه‌ی گوشی ذخیره نمی‌شود.
         */
        fun hash(text: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest((SALT + text).toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
