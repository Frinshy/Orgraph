# Suppress missing references
-dontwarn androidx.compose.ui.res.**
-dontwarn com.konyaco.fluent.**

# Keep necessary Compose and Fluent classes
-keep class androidx.compose.** { *; }
-keep class com.konyaco.fluent.** { *; }