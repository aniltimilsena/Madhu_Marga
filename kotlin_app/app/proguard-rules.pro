# Add project specific ProGuard rules here.

# Supabase / Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.smarthive.manager.**$$serializer { *; }
-keepclassmembers class com.smarthive.manager.** {
    *** Companion;
}
-keepclasseswithmembers class com.smarthive.manager.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit / Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.smarthive.manager.data.remote.gemini.** { *; }
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Hilt
-dontwarn dagger.hilt.**
