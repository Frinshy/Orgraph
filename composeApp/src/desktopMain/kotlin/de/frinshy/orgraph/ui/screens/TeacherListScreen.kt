package de.frinshy.orgraph.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.presentation.viewmodel.OrgraphViewModel
import de.frinshy.orgraph.ui.components.*

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
    val selectedView by viewModel.selectedView.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        OrgraphTopAppBar(
            title = school.name,
            actions = {
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
                    title = "No Teachers Yet",
                    description = "Add your first teacher to get started with organizing your school staff.",
                    buttonText = "Add Teacher",
                    onButtonClick = { viewModel.showAddTeacherDialog() }
                )
            } else {
                // Teacher list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Statistics card
                    item {
                        SchoolStatisticsCard(school = school)
                    }

                    // Teachers
                    items(school.teachers) { teacher ->
                        TeacherCard(
                            teacher = teacher,
                            onEdit = { teacherToEdit ->
                                viewModel.showEditTeacherDialog(teacherToEdit)
                            },
                            onDelete = { teacherId ->
                                viewModel.removeTeacher(teacherId)
                            }
                        )
                    }
                }
            }

            // Floating Action Button
            OrgraphFloatingActionButton(
                onClick = { viewModel.showAddTeacherDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }

    // Add Teacher Dialog
    if (showAddDialog) {
        AddTeacherDialog(
            availableSubjects = school.subjects,
            onDismiss = { viewModel.hideAddTeacherDialog() },
            onAddTeacher = { name, email, phone, subjects, description, experience ->
                viewModel.addTeacher(name, email, phone, subjects, description, experience)
            }
        )
    }
    
    // Edit Teacher Dialog
    if (showEditDialog) {
        selectedTeacher?.let { teacher ->
            EditTeacherDialog(
                teacher = teacher,
                availableSubjects = school.subjects,
                onDismiss = { viewModel.hideEditTeacherDialog() },
                onUpdateTeacher = { updatedTeacher ->
                    viewModel.updateTeacher(updatedTeacher)
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
                text = "School Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

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
                    label = "Subjects",
                    value = school.subjects.size.toString(),
                    color = MaterialTheme.colorScheme.secondary
                )

                StatisticItem(
                    label = "Avg Experience",
                    value = "${school.teachers.map { it.experience }.average().toInt()}y",
                    color = MaterialTheme.colorScheme.tertiary
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
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
