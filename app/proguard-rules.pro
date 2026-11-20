# 1. Giữ thông tin dòng code để đọc stacktrace nếu app lỗi trên bản Release
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 2. Xóa toàn bộ Log.d, Log.v, Log.i khi build Release (chống hacker đọc log)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
}

# 3. Giữ nguyên tên các Model / Request / Response dùng cho Gson & Retrofit
# (Bắt buộc giữ tên biến để map đúng key JSON từ Backend)
-keep class com.danghung.elearning.model.** { *; }
-keep class com.danghung.elearning.api.res.** { *; }
-keep class com.danghung.elearning.api.req.** { *; }

# Nếu bạn có package lưu Entity của Room Database (ví dụ: database/entity), giữ lại:
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# 4. Cấu hình chuẩn cho Retrofit 2 & OkHttp 4
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-dontwarn okhttp3.**
-dontwarn okio.**

# 5. Cấu hình chuẩn cho Gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 6. Cấu hình cho Jsoup & Glide
-keeppackagenames org.jsoup.nodes
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}