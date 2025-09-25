package de.frinshy.orgraph.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.data.models.Scope
import de.frinshy.orgraph.data.models.Teacher
import de.frinshy.orgraph.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.util.prefs.Preferences

class OrgraphViewModel : ViewModel() {
    private val repository = SchoolRepository()
    private val prefs = Preferences.userNodeForPackage(OrgraphViewModel::class.java)
    
    val school: StateFlow<School> = repository.school
    
    private val _selectedView = MutableStateFlow(ViewMode.LIST)
    val selectedView: StateFlow<ViewMode> = _selectedView.asStateFlow()
    
    private val _showAddTeacherDialog = MutableStateFlow(false)
    val showAddTeacherDialog: StateFlow<Boolean> = _showAddTeacherDialog.asStateFlow()
    
    private val _showEditTeacherDialog = MutableStateFlow(false)
    val showEditTeacherDialog: StateFlow<Boolean> = _showEditTeacherDialog.asStateFlow()
    
    private val _showAddScopeDialog = MutableStateFlow(false)
    val showAddScopeDialog: StateFlow<Boolean> = _showAddScopeDialog.asStateFlow()
    
    private val _selectedTeacher = MutableStateFlow<Teacher?>(null)
    val selectedTeacher: StateFlow<Teacher?> = _selectedTeacher.asStateFlow()
    
    // Theme management
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
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
    
    fun showAddScopeDialog() {
        _showAddScopeDialog.value = true
    }
    
    fun hideAddScopeDialog() {
        _showAddScopeDialog.value = false
    }
    
    fun selectTeacher(teacher: Teacher?) {
        _selectedTeacher.value = teacher
    }
    
    fun addTeacher(
        name: String,
        email: String,
        phone: String,
        scopes: List<Scope>,
        description: String,
        experience: Int
    ) {
        val teacher = Teacher(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            phone = phone,
            scopes = scopes,
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
    
    fun addScope(name: String, color: Color, description: String) {
        val scope = Scope(
            id = UUID.randomUUID().toString(),
            name = name,
            color = color,
            description = description
        )
        
        viewModelScope.launch {
            repository.addScope(scope)
            hideAddScopeDialog()
        }
    }
    
    fun removeScope(scopeId: String) {
        viewModelScope.launch {
            repository.removeScope(scopeId)
        }
    }
    
    // Theme management functions
    fun toggleTheme() {
        val newTheme = !_isDarkTheme.value
        _isDarkTheme.value = newTheme
        prefs.putBoolean("dark_theme", newTheme)
        try {
            prefs.flush()
        } catch (e: Exception) {
            // Silently ignore flush errors
        }
    }
    
    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        prefs.putBoolean("dark_theme", isDark)
        try {
            prefs.flush()
        } catch (e: Exception) {
            // Silently ignore flush errors
        }
    }
    
    fun updateScope(scope: Scope) {
        viewModelScope.launch {
            repository.updateScope(scope)
        }
    }
}