package de.frinshy.orgraph.data.local

import androidx.compose.ui.graphics.Color
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.data.models.Subject
import de.frinshy.orgraph.data.models.Teacher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.prefs.Preferences

@Serializable
data class SerializableSubject(
    val id: String,
    val name: String,
    val color: Long,
    val description: String = ""
)

@Serializable
data class SerializableTeacher(
    val id: String,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val subjects: List<SerializableSubject> = emptyList(),
    val description: String = "",
    val experience: Int = 0
)

@Serializable
data class SerializableSchool(
    val id: String,
    val name: String,
    val address: String = "",
    val teachers: List<SerializableTeacher> = emptyList(),
    val subjects: List<SerializableSubject> = emptyList()
)

class LocalDataManager {
    private val prefs = Preferences.userNodeForPackage(LocalDataManager::class.java)
    private val json = Json { prettyPrint = true }
    private val schoolKey = "orgraph_school_data"
    
    suspend fun saveSchool(school: School) {
        withContext(Dispatchers.IO) {
            try {
                val serializableSchool = school.toSerializable()
                val jsonString = json.encodeToString(serializableSchool)
                prefs.put(schoolKey, jsonString)
                prefs.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun loadSchool(): School? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = prefs.get(schoolKey, null)
                if (jsonString != null) {
                    val serializableSchool = json.decodeFromString<SerializableSchool>(jsonString)
                    serializableSchool.toSchool()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    suspend fun clearData() {
        withContext(Dispatchers.IO) {
            prefs.remove(schoolKey)
            prefs.flush()
        }
    }
}

// Extension functions for conversion
private fun School.toSerializable(): SerializableSchool {
    return SerializableSchool(
        id = id,
        name = name,
        address = address,
        teachers = teachers.map { it.toSerializable() },
        subjects = subjects.map { it.toSerializable() }
    )
}

private fun Teacher.toSerializable(): SerializableTeacher {
    return SerializableTeacher(
        id = id,
        name = name,
        email = email,
        phone = phone,
        subjects = subjects.map { it.toSerializable() },
        description = description,
        experience = experience
    )
}

private fun Subject.toSerializable(): SerializableSubject {
    return SerializableSubject(
        id = id,
        name = name,
        color = color.value.toLong(),
        description = description
    )
}

private fun SerializableSchool.toSchool(): School {
    return School(
        id = id,
        name = name,
        address = address,
        teachers = teachers.map { it.toTeacher() },
        subjects = subjects.map { it.toSubject() }
    )
}

private fun SerializableTeacher.toTeacher(): Teacher {
    return Teacher(
        id = id,
        name = name,
        email = email,
        phone = phone,
        subjects = subjects.map { it.toSubject() },
        description = description,
        experience = experience
    )
}

private fun SerializableSubject.toSubject(): Subject {
    return Subject(
        id = id,
        name = name,
        color = Color(color.toULong()),
        description = description
    )
}