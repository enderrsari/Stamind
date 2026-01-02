# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# =====================
# STAMIND APP PROGUARD RULES
# =====================

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# =====================
# DATA MODELS (Firestore serialization)
# =====================
-keep class com.stamindapp.stamind.database.** { *; }
-keep class com.stamindapp.stamind.engine.JournalAnalysis { *; }
-keep class com.stamindapp.stamind.engine.SuggestionItem { *; }

# =====================
# FIREBASE
# =====================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase AI
-keep class com.google.firebase.ai.** { *; }
-dontwarn com.google.firebase.ai.**

# Firebase Firestore
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}

# =====================
# GOOGLE PLAY BILLING
# =====================
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**
-keep class com.android.vending.billing.** { *; }

# =====================
# KOTLINX SERIALIZATION
# =====================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.stamindapp.stamind.**$$serializer { *; }
-keepclassmembers class com.stamindapp.stamind.** {
    *** Companion;
}
-keepclasseswithmembers class com.stamindapp.stamind.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# =====================
# OKHTTP
# =====================
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# =====================
# COMPOSE
# =====================
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# =====================
# CUSTOM EXCEPTIONS
# =====================
-keep class com.stamindapp.stamind.engine.AnalysisException { *; }
-keep class com.stamindapp.stamind.engine.ChatException { *; }
-keep class com.stamindapp.stamind.engine.InsightException { *; }
-keep class com.stamindapp.stamind.engine.RateLimitException { *; }