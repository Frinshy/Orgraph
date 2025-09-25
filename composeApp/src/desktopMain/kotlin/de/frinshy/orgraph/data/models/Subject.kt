package de.frinshy.orgraph.data.models

import androidx.compose.ui.graphics.Color

data class Subject(
    val id: String,
    val name: String,
    val color: Color,
    val description: String = ""
) {
    companion object {
        fun defaultSubjects() = listOf(
            Subject("1", "English", Color(0xFF6750A4), "English Language and Literature"),
            Subject("2", "German", Color(0xFF006A6B), "German Language and Literature"),
            Subject("3", "Mathematics", Color(0xFF8B5000), "Mathematics and Algebra"),
            Subject("4", "Physics", Color(0xFF904A00), "Physics and Natural Sciences"),
            Subject("5", "Chemistry", Color(0xFF006D3B), "Chemistry and Laboratory Sciences"),
            Subject("6", "Biology", Color(0xFF8E4585), "Biology and Life Sciences"),
            Subject("7", "History", Color(0xFF006783), "History and Social Studies"),
            Subject("8", "Geography", Color(0xFF984061), "Geography and Earth Sciences"),
            Subject("9", "Art", Color(0xFF8B5A2B), "Art and Creative Studies"),
            Subject("10", "Music", Color(0xFF6750A4), "Music and Performance Arts")
        )
    }
}