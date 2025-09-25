package de.frinshy.orgraph.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.frinshy.orgraph.data.config.AppSettings
import de.frinshy.orgraph.data.config.ConfigManager
import de.frinshy.orgraph.data.io.ImportExportManager
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.data.models.Scope
import de.frinshy.orgraph.data.models.Teacher
import de.frinshy.orgraph.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class OrgraphViewModel : ViewModel() {
    private val repository = SchoolRepository()
    private val configManager = ConfigManager()
    private val importExportManager = ImportExportManager()
    
    val school: StateFlow<School> = repository.school
    
    private val _selectedView = MutableStateFlow(ViewMode.LIST)
    val selectedView: StateFlow<ViewMode> = _selectedView.asStateFlow()
    
    private val _showAddTeacherDialog = MutableStateFlow(false)
    val showAddTeacherDialog: StateFlow<Boolean> = _showAddTeacherDialog.asStateFlow()
    
    private val _showEditTeacherDialog = MutableStateFlow(false)
    val showEditTeacherDialog: StateFlow<Boolean> = _showEditTeacherDialog.asStateFlow()
    
    private val _showAddScopeDialog = MutableStateFlow(false)
    val showAddScopeDialog: StateFlow<Boolean> = _showAddScopeDialog.asStateFlow()
    
    private val _showEditScopeDialog = MutableStateFlow(false)
    val showEditScopeDialog: StateFlow<Boolean> = _showEditScopeDialog.asStateFlow()
    
    private val _selectedScope = MutableStateFlow<Scope?>(null)
    val selectedScope: StateFlow<Scope?> = _selectedScope.asStateFlow()

    private val _selectedTeacher = MutableStateFlow<Teacher?>(null)
    val selectedTeacher: StateFlow<Teacher?> = _selectedTeacher.asStateFlow()    // Theme management
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
    init {
        // Initialize data and settings from persistent storage
        viewModelScope.launch {
            repository.initializeData()
            loadSettings()
            
            // Debug: Print config directory location
            println("Orgraph config directory: ${getConfigDirectory()}")
        }
    }
    
    enum class ViewMode {
        LIST, MINDMAP
    }
    
    fun switchView(viewMode: ViewMode) {
        _selectedView.value = viewMode
        viewModelScope.launch {
            saveSettings()
        }
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
    
    fun showEditScopeDialog(scope: Scope) {
        _selectedScope.value = scope
        _showEditScopeDialog.value = true
    }
    
    fun hideEditScopeDialog() {
        _selectedScope.value = null
        _showEditScopeDialog.value = false
    }
    
    fun selectTeacher(teacher: Teacher?) {
        _selectedTeacher.value = teacher
    }
    
    fun addTeacher(
        name: String,
        subtitle: String,
        backgroundImage: String,
        email: String,
        phone: String,
        scopes: List<Scope>,
        description: String,
        experience: Int
    ) {
        val teacher = Teacher(
            id = UUID.randomUUID().toString(),
            name = name,
            subtitle = subtitle,
            backgroundImage = backgroundImage,
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
    
    fun addScope(name: String, subtitle: String, backgroundImage: String, color: Color, description: String) {
        val scope = Scope(
            id = UUID.randomUUID().toString(),
            name = name,
            subtitle = subtitle,
            backgroundImage = backgroundImage,
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
    
    // Settings management functions
    private suspend fun loadSettings() {
        try {
            val settings = configManager.loadConfig("app_settings.json", AppSettings.serializer())
                ?: AppSettings() // Use default settings if file doesn't exist
            
            _isDarkTheme.value = settings.isDarkTheme
            _selectedView.value = when (settings.lastViewMode) {
                "MINDMAP" -> ViewMode.MINDMAP
                else -> ViewMode.LIST
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to load settings: ${e.message}")
        }
    }
    
    private suspend fun saveSettings() {
        try {
            val settings = AppSettings(
                isDarkTheme = _isDarkTheme.value,
                lastViewMode = when (_selectedView.value) {
                    ViewMode.MINDMAP -> "MINDMAP"
                    ViewMode.LIST -> "LIST"
                }
            )
            configManager.saveConfig("app_settings.json", settings, AppSettings.serializer())
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to save settings: ${e.message}")
        }
    }
    
    // Theme management functions
    fun toggleTheme() {
        val newTheme = !_isDarkTheme.value
        _isDarkTheme.value = newTheme
        viewModelScope.launch {
            saveSettings()
        }
    }
    
    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        viewModelScope.launch {
            saveSettings()
        }
    }
    
    fun getConfigDirectory(): String = configManager.getConfigDirectory()
    
    fun updateScope(scope: Scope) {
        viewModelScope.launch {
            repository.updateScope(scope)
            hideEditScopeDialog()
        }
    }
    
    // Import/Export functions
    suspend fun exportToFile(filePath: String): Result<String> {
        return importExportManager.exportToFile(school.value, filePath)
    }
    
    suspend fun importFromFile(filePath: String): Result<String> {
        return try {
            val result = importExportManager.importFromFile(filePath)
            result.fold(
                onSuccess = { importResult ->
                    // Apply imported data
                    repository.saveSchool(importResult.school)
                    
                    Result.success("Configuration imported successfully from: $filePath")
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Import failed: ${e.message}"))
        }
    }
    
    suspend fun quickExport(): Result<String> {
        return importExportManager.quickExport(school.value)
    }
    
    suspend fun getAvailableBackups() = importExportManager.getAvailableBackups()
}