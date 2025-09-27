package de.frinshy.orgraph.data.serialization

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer for Compose Color that safely handles color values
 * Stores colors as hex strings to avoid overflow issues with Long values
 */
object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        try {
            // First validate the color by trying to convert to ARGB
            val argb = value.toArgb()
            val hexString = String.format("#%08X", argb)
            encoder.encodeString(hexString)
        } catch (e: Exception) {
            // If the color is invalid, serialize a safe default color
            println("Warning: Invalid color during serialization, using default purple. Error: ${e.message}")
            encoder.encodeString("#FF6750A4") // Default purple
        }
    }

    override fun deserialize(decoder: Decoder): Color {
        val hexString = decoder.decodeString()
        return try {
            // Parse hex string back to Color
            val colorInt = when {
                hexString.startsWith("#") -> {
                    // Remove # and parse as hex
                    val hex = hexString.substring(1)
                    if (hex.length == 8) {
                        // ARGB format
                        hex.toLong(16).toInt()
                    } else if (hex.length == 6) {
                        // RGB format - add full alpha
                        (0xFF000000L or hex.toLong(16)).toInt()
                    } else {
                        throw IllegalArgumentException("Invalid hex color format: $hexString")
                    }
                }
                else -> {
                    // Try to parse as direct integer (for backward compatibility)
                    hexString.toIntOrNull() ?: throw IllegalArgumentException("Invalid color format: $hexString")
                }
            }
            
            // Create color and immediately validate it
            val color = Color(colorInt)
            
            // Test the color is valid by attempting to use it
            @Suppress("UNUSED_VARIABLE") 
            val testArgb = color.toArgb() // This will fail if color is invalid
            
            color
        } catch (e: Exception) {
            println("Warning: Failed to parse color '$hexString', using default purple. Error: ${e.message}")
            Color(0xFF6750A4) // Default purple color
        }
    }
}

/**
 * Alternative Long-based serializer for backward compatibility
 */
object ColorLongSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = 
        PrimitiveSerialDescriptor("Color", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Color) {
        // Convert to ARGB int and then to Long
        val argb = value.toArgb()
        encoder.encodeLong(argb.toLong())
    }

    override fun deserialize(decoder: Decoder): Color {
        val longValue = decoder.decodeLong()
        return try {
            // Validate the color value is within valid ARGB range
            val colorValue = when {
                longValue == 0L -> 0xFF6750A4UL // Default purple if zero
                longValue < 0 -> {
                    // For negative values, interpret as unsigned
                    val unsignedValue = longValue.toULong()
                    // Validate the unsigned value is in valid ARGB range
                    if (unsignedValue > 0xFFFFFFFFUL) {
                        println("Warning: Color value $longValue ($unsignedValue) is out of valid ARGB range, using default purple")
                        0xFF6750A4UL
                    } else {
                        unsignedValue
                    }
                }
                else -> {
                    // Positive values
                    val unsignedValue = longValue.toULong()
                    if (unsignedValue > 0xFFFFFFFFUL) {
                        println("Warning: Color value $longValue is out of valid ARGB range, using default purple")
                        0xFF6750A4UL
                    } else {
                        unsignedValue
                    }
                }
            }
            
            // Create the color and test if it's valid
            val testColor = Color(colorValue)
            
            // Test color validity by checking if we can convert to ARGB without error
            @Suppress("UNUSED_VARIABLE")
            val argb = testColor.toArgb()
            
            testColor
        } catch (e: Exception) {
            println("Warning: Failed to create valid color from value $longValue. Error: ${e.message}. Using default purple")
            Color(0xFF6750A4UL) // Fallback to default purple
        }
    }
}