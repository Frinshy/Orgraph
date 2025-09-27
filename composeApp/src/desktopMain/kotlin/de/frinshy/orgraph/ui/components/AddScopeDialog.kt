package de.frinshy.orgraph.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.frinshy.orgraph.data.models.Scope
import de.frinshy.orgraph.presentation.components.CompactImageSelector
import de.frinshy.orgraph.util.ImageUtils
import java.util.*

@Composable
fun AddScopeDialog(
    configDirectory: String,
    onDismiss: () -> Unit,
    onAddScope: (Scope) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFF2196F3)) }
    var selectedImagePath by remember { mutableStateOf("") }

    val entityId = remember { UUID.randomUUID().toString() }

    // Color palette for selection
    val colorPalette = listOf(
        Color(0xFF1976D2), Color(0xFF388E3C), Color(0xFFF57C00),
        Color(0xFFD32F2F), Color(0xFF7B1FA2), Color(0xFF00796B),
        Color(0xFFF4511E), Color(0xFF5D4037), Color(0xFF455A64),
        Color(0xFF0097A7), Color(0xFF512DA8), Color(0xFF689F38)
    )

    OrgraphDialog(
        title = "Neuen Bereich hinzufügen",
        onDismiss = onDismiss,
        actions = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
            Button(
                onClick = {
                    val newScope = Scope(
                        id = entityId,
                        name = name,
                        subtitle = subtitle,
                        description = description,
                        color = selectedColor,
                        backgroundImage = selectedImagePath
                    )
                    onAddScope(newScope)
                    onDismiss()
                },
                enabled = name.isNotBlank()
            ) {
                Text("Hinzufügen")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OrgraphFormField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                placeholder = "Name des Bereichs eingeben",
                singleLine = true,
                required = true
            )

            OrgraphFormField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = "Untertitel",
                placeholder = "Untertitel des Bereichs eingeben",
                singleLine = true
            )

            OrgraphFormSection(title = "Hintergrundbild") {
                CompactImageSelector(
                    selectedImagePath = selectedImagePath,
                    configDirectory = configDirectory,
                    onImageSelected = { selectedPath ->
                        val copiedPath = ImageUtils.copyImageToAppDirectory(
                            sourceImagePath = selectedPath,
                            targetType = "scope",
                            entityId = entityId,
                            configDirectory = configDirectory
                        )
                        copiedPath?.let { selectedImagePath = it }
                    },
                    onImageCleared = {
                        selectedImagePath = ""
                    }
                )
            }

            OrgraphFormSection(title = "Farbe auswählen") {
                ColorSelectionGrid(
                    selectedColor = selectedColor,
                    colorPalette = colorPalette,
                    onColorSelected = { selectedColor = it }
                )
            }

            OrgraphFormField(
                value = description,
                onValueChange = { description = it },
                label = "Beschreibung",
                placeholder = "Beschreibung des Bereichs eingeben",
                singleLine = false,
                minLines = 3
            )
        }
    }
}
