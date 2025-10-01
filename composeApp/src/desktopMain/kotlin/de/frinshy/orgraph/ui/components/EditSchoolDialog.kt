package de.frinshy.orgraph.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.presentation.components.CompactImageSelector
import de.frinshy.orgraph.util.ImageUtils

@Composable
fun EditSchoolDialog(
    school: School,
    configDirectory: String,
    onDismiss: () -> Unit,
    onUpdateSchool: (School) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(school.name) }
    var backgroundImage by remember { mutableStateOf(school.backgroundImage) }

    val isValidForm = name.isNotBlank()

    OrgraphDialog(
        title = "Edit School",
        onDismiss = onDismiss,
        actions = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
            FilledTonalButton(
                onClick = {
                    if (isValidForm) {
                        val updatedSchool = school.copy(
                            name = name.trim(),
                            backgroundImage = backgroundImage
                        )
                        onUpdateSchool(updatedSchool)
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
                Text("Update School")
            }
        },
        modifier = modifier
    ) {
        OrgraphFormField(
            value = name,
            onValueChange = { name = it },
            label = "School Name *",
            placeholder = "e.g., Lincoln High School, Springfield Elementary...",
            singleLine = true,
            required = true
        )

        OrgraphFormSection(
            title = "School Image",
            subtitle = "Choose an image to represent this school"
        ) {
            CompactImageSelector(
                selectedImagePath = backgroundImage,
                configDirectory = configDirectory,
                onImageSelected = { selectedPath ->
                    val copiedPath = ImageUtils.copyImageToAppDirectory(
                        sourceImagePath = selectedPath,
                        targetType = "school",
                        entityId = school.id,
                        configDirectory = configDirectory
                    )
                    copiedPath?.let { backgroundImage = it }
                },
                onImageCleared = { backgroundImage = "" }
            )
        }
    }
}