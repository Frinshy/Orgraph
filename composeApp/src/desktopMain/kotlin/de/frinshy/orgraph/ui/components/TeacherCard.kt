package de.frinshy.orgraph.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.frinshy.orgraph.data.models.Teacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCard(
    teacher: Teacher,
    onEdit: (Teacher) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    OrgraphCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = teacher.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (teacher.subtitle.isNotBlank()) {
                            Text(
                                text = teacher.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${teacher.scopes.size} scopes • ${teacher.experience} years exp.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row {
                    OrgraphIconButton(
                        onClick = { onEdit(teacher) },
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit teacher"
                    )
                    OrgraphIconButton(
                        onClick = { onDelete(teacher.id) },
                        icon = Icons.Default.Delete,
                        contentDescription = "Delete teacher",
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
            
            // Scopes chips
            if (teacher.scopes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(teacher.scopes) { scope ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SubjectIndicator(
                                color = scope.color,
                                size = 8.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OrgraphChip(
                                label = scope.name,
                                selected = true
                            )
                        }
                    }
                }
            }
            
            // Expanded content
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                if (teacher.email.isNotEmpty()) {
                    InfoRow(label = "Email", value = teacher.email)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (teacher.phone.isNotEmpty()) {
                    InfoRow(label = "Phone", value = teacher.phone)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (teacher.description.isNotEmpty()) {
                    InfoRow(label = "Description", value = teacher.description)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}