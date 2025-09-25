package de.frinshy.orgraph.data.models

data class School(
    val id: String,
    val name: String,
    val address: String = "",
    val teachers: List<Teacher> = emptyList(),
    val subjects: List<Subject> = Subject.defaultSubjects()
) {
    fun getTeachersBySubject(subjectId: String): List<Teacher> {
        return teachers.filter { teacher -> 
            teacher.subjects.any { it.id == subjectId }
        }
    }
    
    fun getSubjectsWithTeachers(): List<Pair<Subject, List<Teacher>>> {
        return subjects.map { subject ->
            subject to getTeachersBySubject(subject.id)
        }
    }
}