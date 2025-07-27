# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Keep Gson classes and annotations
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Gson specific rules
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep our data classes for Gson serialization
-keep class com.tk.daystrack.Event { *; }
-keep class com.tk.daystrack.EventInstance { *; }

# Keep Compose related classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep ViewModel and LiveData
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }

# Keep Activity and Fragment classes
-keep public class * extends android.app.Activity
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.activity.ComponentActivity

# Keep SharedPreferences related classes
-keep class android.content.SharedPreferences { *; }

# Keep UUID class for Event ID generation
-keep class java.util.UUID { *; }

# Keep LocalDate for date handling
-keep class java.time.LocalDate { *; }

# Keep reorderable library
-keep class org.burnoutcrew.reorderable.** { *; }

# Keep enum classes
-keep enum com.tk.daystrack.SortOption { *; }
-keep enum com.tk.daystrack.FontSize { *; }

# Keep string resources
-keep class com.tk.daystrack.R$string { *; }