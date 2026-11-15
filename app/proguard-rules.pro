# قوانین فشرده‌سازی (R8) برای نسخه نهایی
# این قوانین از کار افتادن بخش‌های مهم برنامه هنگام فشرده‌سازی جلوگیری می‌کنند

# پایگاه داده Room: کلاس اصلی دیتابیس حفظ شود
-keep class * extends androidx.room.RoomDatabase

# مدل‌های داده Room (جدول‌ها) — نام فیلدها برای کوئری‌ها لازم است
-keepclassmembers class * {
    @androidx.room.<methods> <fields>;
}

# تابع اصلی‌های Kotlin
-keepclassmembers class * {
    public static void main(java.lang.String[]);
}
