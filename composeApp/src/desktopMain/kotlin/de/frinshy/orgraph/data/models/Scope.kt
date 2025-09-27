package de.frinshy.orgraph.data.models

import androidx.compose.ui.graphics.Color
import de.frinshy.orgraph.data.serialization.ColorSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Scope(
    val id: String,
    val name: String,
    val subtitle: String = "", // subtitle like "Core Subject", "Elective", "AP Course", etc.
    val backgroundImage: String = "", // path to background image file
    @Serializable(with = ColorSerializer::class)
    val color: Color,
    val description: String = ""
)