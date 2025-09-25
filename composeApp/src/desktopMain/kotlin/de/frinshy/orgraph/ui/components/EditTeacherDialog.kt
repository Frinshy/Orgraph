package de.frinshy.orgraph.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.frinshy.orgraph.data.models.Subject
import de.frinshy.orgraph.data.models.Teacher

@Composable
fun EditTeacherDialog(
    teacher: Teacher,
    availableSubjects: List<Subject>,
    onDismiss: () -> Unit,
    onUpdateTeacher: (Teacher) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(teacher.name) }
    var email by remember { mutableStateOf(teacher.email) }
    var phone by remember { mutableStateOf(teacher.phone) }
    var description by remember { mutableStateOf(teacher.description) }
    var experience by remember { mutableStateOf(teacher.experience.toString()) }
    var selectedSubjects by remember { mutableStateOf(teacher.subjects.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.widthIn(max = 600.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Teacher",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                    OrgraphIconButton(
                        onClick = onDismiss,
                        icon = Icons.Default.Close,
                        contentDescription = "Close dialog"
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Form fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Years of Experience") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Subjects selection
                Text(
                    text = "Subjects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableSubjects) { subject ->
                        val isSelected = selectedSubjects.contains(subject)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedSubjects = if (checked) {
                                        selectedSubjects + subject
                                    } else {
                                        selectedSubjects - subject
                                    }
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            SubjectIndicator(
                                color = subject.color,
                                size = 12.dp
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OrgraphButton(
                        onClick = {
                            if (name.isNotBlank()) {
                                val updatedTeacher = teacher.copy(
                                    name = name.trim(),
                                    email = email.trim(),
                                    phone = phone.trim(),
                                    subjects = selectedSubjects.toList(),
                                    description = description.trim(),
                                    experience = experience.toIntOrNull() ?: 0
                                )
                                onUpdateTeacher(updatedTeacher)
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Update Teacher")
                    }
                }
            }
        }
    }
}