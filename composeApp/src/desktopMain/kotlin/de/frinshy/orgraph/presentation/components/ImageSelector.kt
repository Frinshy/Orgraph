package de.frinshy.orgraph.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.frinshy.orgraph.util.ImageUtils

/**
 * Component for selecting and displaying background images
 */
@Composable
fun ImageSelector(
    selectedImagePath: String,
    configDirectory: String,
    onImageSelected: (String) -> Unit,
    onImageCleared: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Background Image"
) {
    var loadedImage by remember(selectedImagePath) {
        mutableStateOf<ImageBitmap?>(null)
    }
    
    // Load image when path changes
    LaunchedEffect(selectedImagePath) {
        loadedImage = if (selectedImagePath.isNotBlank()) {
            ImageUtils.loadImageBitmap(selectedImagePath, configDirectory)
        } else {
            null
        }
    }
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(8.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
                .clickable {
                    val selectedPath = ImageUtils.selectImageFile()
                    if (selectedPath != null) {
                        onImageSelected(selectedPath)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (loadedImage != null) {
                // Display selected image
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = loadedImage!!,
                        contentDescription = "Selected background image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Clear button
                    IconButton(
                        onClick = {
                            onImageCleared()
                            loadedImage = null
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear image",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                // Show placeholder when no image is selected
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Select image",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Click to select image",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        if (selectedImagePath.isNotBlank()) {
            Text(
                text = "Selected: ${selectedImagePath.substringAfterLast('/')}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * Compact version of ImageSelector for use in forms
 */
@Composable
fun CompactImageSelector(
    selectedImagePath: String,
    configDirectory: String,
    onImageSelected: (String) -> Unit,
    onImageCleared: () -> Unit,
    modifier: Modifier = Modifier
) {
    var loadedImage by remember(selectedImagePath) {
        mutableStateOf<ImageBitmap?>(null)
    }
    
    // Load image when path changes
    LaunchedEffect(selectedImagePath) {
        loadedImage = if (selectedImagePath.isNotBlank()) {
            ImageUtils.loadImageBitmap(selectedImagePath, configDirectory)
        } else {
            null
        }
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image preview
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(8.dp)
                )
                .background(MaterialTheme.colorScheme.surface)
                .clickable {
                    val selectedPath = ImageUtils.selectImageFile()
                    if (selectedPath != null) {
                        onImageSelected(selectedPath)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (loadedImage != null) {
                androidx.compose.foundation.Image(
                    bitmap = loadedImage!!,
                    contentDescription = "Background image preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add image",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Info and controls
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (selectedImagePath.isNotBlank()) {
                    selectedImagePath.substringAfterLast('/')
                } else {
                    "No image selected"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedImagePath.isNotBlank()) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Text(
                text = "Click preview to change",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Clear button (only show if image is selected)
        if (selectedImagePath.isNotBlank()) {
            IconButton(onClick = {
                onImageCleared()
                loadedImage = null
            }) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Clear image",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}