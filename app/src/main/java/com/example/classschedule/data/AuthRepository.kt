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
 *
 * - چهار حساب نقش‌محور (هم‌تراز با وب‌سایت) با نام کاربری و رمز جداگانه:
 *   دانش‌آموز (student1) / معلم (teacher1) / مدیر (manager) / مالک برند MTM Kode (owner)
 * - رمز پیش‌فرض همه‌ی حساب‌ها «۱۲۳۴» است؛ مدیر می‌تواند نام کاربری و رمز حساب
 *   دانش‌آموزان و معلمان را عوض کند و مالک برند علاوه بر آن‌ها، حساب مدیر را هم.
 * - رمز عبور هرگز به صورت خام ذخیره نمی‌شود؛ فقط هش SHA-256 آن نگه داشته می‌شود
 * - حساب قدیمی نسخه‌ی قبل (admin) در اولین اجرا به حساب مالک برند منتقل می‌شود
 */
class AuthRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** آیا کاربر همین الان وارد شده است؟ (با خروج، صفحه ورود دوباره باز می‌شود) */
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_LOGGED_IN, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /** اطلاعات یک حساب: نقش + نام کاربری فعلی */
    data class Account(val role: Int, val username: String)

    init {
        migrateLegacyAccountIfNeeded()
        ensureDefaultAccounts()
    }

    /**
     * مهاجرت حساب قدیمی نسخه‌های قبل (یک حساب مشترک admin):
     * اگر رمز آن حساب شخصی‌سازی شده بود، همان نام کاربری و رمز به حساب
     * «مالک برند MTM Kode» منتقل می‌شود تا ورود کاربر قبلی از کار نیفتد.
     */
    private fun migrateLegacyAccountIfNeeded() {
        val legacyHash = prefs.getString(KEY_LEGACY_PASSWORD_HASH, null) ?: return
        if (legacyHash != hash(DEFAULT_PASSWORD)) {
            val legacyUsername = prefs.getString(KEY_LEGACY_USERNAME, LEGACY_USERNAME)
                ?: LEGACY_USERNAME
            prefs.edit()
                .putString(usernameKey(AppRoles.ROLE_OWNER), legacyUsername)
                .putString(passwordHashKey(AppRoles.ROLE_OWNER), legacyHash)
                .apply()
        }
        prefs.edit()
            .remove(KEY_LEGACY_USERNAME)
            .remove(KEY_LEGACY_PASSWORD_HASH)
            .apply()
    }

    /** ساختن حساب‌های پیش‌فرض برای نقش‌هایی که هنوز حساب ندارند */
    private fun ensureDefaultAccounts() {
        val editor = prefs.edit()
        for (role in ACCOUNT_ROLES) {
            if (!prefs.contains(usernameKey(role))) {
                editor.putString(usernameKey(role), defaultUsername(role))
                editor.putString(passwordHashKey(role), hash(DEFAULT_PASSWORD))
            }
        }
        editor.apply()
    }

    /**
     * بررسی نام کاربری و رمز عبور روی هر چهار حساب:
     * اگر درست بود، وضعیت «واردشده» ذخیره و نقشِ حساب برگردانده می‌شود؛
     * در غیر این صورت ROLE_NONE برمی‌گردد (یعنی نام کاربری یا رمز اشتباه است).
     */
    fun login(username: String, password: String): Int {
        val cleanUser = username.trim()
        if (cleanUser.isEmpty() || password.isEmpty()) return AppRoles.ROLE_NONE

        for (role in ACCOUNT_ROLES) {
            val savedUser = prefs.getString(usernameKey(role), null) ?: continue
            val savedHash = prefs.getString(passwordHashKey(role), null) ?: continue
            if (cleanUser == savedUser && hash(password) == savedHash) {
                prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply()
                _isLoggedIn.update { true }
                return role
            }
        }
        return AppRoles.ROLE_NONE
    }

    /** خروج از حساب: وضعیت ورود پاک می‌شود و صفحه ورود دوباره باز می‌شود (داده‌ها دست‌نخورده می‌مانند) */
    fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply()
        _isLoggedIn.update { false }
    }

    /** فهرست همه‌ی حساب‌ها با نام کاربری فعلی — برای نمایش در صفحه ورود و مدیریت حساب‌ها */
    fun accounts(): List<Account> =
        ACCOUNT_ROLES.map { role ->
            Account(
                role = role,
                username = prefs.getString(usernameKey(role), defaultUsername(role))
                    ?: defaultUsername(role)
            )
        }

    /** نام کاربری فعلی یک نقش */
    fun usernameOf(role: Int): String =
        prefs.getString(usernameKey(role), defaultUsername(role)) ?: defaultUsername(role)

    /**
     * آیا این نقش اجازه‌ی تغییر حساب نقش مقصد را دارد؟
     * - مدیر: فقط حساب‌های دانش‌آموز و معلم
     * - مالک برند MTM Kode: حساب‌های دانش‌آموز، معلم و مدیر (به‌علاوه حساب خودش از تنظیمات)
     */
    fun canEditAccount(callerRole: Int, targetRole: Int): Boolean = when (callerRole) {
        AppRoles.ROLE_OWNER ->
            targetRole == AppRoles.ROLE_STUDENT ||
                targetRole == AppRoles.ROLE_TEACHER ||
                targetRole == AppRoles.ROLE_MANAGER
        AppRoles.ROLE_MANAGER ->
            targetRole == AppRoles.ROLE_STUDENT || targetRole == AppRoles.ROLE_TEACHER
        else -> false
    }

    /**
     * تغییر نام کاربری و رمز عبور حساب یک نقش (مدیریت حساب‌ها):
     * - رمز جدید خالی باشد یعنی رمز فعلی دست نمی‌خورد
     * - نتیجه به شکل کد بازگردانده می‌شود تا پیام فارسی دقیق در UI نمایش داده شود
     */
    fun updateAccount(
        callerRole: Int,
        targetRole: Int,
        newUsername: String,
        newPassword: String
    ): UpdateAccountResult {
        if (!canEditAccount(callerRole, targetRole)) return UpdateAccountResult.NO_PERMISSION

        val cleanUser = newUsername.trim()
        val currentUsername = usernameOf(targetRole)
        val usernameChanged = cleanUser != currentUsername

        if (cleanUser.isEmpty() ||
            cleanUser.length < MIN_USERNAME_LENGTH ||
            cleanUser.any { it.isWhitespace() }
        ) {
            return UpdateAccountResult.INVALID_USERNAME
        }

        if (usernameChanged) {
            val taken = ACCOUNT_ROLES.any { role ->
                role != targetRole && usernameOf(role).equals(cleanUser, ignoreCase = true)
            }
            if (taken) return UpdateAccountResult.USERNAME_TAKEN
        }

        val passwordChanged = newPassword.isNotEmpty()
        if (passwordChanged && newPassword.length < MIN_PASSWORD_LENGTH) {
            return UpdateAccountResult.PASSWORD_TOO_SHORT
        }
        if (!usernameChanged && !passwordChanged) {
            return UpdateAccountResult.NOTHING_CHANGED
        }

        val editor = prefs.edit()
        editor.putString(usernameKey(targetRole), cleanUser)
        if (passwordChanged) {
            editor.putString(passwordHashKey(targetRole), hash(newPassword))
        }
        editor.apply()
        return UpdateAccountResult.SUCCESS
    }

    enum class UpdateAccountResult {
        SUCCESS,           // حساب با موفقیت به‌روزرسانی شد
        NO_PERMISSION,     // این نقش اجازه‌ی تغییر حساب مقصد را ندارد
        INVALID_USERNAME,  // نام کاربری خالی، کوتاه یا دارای فاصله است
        USERNAME_TAKEN,    // این نام کاربری برای حساب دیگری گرفته شده است
        PASSWORD_TOO_SHORT,// رمز جدید خیلی کوتاه است
        NOTHING_CHANGED    // نام کاربری همان قبلی است و رمز هم خالی مانده
    }

    /**
     * تغییر نام کاربری حساب خودِ مالک برند — با تأیید رمز فعلی.
     */
    fun changeSelfUsername(currentPassword: String, newUsername: String): SelfUsernameResult {
        val ownerHash = prefs.getString(passwordHashKey(AppRoles.ROLE_OWNER), null)
            ?: hash(DEFAULT_PASSWORD)
        if (hash(currentPassword) != ownerHash) {
            return SelfUsernameResult.WRONG_PASSWORD
        }
        val cleanUser = newUsername.trim()
        if (cleanUser.isEmpty() ||
            cleanUser.length < MIN_USERNAME_LENGTH ||
            cleanUser.any { it.isWhitespace() }
        ) {
            return SelfUsernameResult.INVALID_USERNAME
        }
        val taken = ACCOUNT_ROLES.any { role ->
            role != AppRoles.ROLE_OWNER &&
                usernameOf(role).equals(cleanUser, ignoreCase = true)
        }
        if (taken) return SelfUsernameResult.USERNAME_TAKEN
        prefs.edit().putString(usernameKey(AppRoles.ROLE_OWNER), cleanUser).apply()
        return SelfUsernameResult.SUCCESS
    }

    enum class SelfUsernameResult {
        SUCCESS,           // نام کاربری عوض شد
        WRONG_PASSWORD,    // رمز فعلی درست نیست
        INVALID_USERNAME,  // نام کاربری خالی، کوتاه یا دارای فاصله است
        USERNAME_TAKEN     // این نام کاربری برای حساب دیگری گرفته شده است
    }

    /**
     * تغییر رمز عبور حساب خودِ مالک برند — با تأیید رمز فعلی.
     * نتیجه به شکل کد بازگردانده می‌شود تا پیام فارسی دقیق در UI نمایش داده شود.
     */
    fun changePassword(currentPassword: String, newPassword: String): ChangePasswordResult {
        val ownerHash = prefs.getString(passwordHashKey(AppRoles.ROLE_OWNER), null)
            ?: hash(DEFAULT_PASSWORD)
        if (hash(currentPassword) != ownerHash) {
            return ChangePasswordResult.WRONG_CURRENT
        }
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            return ChangePasswordResult.TOO_SHORT
        }
        if (newPassword == currentPassword) {
            return ChangePasswordResult.SAME_AS_CURRENT
        }
        prefs.edit().putString(passwordHashKey(AppRoles.ROLE_OWNER), hash(newPassword)).apply()
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
        private const val KEY_LOGGED_IN = "auth_logged_in"

        // کلیدهای حساب قدیمی نسخه‌های قبل — فقط برای مهاجرت خوانده و پاک می‌شوند
        private const val KEY_LEGACY_USERNAME = "auth_username"
        private const val KEY_LEGACY_PASSWORD_HASH = "auth_password_hash"

        /** حساب پیش‌فرض هر نقش — هم‌تراز با حساب‌های نمونه‌ی وب‌سایت */
        const val DEFAULT_STUDENT_USERNAME = "student1"
        const val DEFAULT_TEACHER_USERNAME = "teacher1"
        const val DEFAULT_MANAGER_USERNAME = "manager"
        const val DEFAULT_OWNER_USERNAME = "owner"

        /** حساب قدیمی نسخه‌ی قبل (برای مهاجرت) */
        const val LEGACY_USERNAME = "admin"

        /** رمز پیش‌فرض همه‌ی حساب‌ها */
        const val DEFAULT_PASSWORD = "1234"
        const val MIN_PASSWORD_LENGTH = 4
        const val MIN_USERNAME_LENGTH = 3

        /** نمک ثابت هش رمز عبور — رمز خام هرگز ذخیره نمی‌شود */
        private const val SALT = "kelass-mtm-kode-v1"

        /** نقش‌هایی که حساب دارند (به ترتیب نمایش) */
        val ACCOUNT_ROLES = listOf(
            AppRoles.ROLE_STUDENT,
            AppRoles.ROLE_TEACHER,
            AppRoles.ROLE_MANAGER,
            AppRoles.ROLE_OWNER
        )

        private fun usernameKey(role: Int) = "auth_username_role_$role"
        private fun passwordHashKey(role: Int) = "auth_password_hash_role_$role"

        fun defaultUsername(role: Int): String = when (role) {
            AppRoles.ROLE_STUDENT -> DEFAULT_STUDENT_USERNAME
            AppRoles.ROLE_TEACHER -> DEFAULT_TEACHER_USERNAME
            AppRoles.ROLE_MANAGER -> DEFAULT_MANAGER_USERNAME
            else -> DEFAULT_OWNER_USERNAME
        }

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
