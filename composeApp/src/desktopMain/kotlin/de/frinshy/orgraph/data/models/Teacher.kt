package de.frinshy.orgraph.data.models

data class Teacher(
    val id: String,
    val name: String,
    val email: String = "",
    val phone: String = "",
    val subjects: List<Subject> = emptyList(),
    val description: String = "",
    val experience: Int = 0 // years of experience
)
