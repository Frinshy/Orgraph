package de.frinshy.orgraph.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
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
    val showEditSchoolDialog by viewModel.showEditSchoolDialog.collectAsState()
    val selectedTeacher by viewModel.selectedTeacher.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val currentColorScheme = MaterialTheme.colorScheme

    // State to track positions and mindmap data for exports
    var mindMapData by remember { mutableStateOf<MindMapNode?>(null) }
    var showExportMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f)
                    ),
                    radius = 1500f
                )
            )
    ) {
        // Enhanced Top App Bar with improved styling
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 6.dp, // Reduced from 8dp
            tonalElevation = 4.dp, // Reduced from 6dp
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp) // Reduced from 16dp
        ) {
            OrgraphTopAppBar(
                title = "${school.name} - Mind Map",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), // Reduced from 8dp, 4dp
                actions = {
                    // Export button (only show when there are teachers)
                    if (school.teachers.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(end = 2.dp) // Reduced from 4dp
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                .padding(2.dp) // Reduced from 4dp
                        ) {
                            OrgraphIconButton(
                                onClick = { showExportMenu = !showExportMenu },
                                icon = Icons.Default.Download,
                                contentDescription = "Export mind map"
                            )
                        }

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
                                            configDirectory = viewModel.getConfigDirectory(),
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

                    // Export config button
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .padding(4.dp)
                    ) {
                        OrgraphIconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val fileDialog =
                                        FileDialog(null as Frame?, "Export Orgraph Configuration", FileDialog.SAVE)
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
                            contentDescription = "Export configuration",
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Import config button
                    Box(
                        modifier = Modifier
                            .padding(end = 2.dp) // Reduced from 4dp
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                            .padding(2.dp) // Reduced from 4dp
                    ) {
                        OrgraphIconButton(
                            onClick = {
                                coroutineScope.launch {
                                    val fileDialog =
                                        FileDialog(null as Frame?, "Import Orgraph Configuration", FileDialog.LOAD)
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
                            icon = Icons.Default.FolderOpen,
                            contentDescription = "Import configuration",
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }

                    // Theme toggle button
                    Box(
                        modifier = Modifier
                            .padding(end = 2.dp) // Reduced from 4dp
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f))
                            .padding(2.dp) // Reduced from 4dp
                    ) {
                        val isDarkTheme by viewModel.isDarkTheme.collectAsState()
                        ThemeToggleButton(
                            isDarkTheme = isDarkTheme,
                            onToggle = { viewModel.toggleTheme() }
                        )
                    }

                    // Enhanced View toggle button with background
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f))
                            .padding(2.dp) // Reduced from 4dp
                    ) {
                        OrgraphIconButton(
                            onClick = { viewModel.switchView(OrgraphViewModel.ViewMode.LIST) },
                            icon = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Switch to list view",
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            )
        }

        // Enhanced Mind Map Content
        if (school.teachers.isEmpty()) {
            // Enhanced empty state with animation
            var showEmptyState by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                showEmptyState = true
            }

            val animatedAlpha by animateFloatAsState(
                targetValue = if (showEmptyState) 1f else 0f,
                animationSpec = tween(1000)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp) // Reduced from 24dp
            ) {
                EmptyStateView(
                    icon = Icons.Default.Add,
                    title = "Mind Map View",
                    description = "Add teachers to visualize your school structure as an interactive mind map.",
                    buttonText = "Add Teacher",
                    onButtonClick = { viewModel.showAddTeacherDialog() },
                    modifier = Modifier.graphicsLayer { alpha = animatedAlpha }
                )
            }
        } else {
            // Enhanced Mind Map View
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                shadowElevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MindMapView(
                        school = school,
                        modifier = Modifier.fillMaxSize(),
                        configDirectory = viewModel.getConfigDirectory(),
                        onPositionsReady = { scopes, teachers ->
                        },
                        onMindMapDataReady = { data ->
                            mindMapData = data
                        },
                        onSchoolClick = {
                            viewModel.showEditSchoolDialog()
                        },
                        onScopeClick = { scopeId ->
                            val scope = school.scopes.find { it.id == scopeId }
                            scope?.let { viewModel.showEditScopeDialog(it) }
                        },
                        onTeacherClick = { teacherId ->
                            val teacher = school.teachers.find { it.id == teacherId }
                            teacher?.let { viewModel.showEditTeacherDialog(it) }
                        }
                    )

                    // Enhanced FAB with animation
                    var fabVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        fabVisible = true
                    }

                    val fabScale by animateFloatAsState(
                        targetValue = if (fabVisible) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )

                    OrgraphFloatingActionButton(
                        onClick = { viewModel.showAddTeacherDialog() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                            .graphicsLayer {
                                scaleX = fabScale
                                scaleY = fabScale
                            },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
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

    // Edit School Dialog
    if (showEditSchoolDialog) {
        EditSchoolDialog(
            school = school,
            configDirectory = viewModel.getConfigDirectory(),
            onDismiss = { viewModel.hideEditSchoolDialog() },
            onUpdateSchool = { updatedSchool ->
                viewModel.updateSchool(updatedSchool)
            }
        )
    }
}