package de.frinshy.orgraph.data.models

import kotlinx.serialization.Serializable

@Serializable
data class School(
    val id: String,
    val name: String,
    val backgroundImage: String = "", // path to background image file
    val teachers: List<Teacher> = emptyList(),
    val scopes: List<Scope> = emptyList()
) {
    fun getTeachersByScope(scopeId: String): List<Teacher> {
        return teachers.filter { teacher -> 
            teacher.scopes.any { it.id == scopeId }
        }
    }
    
    fun getScopesWithTeachers(): List<Pair<Scope, List<Teacher>>> {
        return scopes.map { scope ->
            scope to getTeachersByScope(scope.id)
        }
    }
}