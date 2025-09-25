package de.frinshy.orgraph.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.presentation.viewmodel.OrgraphViewModel
import de.frinshy.orgraph.ui.components.*

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

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        OrgraphTopAppBar(
            title = "${school.name} - Mind Map",
            actions = {
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
                    modifier = Modifier.fillMaxSize()
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
            onDismiss = { viewModel.hideAddTeacherDialog() },
            onAddTeacher = { name, email, phone, scopes, description, experience ->
                viewModel.addTeacher(name, email, phone, scopes, description, experience)
            }
        )
    }
    
    // Edit Teacher Dialog
    if (showEditDialog) {
        selectedTeacher?.let { teacher ->
            EditTeacherDialog(
                teacher = teacher,
                availableScopes = school.scopes,
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
            onDismiss = { viewModel.hideAddScopeDialog() },
            onAddScope = { name, color, description ->
                viewModel.addScope(name, color, description)
            }
        )
    }
}
