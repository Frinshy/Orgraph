package de.frinshy.orgraph.data.config

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val isDarkTheme: Boolean = false,
    val windowWidth: Int = 1280,
    val windowHeight: Int = 720,
    val lastViewMode: String = "LIST", // LIST or MINDMAP
    val exportDirectory: String = "",
    val appVersion: String = "1.0.0"
)