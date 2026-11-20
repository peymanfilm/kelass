package com.example.classschedule.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * دروازه‌ی احراز هویت برنامه «کلاس چی» — کاملاً آفلاین و روی خود گوشی:
 *
 * - حساب‌ها مثل وب‌سایت در جدول کاربران نگه داشته می‌شوند؛ مدیر می‌تواند هر تعداد
 *   حساب دانش‌آموز و معلم بسازد و مالک برند MTM Kode علاوه بر آن‌ها، حساب مدیر را هم
 *   می‌سازد و نام کاربری و رمز همه را عوض می‌کند.
 * - حساب‌های پیش‌فرض (student1 / teacher1 / manager / owner) با رمز «۱۲۳۴» در اولین اجرا
 *   ساخته می‌شوند و حساب‌های شخصی‌سازی‌شده‌ی نسخه‌های قبل (در SharedPreferences) به جدول
 *   منتقل می‌شوند تا هیچ رمز عوض‌شده‌ای از بین نرود.
 * - رمز عبور هرگز به صورت خام ذخیره نمی‌شود؛ فقط هش SHA-256 آن نگه داشته می‌شود
 * - حساب قدیمی نسخه‌های قبل (admin) به حساب مالک برند منتقل می‌شود
 */
class AuthRepository(
    context: Context,
    private val userDao: UserDao
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** آیا کاربر همین الان وارد شده است؟ (با خروج، صفحه ورود دوباره باز می‌شود) */
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_LOGGED_IN, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /** فهرست زنده‌ی همه‌ی حساب‌ها — صفحه ورود و تب «حساب‌ها» به آن گوش می‌دهند */
    val usersFlow: Flow<List<AppUser>> = userDao.observeAll()

    init {
        // ساخت حساب‌های پیش‌فرض و انتقال حساب‌های قدیمی — یک‌بار در اولین اجرا
        runBlocking { withContext(Dispatchers.IO) { seedUsersIfNeeded() } }
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

    /**
     * اگر جدول کاربران خالی باشد، حساب هر نقش از تنظیمات نسخه‌ی قبل (نام کاربری و رمز
     * احتمالاً شخصی‌سازی‌شده) یا از مقادیر پیش‌فرض ساخته می‌شود.
     */
    private suspend fun seedUsersIfNeeded() {
        migrateLegacyAccountIfNeeded()
        if (userDao.count() > 0) return
        for (role in ACCOUNT_ROLES) {
            val username = prefs.getString(usernameKey(role), null) ?: defaultUsername(role)
            val passwordHash = prefs.getString(passwordHashKey(role), null)
                ?: hash(DEFAULT_PASSWORD)
            userDao.insert(
                AppUser(
                    name = defaultName(role, username),
                    username = username,
                    passwordHash = passwordHash,
                    role = role
                )
            )
        }
        // کلیدهای قدیمی نقش‌محور دیگر لازم نیستند — از این پس جدول کاربران مرجع است
        val editor = prefs.edit()
        for (role in ACCOUNT_ROLES) {
            editor.remove(usernameKey(role)).remove(passwordHashKey(role))
        }
        editor.apply()
    }

    /** نام نمایشی پیش‌فرض هر حساب — هم‌تراز با حساب‌های نمونه‌ی وب‌سایت */
    private fun defaultName(role: Int, username: String): String = when {
        role == AppRoles.ROLE_OWNER -> "مالک برند MTM Kode"
        username != defaultUsername(role) -> when (role) {
            AppRoles.ROLE_TEACHER -> "معلم"
            AppRoles.ROLE_MANAGER -> "مدیر"
            else -> "دانش‌آموز"
        }
        role == AppRoles.ROLE_MANAGER -> "مدیر مدرسه"
        role == AppRoles.ROLE_TEACHER -> "معلم"
        else -> "دانش‌آموز"
    }

    // ------------------------------ ورود و خروج ------------------------------

    /**
     * بررسی نام کاربری و رمز عبور در جدول کاربران:
     * اگر درست بود، وضعیت «واردشده» ذخیره و نقشِ حساب برگردانده می‌شود؛
     * در غیر این صورت ROLE_NONE برمی‌گردد (یعنی نام کاربری یا رمز اشتباه است).
     */
    fun login(username: String, password: String): Int {
        val cleanUser = username.trim()
        if (cleanUser.isEmpty() || password.isEmpty()) return AppRoles.ROLE_NONE

        val user = runBlocking {
            withContext(Dispatchers.IO) { userDao.findByUsername(cleanUser) }
        } ?: return AppRoles.ROLE_NONE

        if (hash(password) != user.passwordHash) return AppRoles.ROLE_NONE

        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putInt(KEY_CURRENT_USER_ID, user.id)
            .apply()
        _isLoggedIn.update { true }
        return user.role
    }

    /** خروج از حساب: وضعیت ورود پاک می‌شود و صفحه ورود دوباره باز می‌شود (داده‌ها دست‌نخورده می‌مانند) */
    fun logout() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .putInt(KEY_CURRENT_USER_ID, -1)
            .apply()
        _isLoggedIn.update { false }
    }

    /** شناسه‌ی حساب واردشده — برای پیدا کردن ردیف او در فهرست کاربران */
    fun currentUserId(): Int = prefs.getInt(KEY_CURRENT_USER_ID, -1)

    /** نام کاربری فعلی اولین حساب یک نقش — در تنظیمات مالک برند استفاده می‌شود */
    fun usernameOf(role: Int): String {
        val user = runBlocking {
            withContext(Dispatchers.IO) { userDao.firstByRole(role) }
        }
        return user?.username ?: defaultUsername(role)
    }

    // ------------------------------ قوانین دسترسی ------------------------------

    /**
     * آیا این نقش اجازه‌ی تغییر حساب نقش مقصد را دارد؟
     * - مدیر: فقط حساب‌های دانش‌آموز و معلم
     * - مالک برند MTM Kode: حساب‌های دانش‌آموز، معلم و مدیر (حساب خودش از تنظیمات تغییر می‌کند)
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

    /** آیا این نقش اجازه‌ی ساختن حساب تازه‌ی نقش داده‌شده را دارد؟ */
    fun canAddUser(callerRole: Int, role: Int): Boolean = canEditAccount(callerRole, role)

    // ------------------------------ مدیریت حساب‌ها ------------------------------

    /**
     * تغییر نام، نام کاربری و رمز عبور یک حساب (مدیریت حساب‌ها):
     * - رمز جدید خالی باشد یعنی رمز فعلی دست نمی‌خورد
     * - نتیجه به شکل کد بازگردانده می‌شود تا پیام فارسی دقیق در UI نمایش داده شود
     */
    fun updateUser(
        callerRole: Int,
        targetUserId: Int,
        newName: String,
        newUsername: String,
        newPassword: String
    ): UpdateAccountResult {
        val user = runBlocking {
            withContext(Dispatchers.IO) { userDao.byId(targetUserId) }
        } ?: return UpdateAccountResult.NOT_FOUND

        if (!canEditAccount(callerRole, user.role)) return UpdateAccountResult.NO_PERMISSION

        val cleanUser = newUsername.trim()
        val cleanName = newName.trim()
        if (cleanUser.isEmpty() ||
            cleanUser.length < MIN_USERNAME_LENGTH ||
            cleanUser.any { it.isWhitespace() }
        ) {
            return UpdateAccountResult.INVALID_USERNAME
        }

        if (cleanUser != user.username) {
            val taken = runBlocking {
                withContext(Dispatchers.IO) { userDao.findByUsername(cleanUser) }
            }
            if (taken != null && taken.id != user.id) return UpdateAccountResult.USERNAME_TAKEN
        }

        val passwordChanged = newPassword.isNotEmpty()
        if (passwordChanged && newPassword.length < MIN_PASSWORD_LENGTH) {
            return UpdateAccountResult.PASSWORD_TOO_SHORT
        }
        if (!passwordChanged && cleanUser == user.username && cleanName == user.name) {
            return UpdateAccountResult.NOTHING_CHANGED
        }

        val updated = user.copy(
            name = cleanName.ifBlank { user.name },
            username = cleanUser,
            passwordHash = if (passwordChanged) hash(newPassword) else user.passwordHash
        )
        runBlocking { withContext(Dispatchers.IO) { userDao.update(updated) } }
        return UpdateAccountResult.SUCCESS
    }

    enum class UpdateAccountResult {
        SUCCESS,           // حساب با موفقیت به‌روزرسانی شد
        NO_PERMISSION,     // این نقش اجازه‌ی تغییر این حساب را ندارد
        NOT_FOUND,         // حساب پیدا نشد
        INVALID_USERNAME,  // نام کاربری خالی، کوتاه یا دارای فاصله است
        USERNAME_TAKEN,    // این نام کاربری برای حساب دیگری گرفته شده است
        PASSWORD_TOO_SHORT,// رمز جدید خیلی کوتاه است
        NOTHING_CHANGED    // هیچ چیزی عوض نشده است
    }

    /**
     * ساختن حساب تازه (مثل تب «کاربران» وب‌سایت):
     * - مدیر: حساب دانش‌آموز و معلم
     * - مالک برند: حساب دانش‌آموز، معلم و مدیر
     */
    fun addUser(
        callerRole: Int,
        role: Int,
        name: String,
        username: String,
        password: String
    ): AddUserResult {
        if (!canAddUser(callerRole, role)) return AddUserResult.NO_PERMISSION

        val cleanUser = username.trim()
        val cleanName = name.trim().ifBlank { defaultName(role, "") }
        if (cleanUser.isEmpty() ||
            cleanUser.length < MIN_USERNAME_LENGTH ||
            cleanUser.any { it.isWhitespace() }
        ) {
            return AddUserResult.INVALID_USERNAME
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return AddUserResult.PASSWORD_TOO_SHORT
        }
        val taken = runBlocking {
            withContext(Dispatchers.IO) { userDao.findByUsername(cleanUser) }
        }
        if (taken != null) return AddUserResult.USERNAME_TAKEN

        runBlocking {
            withContext(Dispatchers.IO) {
                userDao.insert(
                    AppUser(
                        name = cleanName,
                        username = cleanUser,
                        passwordHash = hash(password),
                        role = role
                    )
                )
            }
        }
        return AddUserResult.SUCCESS
    }

    enum class AddUserResult {
        SUCCESS,           // حساب ساخته شد
        NO_PERMISSION,     // این نقش اجازه‌ی ساختن این نوع حساب را ندارد
        INVALID_USERNAME,  // نام کاربری خالی، کوتاه یا دارای فاصله است
        USERNAME_TAKEN,    // این نام کاربری قبلاً گرفته شده است
        PASSWORD_TOO_SHORT // رمز خیلی کوتاه است
    }

    /**
     * حذف یک حساب — با همان قوانین دسترسی تغییر حساب‌ها؛
     * حذف حسابی که با آن وارد شده‌ایم مجاز نیست.
     */
    fun deleteUser(callerRole: Int, targetUserId: Int): DeleteUserResult {
        val user = runBlocking {
            withContext(Dispatchers.IO) { userDao.byId(targetUserId) }
        } ?: return DeleteUserResult.NOT_FOUND

        if (!canEditAccount(callerRole, user.role)) return DeleteUserResult.NO_PERMISSION
        if (targetUserId == currentUserId()) return DeleteUserResult.CANNOT_DELETE_SELF

        runBlocking { withContext(Dispatchers.IO) { userDao.delete(user) } }
        return DeleteUserResult.SUCCESS
    }

    enum class DeleteUserResult {
        SUCCESS,            // حساب حذف شد
        NO_PERMISSION,      // این نقش اجازه‌ی حذف این حساب را ندارد
        NOT_FOUND,          // حساب پیدا نشد
        CANNOT_DELETE_SELF  // نمی‌توان حسابی را حذف کرد که با آن وارد شده‌ایم
    }

    // ------------------------------ حساب خودِ مالک برند ------------------------------

    /**
     * تغییر نام کاربری حساب خودِ مالک برند — با تأیید رمز فعلی.
     */
    fun changeSelfUsername(currentPassword: String, newUsername: String): SelfUsernameResult {
        val owner = runBlocking {
            withContext(Dispatchers.IO) { userDao.firstByRole(AppRoles.ROLE_OWNER) }
        } ?: return SelfUsernameResult.INVALID_USERNAME

        if (hash(currentPassword) != owner.passwordHash) {
            return SelfUsernameResult.WRONG_PASSWORD
        }
        val cleanUser = newUsername.trim()
        if (cleanUser.isEmpty() ||
            cleanUser.length < MIN_USERNAME_LENGTH ||
            cleanUser.any { it.isWhitespace() }
        ) {
            return SelfUsernameResult.INVALID_USERNAME
        }
        val taken = runBlocking {
            withContext(Dispatchers.IO) { userDao.findByUsername(cleanUser) }
        }
        if (taken != null && taken.id != owner.id) return SelfUsernameResult.USERNAME_TAKEN

        runBlocking { withContext(Dispatchers.IO) { userDao.update(owner.copy(username = cleanUser)) } }
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
        val owner = runBlocking {
            withContext(Dispatchers.IO) { userDao.firstByRole(AppRoles.ROLE_OWNER) }
        } ?: return ChangePasswordResult.WRONG_CURRENT

        if (hash(currentPassword) != owner.passwordHash) {
            return ChangePasswordResult.WRONG_CURRENT
        }
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            return ChangePasswordResult.TOO_SHORT
        }
        if (newPassword == currentPassword) {
            return ChangePasswordResult.SAME_AS_CURRENT
        }
        runBlocking {
            withContext(Dispatchers.IO) {
                userDao.update(owner.copy(passwordHash = hash(newPassword)))
            }
        }
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
        private const val KEY_CURRENT_USER_ID = "auth_current_user_id"

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
