package de.frinshy.orgraph.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.frinshy.orgraph.data.models.Scope
import de.frinshy.orgraph.presentation.components.CompactImageSelector
import de.frinshy.orgraph.util.ImageUtils

@Composable
fun EditScopeDialog(
    scope: Scope,
    configDirectory: String,
    onDismiss: () -> Unit,
    onUpdateScope: (Scope) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(scope.name) }
    var subtitle by remember { mutableStateOf(scope.subtitle) }
    var backgroundImage by remember { mutableStateOf(scope.backgroundImage) }
    var description by remember { mutableStateOf(scope.description) }
    var selectedColor by remember { mutableStateOf(scope.color) }

    // Color palette for selection
    val colorPalette = listOf(
        Color(0xFF1976D2), Color(0xFF388E3C), Color(0xFFF57C00),
        Color(0xFFD32F2F), Color(0xFF7B1FA2), Color(0xFF00796B),
        Color(0xFFF4511E), Color(0xFF5D4037), Color(0xFF455A64),
        Color(0xFF0097A7), Color(0xFF512DA8), Color(0xFF689F38)
    )

    val isValidForm = name.isNotBlank()

    OrgraphDialog(
        title = "Edit Scope",
        onDismiss = onDismiss,
        actions = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            FilledTonalButton(
                onClick = {
                    if (isValidForm) {
                        val updatedScope = scope.copy(
                            name = name.trim(),
                            subtitle = subtitle.trim(),
                            backgroundImage = backgroundImage,
                            description = description.trim(),
                            color = selectedColor
                        )
                        onUpdateScope(updatedScope)
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
                Text("Update Scope")
            }
        },
        modifier = modifier
    ) {
        OrgraphFormField(
            value = name,
            onValueChange = { name = it },
            label = "Scope Name *",
            placeholder = "e.g., Mathematics, Science, Arts...",
            singleLine = true,
            required = true
        )

        OrgraphFormField(
            value = subtitle,
            onValueChange = { subtitle = it },
            label = "Subtitle/Type",
            placeholder = "e.g., Core Subject, Elective, AP Course",
            singleLine = true
        )

        OrgraphFormSection(
            title = "Background Image",
            subtitle = "Choose an image to represent this scope"
        ) {
            CompactImageSelector(
                selectedImagePath = backgroundImage ?: "",
                configDirectory = configDirectory,
                onImageSelected = { selectedPath ->
                    val copiedPath = ImageUtils.copyImageToAppDirectory(
                        sourceImagePath = selectedPath,
                        targetType = "scope",
                        entityId = scope.id,
                        configDirectory = configDirectory
                    )
                    copiedPath?.let { backgroundImage = it }
                },
                onImageCleared = { backgroundImage = "" }
            )
        }

        OrgraphFormSection(
            title = "Color Theme",
            subtitle = "Select a color to identify this scope"
        ) {
            ColorSelectionGrid(
                selectedColor = selectedColor,
                colorPalette = colorPalette,
                onColorSelected = { selectedColor = it }
            )
        }

        OrgraphFormField(
            value = description,
            onValueChange = { description = it },
            label = "Description",
            placeholder = "Additional details about this scope...",
            singleLine = false,
            minLines = 3
        )
    }
}