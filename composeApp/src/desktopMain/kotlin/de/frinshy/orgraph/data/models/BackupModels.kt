package de.frinshy.orgraph.data.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class OrgraphBackup(
    val version: String = "1.0.0",
    val exportDate: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val school: School
)