package de.frinshy.orgraph.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Professional Color Scheme for Business Applications
// Light theme uses neutral greys and blues for a clean, professional appearance
// Dark theme uses proper Material Design dark colors with blue accents

// Material 3 Expressive Color Scheme - Vibrant and dynamic
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4), // Vibrant purple - M3 Expressive signature
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9DDFF), // Soft purple container
    onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFF625B71), // Rich purple-grey
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8), // Light lavender
    onSecondaryContainer = Color(0xFF1E192B),
    tertiary = Color(0xFF7E5260), // Warm berry
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E3), // Soft pink
    onTertiaryContainer = Color(0xFF30111D),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFDF8FF), // Subtle purple tint
    onBackground = Color(0xFF1C1B1E),
    surface = Color(0xFFFDF8FF), // Consistent with background
    onSurface = Color(0xFF1C1B1E),
    surfaceVariant = Color(0xFFE7E0EB), // Purple-tinted grey
    onSurfaceVariant = Color(0xFF49454E),
    outline = Color(0xFF7A757F),
    outlineVariant = Color(0xFFCAC4CF),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4F0F7),
    inversePrimary = Color(0xFFCFBCFF),
    surfaceDim = Color(0xFFDDD8E0),
    surfaceBright = Color(0xFFFDF8FF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F2FA),
    surfaceContainer = Color(0xFFF1ECF4),
    surfaceContainerHigh = Color(0xFFECE6EE),
    surfaceContainerHighest = Color(0xFFE6E0E8)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCFBCFF), // Bright purple for dark theme
    onPrimary = Color(0xFF22005D),
    primaryContainer = Color(0xFF4F378A),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFFCCC2DC), // Soft lavender
    onSecondary = Color(0xFF1E192B),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8), // Warm pink
    onTertiary = Color(0xFF30111D),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD9E3),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF410002),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF141218), // Rich dark purple
    onBackground = Color(0xFFE6E1E6),
    surface = Color(0xFF141218), // Consistent with background
    onSurface = Color(0xFFE6E1E6),
    surfaceVariant = Color(0xFF49454E),
    onSurfaceVariant = Color(0xFFCAC4CF),
    outline = Color(0xFF948F99),
    outlineVariant = Color(0xFF49454E),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE6E1E6),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF6750A4),
    surfaceDim = Color(0xFF141218),
    surfaceBright = Color(0xFF3B383E),
    surfaceContainerLowest = Color(0xFF0F0D13),
    surfaceContainerLow = Color(0xFF1C1B1E),
    surfaceContainer = Color(0xFF201F22),
    surfaceContainerHigh = Color(0xFF2B292D),
    surfaceContainerHighest = Color(0xFF363438)
)

// Material3 Expressive Typography - Enhanced contrast and expressiveness
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // More expressive weight
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold, // More expressive weight
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // Enhanced weight
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold, // Enhanced weight
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    )
)

// Material3 Expressive Shapes - More rounded and dynamic
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), // Increased from 4dp
    small = RoundedCornerShape(12.dp), // Increased from 8dp
    medium = RoundedCornerShape(16.dp), // Increased from 12dp
    large = RoundedCornerShape(24.dp), // Increased from 16dp
    extraLarge = RoundedCornerShape(32.dp) // Increased from 28dp
)

@Composable
fun OrgraphTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}