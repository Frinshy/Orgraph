package de.frinshy.orgraph.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.data.models.Subject
import de.frinshy.orgraph.data.models.Teacher
import de.frinshy.orgraph.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class OrgraphViewModel : ViewModel() {
    private val repository = SchoolRepository()
    
    val school: StateFlow<School> = repository.school
    
    private val _selectedView = MutableStateFlow(ViewMode.LIST)
    val selectedView: StateFlow<ViewMode> = _selectedView.asStateFlow()
    
    private val _showAddTeacherDialog = MutableStateFlow(false)
    val showAddTeacherDialog: StateFlow<Boolean> = _showAddTeacherDialog.asStateFlow()
    
    private val _showEditTeacherDialog = MutableStateFlow(false)
    val showEditTeacherDialog: StateFlow<Boolean> = _showEditTeacherDialog.asStateFlow()
    
    private val _selectedTeacher = MutableStateFlow<Teacher?>(null)
    val selectedTeacher: StateFlow<Teacher?> = _selectedTeacher.asStateFlow()
    
    init {
        // Initialize data from persistent storage
        viewModelScope.launch {
            repository.initializeData()
        }
    }
    
    enum class ViewMode {
        LIST, MINDMAP
    }
    
    fun switchView(viewMode: ViewMode) {
        _selectedView.value = viewMode
    }
    
    fun showAddTeacherDialog() {
        _showAddTeacherDialog.value = true
    }
    
    fun hideAddTeacherDialog() {
        _showAddTeacherDialog.value = false
    }
    
    fun showEditTeacherDialog(teacher: Teacher) {
        _selectedTeacher.value = teacher
        _showEditTeacherDialog.value = true
    }
    
    fun hideEditTeacherDialog() {
        _selectedTeacher.value = null
        _showEditTeacherDialog.value = false
    }
    
    fun selectTeacher(teacher: Teacher?) {
        _selectedTeacher.value = teacher
    }
    
    fun addTeacher(
        name: String,
        email: String,
        phone: String,
        subjects: List<Subject>,
        description: String,
        experience: Int
    ) {
        val teacher = Teacher(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            phone = phone,
            subjects = subjects,
            description = description,
            experience = experience
        )
        
        viewModelScope.launch {
            repository.addTeacher(teacher)
            hideAddTeacherDialog()
        }
    }
    
    fun removeTeacher(teacherId: String) {
        viewModelScope.launch {
            repository.removeTeacher(teacherId)
        }
    }
    
    fun updateTeacher(teacher: Teacher) {
        viewModelScope.launch {
            repository.updateTeacher(teacher)
            hideEditTeacherDialog()
        }
    }
}