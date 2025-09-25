package de.frinshy.orgraph.data.models

data class Teacher(
    val id: String,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val scopes: List<Scope> = emptyList(),
    val description: String = "",
    val experience: Int = 0 // years of experience
)
