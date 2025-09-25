package de.frinshy.orgraph.data.repository

import de.frinshy.orgraph.data.local.LocalDataManager
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.data.models.Subject
import de.frinshy.orgraph.data.models.Teacher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SchoolRepository {
    private val localDataManager = LocalDataManager()
    
    private val _school = MutableStateFlow(
        School(
            id = "1",
            name = "Orgraph Academy",
            address = "123 Education Street"
        )
    )
    val school: StateFlow<School> = _school.asStateFlow()
    
    suspend fun initializeData() {
        val savedSchool = localDataManager.loadSchool()
        if (savedSchool != null) {
            _school.value = savedSchool
        }
    }
    
    suspend fun addTeacher(teacher: Teacher) {
        val currentSchool = _school.value
        val newSchool = currentSchool.copy(
            teachers = currentSchool.teachers + teacher
        )
        _school.value = newSchool
        localDataManager.saveSchool(newSchool)
    }
    
    suspend fun removeTeacher(teacherId: String) {
        val currentSchool = _school.value
        val newSchool = currentSchool.copy(
            teachers = currentSchool.teachers.filter { it.id != teacherId }
        )
        _school.value = newSchool
        localDataManager.saveSchool(newSchool)
    }
    
    suspend fun updateTeacher(teacher: Teacher) {
        val currentSchool = _school.value
        val newSchool = currentSchool.copy(
            teachers = currentSchool.teachers.map { 
                if (it.id == teacher.id) teacher else it 
            }
        )
        _school.value = newSchool
        localDataManager.saveSchool(newSchool)
    }
    
    suspend fun addSubject(subject: Subject) {
        val currentSchool = _school.value
        val newSchool = currentSchool.copy(
            subjects = currentSchool.subjects + subject
        )
        _school.value = newSchool
        localDataManager.saveSchool(newSchool)
    }
    
    suspend fun removeSubject(subjectId: String) {
        val currentSchool = _school.value
        val newSchool = currentSchool.copy(
            subjects = currentSchool.subjects.filter { it.id != subjectId }
        )
        _school.value = newSchool
        localDataManager.saveSchool(newSchool)
    }
    
    suspend fun clearAllData() {
        localDataManager.clearData()
        _school.value = School(
            id = "1",
            name = "Orgraph Academy",
            address = "123 Education Street"
        )
    }
}