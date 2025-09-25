package de.frinshy.orgraph.data.models

import androidx.compose.ui.graphics.Color
import de.frinshy.orgraph.data.serialization.ColorSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class OrgraphBackup(
    val version: String = "1.0.0",
    val exportDate: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val school: SerializableSchool
)

@Serializable
data class SerializableSchool(
    val id: String,
    val name: String,
    val address: String = "",
    val teachers: List<SerializableTeacher> = emptyList(),
    val scopes: List<SerializableScope> = emptyList()
)

@Serializable
data class SerializableTeacher(
    val id: String,
    val name: String,
    val subtitle: String = "",
    val backgroundImage: String = "",
    val email: String = "",
    val phone: String = "",
    val scopes: List<SerializableScope> = emptyList(),
    val description: String = "",
    val experience: Int = 0
)

@Serializable
data class SerializableScope(
    val id: String,
    val name: String,
    val subtitle: String = "",
    val backgroundImage: String = "",
    @Serializable(with = ColorSerializer::class)
    val color: Color,
    val description: String = ""
)