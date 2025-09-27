package de.frinshy.orgraph.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.frinshy.orgraph.data.models.Scope
import de.frinshy.orgraph.data.models.Teacher
import de.frinshy.orgraph.presentation.components.CompactImageSelector
import de.frinshy.orgraph.util.ImageUtils

@Composable
fun EditTeacherDialog(
    teacher: Teacher,
    availableScopes: List<Scope>,
    configDirectory: String,
    onDismiss: () -> Unit,
    onUpdateTeacher: (Teacher) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(teacher.name) }
    var subtitle by remember { mutableStateOf(teacher.subtitle) }
    var backgroundImage by remember { mutableStateOf(teacher.backgroundImage) }
    var email by remember { mutableStateOf(teacher.email) }
    var phone by remember { mutableStateOf(teacher.phone) }
    var description by remember { mutableStateOf(teacher.description) }
    var experience by remember { mutableStateOf(teacher.experience.toString()) }
    var selectedScopes by remember { mutableStateOf(teacher.scopes.toSet()) }

    val isValidForm = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()
    val experienceInt = experience.toIntOrNull() ?: 0

    OrgraphDialog(
        title = "Edit Teacher",
        onDismiss = onDismiss,
        actions = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            FilledTonalButton(
                onClick = {
                    if (isValidForm) {
                        val updatedTeacher = teacher.copy(
                            name = name.trim(),
                            subtitle = subtitle.trim(),
                            backgroundImage = backgroundImage,
                            email = email.trim(),
                            phone = phone.trim(),
                            description = description.trim(),
                            experience = experienceInt,
                            scopes = selectedScopes.toList()
                        )
                        onUpdateTeacher(updatedTeacher)
                        onDismiss()
                    }
                },
                enabled = isValidForm,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Teacher")
            }
        },
        modifier = modifier.widthIn(max = 600.dp)
    ) {
        OrgraphFormField(
            value = name,
            onValueChange = { name = it },
            label = "Teacher Name *",
            placeholder = "e.g., Dr. Smith, Ms. Johnson",
            singleLine = true,
            required = true
        )

        OrgraphFormField(
            value = subtitle,
            onValueChange = { subtitle = it },
            label = "Title/Position",
            placeholder = "e.g., Head of Department, Senior Teacher",
            singleLine = true
        )

        OrgraphFormSection(
            title = "Profile Image",
            subtitle = "Update the profile picture for this teacher"
        ) {
            CompactImageSelector(
                selectedImagePath = backgroundImage,
                configDirectory = configDirectory,
                onImageSelected = { selectedPath ->
                    val copiedPath = ImageUtils.copyImageToAppDirectory(
                        sourceImagePath = selectedPath,
                        targetType = "teacher",
                        entityId = teacher.id,
                        configDirectory = configDirectory
                    )
                    copiedPath?.let { backgroundImage = it }
                },
                onImageCleared = { backgroundImage = "" }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email *") },
                placeholder = { Text("teacher@school.edu") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = email.isNotBlank() && !email.contains("@")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone *") },
                placeholder = { Text("+1 234 567 8900") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = experience,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    experience = newValue
                }
            },
            label = { Text("Years of Experience") },
            placeholder = { Text("5") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OrgraphFormSection(
            title = "Teaching Subjects",
            subtitle = "Select the subjects this teacher teaches"
        ) {
            if (availableScopes.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableScopes) { scope ->
                        ScopeSelectionItem(
                            scope = scope,
                            isSelected = selectedScopes.contains(scope),
                            onSelectionChanged = { isSelected ->
                                selectedScopes = if (isSelected) {
                                    selectedScopes + scope
                                } else {
                                    selectedScopes - scope
                                }
                            }
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "No subjects available. Create some subjects first.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        OrgraphFormField(
            value = description,
            onValueChange = { description = it },
            label = "Description",
            placeholder = "Additional information about this teacher...",
            singleLine = false,
            minLines = 3
        )
    }
}

@Composable
private fun ScopeSelectionItem(
    scope: Scope,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = { onSelectionChanged(!isSelected) },
        label = {
            Text(
                text = scope.name,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        modifier = modifier
    )
}