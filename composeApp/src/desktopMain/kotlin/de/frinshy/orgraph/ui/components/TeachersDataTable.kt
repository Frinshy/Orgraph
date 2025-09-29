package de.frinshy.orgraph.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.frinshy.orgraph.data.models.Scope
import de.frinshy.orgraph.data.models.Teacher

@Composable
fun TeachersDataTable(
    teachers: List<Teacher>,
    scopes: List<Scope>,
    onEditTeacher: (Teacher) -> Unit,
    onDeleteTeacher: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortBy by remember { mutableStateOf(TeacherSortField.NAME) }
    var sortAscending by remember { mutableStateOf(true) }

    val sortedTeachers = remember(teachers, sortBy, sortAscending) {
        when (sortBy) {
            TeacherSortField.NAME -> if (sortAscending) teachers.sortedBy { it.name } else teachers.sortedByDescending { it.name }
            TeacherSortField.SUBTITLE -> if (sortAscending) teachers.sortedBy { it.subtitle } else teachers.sortedByDescending { it.subtitle }
            TeacherSortField.EXPERIENCE -> if (sortAscending) teachers.sortedBy { it.experience } else teachers.sortedByDescending { it.experience }
            TeacherSortField.SCOPES_COUNT -> if (sortAscending) teachers.sortedBy { it.scopes.size } else teachers.sortedByDescending { it.scopes.size }
        }
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.large
    ) {
        Column {
            // Enhanced Table Header with gradient background
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                TeacherTableHeader(
                    sortBy = sortBy,
                    sortAscending = sortAscending,
                    onSortChange = { field ->
                        if (sortBy == field) {
                            sortAscending = !sortAscending
                        } else {
                            sortBy = field
                            sortAscending = true
                        }
                    }
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                thickness = 2.dp
            )

            // Enhanced Table Content with animation
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), // Reduced from 16dp, 12dp
                verticalArrangement = Arrangement.spacedBy(6.dp) // Reduced from 8dp
            ) {
                items(sortedTeachers, key = { it.id }) { teacher ->
                    var showTeacher by remember { mutableStateOf(false) }

                    LaunchedEffect(teacher.id) {
                        showTeacher = true
                    }

                    val teacherAlpha by animateFloatAsState(
                        targetValue = if (showTeacher) 1f else 0f,
                        animationSpec = tween(durationMillis = 400)
                    )

                    TeacherTableRow(
                        teacher = teacher,
                        scopes = scopes,
                        onEdit = { onEditTeacher(teacher) },
                        onDelete = { onDeleteTeacher(teacher.id) },
                        modifier = Modifier.graphicsLayer { alpha = teacherAlpha }
                    )
                }
            }
        }
    }
}

@Composable
private fun TeacherTableHeader(
    sortBy: TeacherSortField,
    sortAscending: Boolean,
    onSortChange: (TeacherSortField) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp), // Reduced from 16dp, 12dp
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name column header
        HeaderCell(
            text = "Name",
            modifier = Modifier.fillMaxWidth(0.25f),
            sortField = TeacherSortField.NAME,
            currentSort = sortBy,
            sortAscending = sortAscending,
            onSort = onSortChange
        )

        // Role column header
        HeaderCell(
            text = "Role",
            modifier = Modifier.fillMaxWidth(0.2f),
            sortField = TeacherSortField.SUBTITLE,
            currentSort = sortBy,
            sortAscending = sortAscending,
            onSort = onSortChange
        )

        // Contact column header
        HeaderCell(
            text = "Contact",
            modifier = Modifier.fillMaxWidth(0.2f),
            sortable = false
        )

        // Scopes column header
        HeaderCell(
            text = "Scopes",
            modifier = Modifier.fillMaxWidth(0.15f),
            sortField = TeacherSortField.SCOPES_COUNT,
            currentSort = sortBy,
            sortAscending = sortAscending,
            onSort = onSortChange,
            alignment = TextAlign.Center
        )

        // Experience column header
        HeaderCell(
            text = "Experience",
            modifier = Modifier.fillMaxWidth(0.1f),
            sortField = TeacherSortField.EXPERIENCE,
            currentSort = sortBy,
            sortAscending = sortAscending,
            onSort = onSortChange,
            alignment = TextAlign.Center
        )

        // Actions column header
        Box(
            modifier = Modifier.width(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Actions",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HeaderCell(
    text: String,
    modifier: Modifier = Modifier,
    weight: Float? = null,
    sortField: TeacherSortField? = null,
    currentSort: TeacherSortField? = null,
    sortAscending: Boolean = true,
    sortable: Boolean = sortField != null,
    onSort: ((TeacherSortField) -> Unit)? = null,
    alignment: TextAlign = TextAlign.Start
) {
    val isActive = sortField == currentSort

    Row(
        modifier = if (weight != null) {
            Modifier.fillMaxWidth(weight)
        } else {
            modifier
        }.let { baseModifier ->
            if (sortable && sortField != null && onSort != null) {
                baseModifier.then(
                    Modifier.clickable { onSort(sortField) }
                )
            } else baseModifier
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = when (alignment) {
            TextAlign.Center -> Arrangement.Center
            TextAlign.End -> Arrangement.End
            else -> Arrangement.Start
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = alignment
        )

        if (sortable && isActive) {
            Icon(
                imageVector = if (sortAscending) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (sortAscending) "Sorted ascending" else "Sorted descending",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TeacherTableRow(
    teacher: Teacher,
    scopes: List<Scope>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val animatedElevation by animateDpAsState(
        targetValue = when {
            isHovered -> 4.dp
            expanded -> 2.dp
            else -> 1.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "row_elevation"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isHovered) 1.01f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "row_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .hoverable(interactionSource)
            .clickable { expanded = !expanded },
        color = when {
            expanded -> MaterialTheme.colorScheme.surfaceContainerHigh
            isHovered -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f)
            else -> MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.medium,
        tonalElevation = animatedElevation,
        shadowElevation = if (isHovered) 4.dp else 1.dp
    ) {
        Column {
            // Main row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp), // Reduced from 16dp, 12dp
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name column
                Column(
                    modifier = Modifier.fillMaxWidth(0.25f)
                ) {
                    Text(
                        text = teacher.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (teacher.description.isNotBlank()) {
                        Text(
                            text = teacher.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Subtitle column
                Box(
                    modifier = Modifier.fillMaxWidth(0.2f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (teacher.subtitle.isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = teacher.subtitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Contact column
                Column(
                    modifier = Modifier.fillMaxWidth(0.2f)
                ) {
                    if (teacher.email.isNotBlank()) {
                        Text(
                            text = teacher.email,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (teacher.phone.isNotBlank()) {
                        Text(
                            text = teacher.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (teacher.email.isBlank() && teacher.phone.isBlank()) {
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Scopes column
                Box(
                    modifier = Modifier.fillMaxWidth(0.15f),
                    contentAlignment = Alignment.Center
                ) {
                    if (teacher.scopes.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${teacher.scopes.size}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Experience column
                Box(
                    modifier = Modifier.fillMaxWidth(0.1f),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = when {
                            teacher.experience >= 10 -> MaterialTheme.colorScheme.tertiary
                            teacher.experience >= 5 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.primary
                        },
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${teacher.experience}y",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = when {
                                teacher.experience >= 10 -> MaterialTheme.colorScheme.onTertiary
                                teacher.experience >= 5 -> MaterialTheme.colorScheme.onSecondary
                                else -> MaterialTheme.colorScheme.onPrimary
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Actions column
                Row(
                    modifier = Modifier.width(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        onClick = onEdit,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit teacher",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.size(28.dp),
                        onClick = onDelete,
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = CircleShape
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete teacher",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Expanded details
            AnimatedVisibility(
                visible = expanded && teacher.scopes.isNotEmpty(),
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(
                    animationSpec = tween(300, delayMillis = 100)
                ),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut(
                    animationSpec = tween(200)
                )
            ) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp), // Reduced from 16dp
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp) // Reduced from 16dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Assignment,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                Text(
                                    text = "Assigned Scopes",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Scope chips with staggered animation
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Group scopes into rows of 3
                                teacher.scopes.chunked(3).forEachIndexed { rowIndex, scopeRow ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        scopeRow.forEachIndexed { colIndex, scope ->
                                            val index = rowIndex * 3 + colIndex
                                            AnimatedVisibility(
                                                visible = expanded,
                                                enter = fadeIn(
                                                    animationSpec = tween(
                                                        durationMillis = 300,
                                                        delayMillis = 200 + (index * 50)
                                                    )
                                                ) + scaleIn(
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    ),
                                                    initialScale = 0.8f
                                                )
                                            ) {
                                                SuggestionChip(
                                                    onClick = { },
                                                    label = {
                                                        Text(
                                                            text = scope.name,
                                                            style = MaterialTheme.typography.labelMedium.copy(
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        )
                                                    },
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                        labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class TeacherSortField {
    NAME,
    SUBTITLE,
    EXPERIENCE,
    SCOPES_COUNT
}