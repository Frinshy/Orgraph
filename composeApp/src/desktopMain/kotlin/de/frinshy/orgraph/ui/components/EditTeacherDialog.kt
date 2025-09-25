package de.frinshy.orgraph.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.frinshy.orgraph.data.models.Teacher
import de.frinshy.orgraph.data.models.Scope

@Composable
fun EditTeacherDialog(
    teacher: Teacher,
    availableScopes: List<Scope>,
    onDismiss: () -> Unit,
    onUpdateTeacher: (Teacher) -> Unit
) {
    var name by remember { mutableStateOf(teacher.name) }
    var email by remember { mutableStateOf(teacher.email) }
    var phone by remember { mutableStateOf(teacher.phone) }
    var description by remember { mutableStateOf(teacher.description) }
    var experience by remember { mutableStateOf(teacher.experience.toString()) }
    var selectedScopes by remember { mutableStateOf(teacher.scopes.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        OrgraphCard(
            modifier = Modifier
                .width(600.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Edit Teacher",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Phone field
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Experience field
                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Years of Experience") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Scopes selection
                Text(
                    text = "Scopes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableScopes) { scope ->
                        val isSelected = selectedScopes.contains(scope)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedScopes = if (checked) {
                                        selectedScopes + scope
                                    } else {
                                        selectedScopes - scope
                                    }
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            SubjectIndicator(
                                color = scope.color,
                                size = 12.dp
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = scope.name,
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
                                onUpdateTeacher(
                                    teacher.copy(
                                        name = name.trim(),
                                        email = email.trim(),
                                        phone = phone.trim(),
                                        scopes = selectedScopes.toList(),
                                        description = description.trim(),
                                        experience = experience.toIntOrNull() ?: 0
                                    )
                                )
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