package de.frinshy.orgraph.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
fun TeacherListScreen(
    school: School,
    viewModel: OrgraphViewModel,
    modifier: Modifier = Modifier
) {
    val showAddDialog by viewModel.showAddTeacherDialog.collectAsState()
    val showEditDialog by viewModel.showEditTeacherDialog.collectAsState()
    val selectedTeacher by viewModel.selectedTeacher.collectAsState()
    val showAddScopeDialog by viewModel.showAddScopeDialog.collectAsState()
    val showEditScopeDialog by viewModel.showEditScopeDialog.collectAsState()
    val selectedScope by viewModel.selectedScope.collectAsState()
    val selectedView by viewModel.selectedView.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        OrgraphTopAppBar(
            title = school.name,
            actions = {
                // Export button
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
                
                // Import button
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
                
                // View toggle button
                OrgraphIconButton(
                    onClick = {
                        val newView = if (selectedView == OrgraphViewModel.ViewMode.LIST) {
                            OrgraphViewModel.ViewMode.MINDMAP
                        } else {
                            OrgraphViewModel.ViewMode.LIST
                        }
                        viewModel.switchView(newView)
                    },
                    icon = if (selectedView == OrgraphViewModel.ViewMode.LIST) {
                        Icons.Default.GridView
                    } else {
                        Icons.Default.List
                    },
                    contentDescription = "Switch view"
                )
            }
        )

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            if (school.teachers.isEmpty()) {
                // Empty state
                EmptyStateView(
                    icon = Icons.Default.Add,
                    title = "Welcome to Orgraph",
                    description = "Add teachers to get started with organizing your school staff.",
                    buttonText = "Add Teacher",
                    onButtonClick = { viewModel.showAddTeacherDialog() }
                )
            } else {
                // Teachers list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Statistics card
                    item {
                        SchoolStatisticsCard(
                            school = school,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Scope management card
                    item {
                        ScopeManagementCard(
                            scopes = school.scopes,
                            onAddScope = { viewModel.showAddScopeDialog() },
                            onEditScope = { scope -> viewModel.showEditScopeDialog(scope) },
                            onDeleteScope = { scopeId -> viewModel.removeScope(scopeId) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Teachers
                    items(school.teachers, key = { it.id }) { teacher ->
                        TeacherCard(
                            teacher = teacher,
                            onEdit = { selectedTeacher ->
                                viewModel.showEditTeacherDialog(selectedTeacher)
                            },
                            onDelete = { teacherId ->
                                viewModel.removeTeacher(teacherId)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // FAB
            OrgraphFloatingActionButton(
                onClick = { viewModel.showAddTeacherDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    // Dialogs
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

    if (showAddScopeDialog) {
        AddScopeDialog(
            configDirectory = viewModel.getConfigDirectory(),
            onDismiss = { viewModel.hideAddScopeDialog() },
            onAddScope = { name, subtitle, backgroundImage, color, description ->
                viewModel.addScope(name, subtitle, backgroundImage, color, description)
            }
        )
    }
    
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

@Composable
private fun SchoolStatisticsCard(
    school: School,
    modifier: Modifier = Modifier
) {
    OrgraphCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    label = "Teachers",
                    value = school.teachers.size.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatisticItem(
                    label = "Scopes",
                    value = school.scopes.size.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}