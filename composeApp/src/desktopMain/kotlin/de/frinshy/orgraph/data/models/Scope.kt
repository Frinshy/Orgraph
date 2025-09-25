package de.frinshy.orgraph.data.models

import androidx.compose.ui.graphics.Color

data class Scope(
    val id: String,
    val name: String,
    val color: Color,
    val description: String = ""
)