package de.frinshy.orgraph.data.local

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import de.frinshy.orgraph.data.config.ConfigManager
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.data.models.Scope
import de.frinshy.orgraph.data.models.Teacher
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SerializableScope(
    val id: String, val name: String, val color: Long, val description: String = ""
)

@Serializable
data class SerializableTeacher(
    val id: String,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val scopes: List<SerializableScope> = emptyList(),
    val subjects: List<SerializableScope> = emptyList(), // For backwards compatibility
    val description: String = "",
    val experience: Int = 0
)

@Serializable
data class SerializableSchool(
    val id: String,
    val name: String,
    val address: String = "",
    val teachers: List<SerializableTeacher> = emptyList(),
    val scopes: List<SerializableScope> = emptyList(),
    val subjects: List<SerializableScope> = emptyList() // For backwards compatibility
)

class LocalDataManager {
    private val configManager = ConfigManager()
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    private val schoolFileName = "school_data.json"

    suspend fun saveSchool(school: School) {
        try {
            val serializableSchool = school.toSerializable()
            configManager.saveConfig(schoolFileName, serializableSchool, SerializableSchool.serializer())
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to save school data: ${e.message}")
        }
    }

    suspend fun loadSchool(): School? {
        return try {
            val serializableSchool = configManager.loadConfig(schoolFileName, SerializableSchool.serializer())
            serializableSchool?.toSchool()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to load school data: ${e.message}")
            null
        }
    }

    suspend fun clearData() {
        try {
            configManager.deleteConfig(schoolFileName)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to clear school data: ${e.message}")
        }
    }
    
    fun getDataDirectory(): String = configManager.getConfigDirectory()
}

// Extension functions for conversion
private fun School.toSerializable(): SerializableSchool {
    return SerializableSchool(
        id = id,
        name = name,
        address = address,
        teachers = teachers.map { it.toSerializable() },
        scopes = scopes.map { it.toSerializable() })
}

private fun Teacher.toSerializable(): SerializableTeacher {
    return SerializableTeacher(
        id = id,
        name = name,
        email = email,
        phone = phone,
        scopes = scopes.map { it.toSerializable() },
        description = description,
        experience = experience
    )
}

private fun Scope.toSerializable(): SerializableScope {
    return SerializableScope(
        id = id, name = name, color = color.value.toLong(), description = description
    )
}

private fun SerializableSchool.toSchool(): School {
    // Migration: use scopes if available, otherwise migrate from subjects
    val finalScopes = if (scopes.isNotEmpty()) scopes else subjects
    
    return School(
        id = id,
        name = name,
        address = address,
        teachers = teachers.map { it.toTeacher() },
        scopes = finalScopes.map { it.toScope() }
    )
}

private fun SerializableTeacher.toTeacher(): Teacher {
    // Migration: use scopes if available, otherwise migrate from subjects
    val finalScopes = if (scopes.isNotEmpty()) scopes else subjects
    
    return Teacher(
        id = id,
        name = name,
        subtitle = "", // LocalDataManager doesn't store subtitles
        backgroundImage = "", // LocalDataManager doesn't store background images
        email = email,
        phone = phone,
        scopes = finalScopes.map { it.toScope() },
        description = description,
        experience = experience
    )
}

private fun SerializableScope.toScope(): Scope {
    // Safe color conversion with comprehensive validation
    val safeColor = try {
        // First validate the color value range
        val colorValue = when {
            color == 0L -> 0xFF6750A4UL // Default purple if zero
            color < 0 -> {
                // For negative values, interpret as unsigned
                val unsignedValue = color.toULong()
                // Validate the unsigned value is in valid ARGB range
                if (unsignedValue > 0xFFFFFFFFUL) {
                    println("Warning: Color value $color ($unsignedValue) for scope $name is out of valid ARGB range, using default purple")
                    0xFF6750A4UL
                } else {
                    unsignedValue
                }
            }
            else -> {
                // Positive values
                val unsignedValue = color.toULong()
                if (unsignedValue > 0xFFFFFFFFUL) {
                    println("Warning: Color value $color for scope $name is out of valid ARGB range, using default purple")
                    0xFF6750A4UL
                } else {
                    unsignedValue
                }
            }
        }
        
        // Create the color and test if it's valid by accessing properties
        val testColor = Color(colorValue)
        
        // Test color validity by checking if we can convert to ARGB without error
        @Suppress("UNUSED_VARIABLE")
        val argb = testColor.toArgb()
        
        testColor
    } catch (e: Exception) {
        println("Warning: Failed to create valid color from value $color for scope $name. Error: ${e.message}. Using default purple")
        Color(0xFF6750A4UL) // Fallback to default purple
    }
    
    return Scope(
        id = id, 
        name = name, 
        subtitle = "", // LocalDataManager doesn't store subtitles
        backgroundImage = "", // LocalDataManager doesn't store background images
        color = safeColor, 
        description = description
    )
}