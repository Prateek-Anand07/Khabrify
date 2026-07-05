# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# =====================================================================
# Moshi ProGuard Rules
# =====================================================================
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Moshi generic types and network adapters
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }

# Keep the Kotlin metadata that KotlinJsonAdapterFactory relies on
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep class kotlin.Metadata { *; }


# =====================================================================
# Hilt & Firebase Service ProGuard Rules (PASTE THESE AT THE BOTTOM)
# =====================================================================
# Keep public classes extending FirebaseMessagingService from being stripped or renamed
-keep public class * extends com.google.firebase.messaging.FirebaseMessagingService { public <init>(); }

# Keep Hilt generated classes for Android Entry Points and Services
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep class * implements dagger.hilt.internal.aggregatedroot.AggregatedRoot { *; }
-keep class * extends android.app.Service { *; }

# Prevent Hilt injected fields (like your notificationDao) from being stripped
-keepclassmembers class * {
    @javax.inject.Inject <fields>;
}

# Protect AboutLibraries models and UI from aggressive R8 optimization
-keep class com.mikepenz.aboutlibraries.** { *; }
-keepclassmembers class com.mikepenz.aboutlibraries.** { *; }
-dontwarn com.mikepenz.aboutlibraries.**

# Protect the generated raw resource ID
-keepclassmembers class **.R$raw {
    public static final int aboutlibraries;
}