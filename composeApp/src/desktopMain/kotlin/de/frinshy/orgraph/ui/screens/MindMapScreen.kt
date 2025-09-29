package de.frinshy.orgraph.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.presentation.viewmodel.OrgraphViewModel
import de.frinshy.orgraph.ui.components.*
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapScreen(
    school: School,
    viewModel: OrgraphViewModel,
    modifier: Modifier = Modifier
) {
    val showAddDialog by viewModel.showAddTeacherDialog.collectAsState()
    val showEditDialog by viewModel.showEditTeacherDialog.collectAsState()
    val selectedTeacher by viewModel.selectedTeacher.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val currentColorScheme = MaterialTheme.colorScheme
    
    // State to track positions and mindmap data for exports
    var scopePositions by remember { mutableStateOf<Map<String, Offset>>(emptyMap()) }
    var teacherPositions by remember { mutableStateOf<Map<String, Offset>>(emptyMap()) }
    var mindMapData by remember { mutableStateOf<de.frinshy.orgraph.ui.components.MindMapNode?>(null) }
    var showExportMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        OrgraphTopAppBar(
            title = "${school.name} - Mind Map",
            actions = {
                // Export button (only show when there are teachers)
                if (school.teachers.isNotEmpty()) {
                    Box {
                        OrgraphIconButton(
                            onClick = { showExportMenu = !showExportMenu },
                            icon = Icons.Default.Download,
                            contentDescription = "Export mind map"
                        )
                        
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export as PNG") },
                                onClick = {
                                    showExportMenu = false
                                    coroutineScope.launch {
                                        exportMindMapToPngExact(
                                            school = school,
                                            colorScheme = currentColorScheme,
                                            fileName = "${school.name}_mindmap"
                                        )
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as SVG") },
                                onClick = {
                                    showExportMenu = false
                                    coroutineScope.launch {
                                        mindMapData?.let { data ->
                                            exportMindMapToSvgExact(
                                                school = school,
                                                mindMapData = data,
                                                colorScheme = currentColorScheme,
                                                fileName = "${school.name}_mindmap"
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Export config button
                OrgraphIconButton(
                    onClick = { 
                        coroutineScope.launch {
                            val fileDialog = FileDialog(null as Frame?, "Export Orgraph Configuration", FileDialog.SAVE)
                            fileDialog.file = "orgraph_backup.json"
                            fileDialog.isVisible = true
                            
                            val fileName = fileDialog.file
                            val directory = fileDialog.directory
                            
                            if (fileName != null && directory != null) {
                                val filePath = File(directory, fileName).absolutePath
                                val result = viewModel.exportToFile(filePath)
                                result.onSuccess { message ->
                                    println("Export successful: $message")
                                }.onFailure { error ->
                                    println("Export failed: ${error.message}")
                                }
                            }
                        }
                    },
                    icon = Icons.Default.FileDownload,
                    contentDescription = "Export configuration"
                )
                
                // Import config button
                OrgraphIconButton(
                    onClick = { 
                        coroutineScope.launch {
                            val fileDialog = FileDialog(null as Frame?, "Import Orgraph Configuration", FileDialog.LOAD)
                            fileDialog.file = "*.json"
                            fileDialog.isVisible = true
                            
                            val fileName = fileDialog.file
                            val directory = fileDialog.directory
                            
                            if (fileName != null && directory != null) {
                                val filePath = File(directory, fileName).absolutePath
                                val result = viewModel.importFromFile(filePath)
                                result.onSuccess { message ->
                                    println("Import successful: $message")
                                }.onFailure { error ->
                                    println("Import failed: ${error.message}")
                                }
                            }
                        }
                    },
                    icon = Icons.Default.FileUpload,
                    contentDescription = "Import configuration"
                )
                
                // Theme toggle button
                val isDarkTheme by viewModel.isDarkTheme.collectAsState()
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onToggle = { viewModel.toggleTheme() }
                )
                
                OrgraphIconButton(
                    onClick = { viewModel.switchView(OrgraphViewModel.ViewMode.LIST) },
                    icon = Icons.Default.List,
                    contentDescription = "Switch to list view"
                )
            }
        )

        // Mind Map Content
        if (school.teachers.isEmpty()) {
            // Empty state
            EmptyStateView(
                icon = Icons.Default.Add,
                title = "Mind Map View",
                description = "Add teachers to visualize your school structure as an interactive mind map.",
                buttonText = "Add Teacher",
                onButtonClick = { viewModel.showAddTeacherDialog() }
            )
        } else {
            // Mind Map View
            Box(modifier = Modifier.fillMaxSize()) {
                MindMapView(
                    school = school,
                    modifier = Modifier.fillMaxSize(),
                    onPositionsReady = { scopes, teachers ->
                        scopePositions = scopes
                        teacherPositions = teachers
                    },
                    onMindMapDataReady = { data ->
                        mindMapData = data
                    }
                )

                // Floating Action Button
                OrgraphFloatingActionButton(
                    onClick = { viewModel.showAddTeacherDialog() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }
        }
    }

    // Add Teacher Dialog
    if (showAddDialog) {
        AddTeacherDialog(
            availableScopes = school.scopes,
            configDirectory = viewModel.getConfigDirectory(),
            onDismiss = { viewModel.hideAddTeacherDialog() },
            onAddTeacher = { name, subtitle, backgroundImage, email, phone, scopes, description, experience ->
                viewModel.addTeacher(name, subtitle, backgroundImage, email, phone, scopes, description, experience)
            }
        )
    }
    
    // Edit Teacher Dialog
    if (showEditDialog) {
        selectedTeacher?.let { teacher ->
            EditTeacherDialog(
                teacher = teacher,
                availableScopes = school.scopes,
                configDirectory = viewModel.getConfigDirectory(),
                onDismiss = { viewModel.hideEditTeacherDialog() },
                onUpdateTeacher = { updatedTeacher ->
                    viewModel.updateTeacher(updatedTeacher)
                }
            )
        }
    }
    
    // Add Scope Dialog
    val showAddScopeDialog by viewModel.showAddScopeDialog.collectAsState()
    if (showAddScopeDialog) {
        AddScopeDialog(
            configDirectory = viewModel.getConfigDirectory(),
            onDismiss = { viewModel.hideAddScopeDialog() },
            onAddScope = { scope ->
                viewModel.addScope(scope.name, scope.subtitle, scope.backgroundImage, scope.color, scope.description)
            }
        )
    }
    
    // Edit Scope Dialog
    val showEditScopeDialog by viewModel.showEditScopeDialog.collectAsState()
    val selectedScope by viewModel.selectedScope.collectAsState()
    if (showEditScopeDialog) {
        selectedScope?.let { scope ->
            EditScopeDialog(
                scope = scope,
                configDirectory = viewModel.getConfigDirectory(),
                onDismiss = { viewModel.hideEditScopeDialog() },
                onUpdateScope = { updatedScope ->
                    viewModel.updateScope(updatedScope)
                }
            )
        }
    }
}
