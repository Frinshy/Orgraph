package de.frinshy.orgraph.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
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
    val selectedTeacher by viewModel.selectedTeacher.collectAsState()
    val showAddScopeDialog by viewModel.showAddScopeDialog.collectAsState()
    val showEditScopeDialog by viewModel.showEditScopeDialog.collectAsState()
    val selectedScope by viewModel.selectedScope.collectAsState()
    val selectedView by viewModel.selectedView.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.2f)
                    )
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
        // Top App Bar
        OrgraphTopAppBar(
            title = "${school.name} - Teachers",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), // Reduced from 8dp, 4dp
            actions = {
                // Enhanced Export button with icon background
                Box(
                    modifier = Modifier
                        .padding(end = 2.dp) // Reduced from 4dp
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        .padding(2.dp) // Reduced from 4dp
                ) {
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
                        icon = Icons.Default.Save,
                        contentDescription = "Export app data",
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                // Enhanced Import button with icon background
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
                        icon = Icons.Default.FolderOpen,
                        contentDescription = "Import app data",
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
                
                // Enhanced Theme toggle button with background
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
                            Icons.AutoMirrored.Filled.List
                        },
                        contentDescription = "Switch view",
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        )
        }

        // Desktop Two-Pane Layout with enhanced styling
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
                    title = "Welcome to Orgraph",
                    description = "Add teachers to get started with organizing your school staff.",
                    buttonText = "Add Teacher",
                    onButtonClick = { viewModel.showAddTeacherDialog() },
                    modifier = Modifier.graphicsLayer { alpha = animatedAlpha }
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp) // Reduced from 12dp
            ) {
                // Enhanced Left Sidebar with improved design
                Surface(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 3.dp,
                    shadowElevation = 4.dp,
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp), // Reduced from 16dp
                        verticalArrangement = Arrangement.spacedBy(12.dp) // Reduced from 16dp
                    ) {
                        // Enhanced statistics card with animation
                        item {
                            var showStats by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(Unit) {
                                showStats = true
                            }
                            
                            val statsAlpha by animateFloatAsState(
                                targetValue = if (showStats) 1f else 0f,
                                animationSpec = tween(600, delayMillis = 200)
                            )
                            
                            SchoolStatisticsCard(
                                school = school,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer { alpha = statsAlpha }
                            )
                        }
                        
                        // Enhanced scope management card with animation
                        item {
                            var showScopes by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(Unit) {
                                showScopes = true
                            }
                            
                            val scopesAlpha by animateFloatAsState(
                                targetValue = if (showScopes) 1f else 0f,
                                animationSpec = tween(600, delayMillis = 400)
                            )
                            
                            ScopeManagementCard(
                                scopes = school.scopes,
                                onAddScope = { viewModel.showAddScopeDialog() },
                                onEditScope = { scope -> viewModel.showEditScopeDialog(scope) },
                                onDeleteScope = { scopeId -> viewModel.removeScope(scopeId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer { alpha = scopesAlpha }
                            )
                        }
                    }
                }
                
                // Enhanced Main Content Area with improved styling
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 6.dp), // Reduced from 8dp
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TeachersDataTable(
                            teachers = school.teachers,
                            scopes = school.scopes,
                            onEditTeacher = { teacher -> viewModel.showEditTeacherDialog(teacher) },
                            onDeleteTeacher = { teacherId -> viewModel.removeTeacher(teacherId) },
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Enhanced FAB with better positioning and styling
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
            onAddScope = { scope ->
                viewModel.addScope(scope.name, scope.subtitle, scope.backgroundImage, scope.color, scope.description)
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
    // Animation for card appearance
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val animatedElevation by animateDpAsState(
        targetValue = if (isVisible) 6.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    OrgraphCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation,
            hoveredElevation = 8.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Enhanced header with gradient background
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp) // Reduced from 24dp
                    )
                    Spacer(modifier = Modifier.width(6.dp)) // Reduced from 8dp
                    Text(
                        text = "School Overview",
                        style = MaterialTheme.typography.titleMedium, // Reduced from titleLarge
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp)) // Reduced from 20dp
            
            // Enhanced stats grid with staggered animations
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp) // Reduced from 16dp
            ) {
                val stats = listOf(
                    Triple(Icons.Default.Person, "Teachers", school.teachers.size.toString()),
                    Triple(Icons.Default.Category, "Scopes", school.scopes.size.toString())
                )
                
                val colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary
                )
                
                stats.forEachIndexed { index, (icon, label, value) ->
                    var showStat by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(Unit) {
                        showStat = true
                    }
                    
                    val statAlpha by animateFloatAsState(
                        targetValue = if (showStat) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = 500,
                            delayMillis = index * 150
                        )
                    )
                    
                    DesktopStatisticRow(
                        icon = icon,
                        label = label,
                        value = value,
                        color = colors[index],
                        modifier = Modifier.graphicsLayer { alpha = statAlpha }
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopStatisticRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // Reduced from 16dp
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Reduced from 12dp
            ) {
                // Enhanced icon with background
                Box(
                    modifier = Modifier
                        .size(32.dp) // Reduced from 40dp
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.15f),
                                    color.copy(alpha = 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp) // Reduced from 22dp
                    )
                }
                
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium, // Reduced from bodyLarge
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Enhanced value with background
            Surface(
                color = color.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = value,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), // Reduced from 12dp, 6dp
                    style = MaterialTheme.typography.bodyLarge, // Reduced from titleMedium
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}