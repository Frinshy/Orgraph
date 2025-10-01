package de.frinshy.orgraph.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Utility object for handling image operations in Orgraph
 */
object ImageUtils {
    
    private const val IMAGES_FOLDER = "images"
    private const val TEACHERS_FOLDER = "teachers"
    private const val SCOPES_FOLDER = "scopes"
    private const val SCHOOLS_FOLDER = "schools"
    
    /**
     * Opens a file dialog to select an image file
     * @return The selected image file path, or null if cancelled
     */
    fun selectImageFile(): String? {
        val fileDialog = FileDialog(null as Frame?, "Select Background Image", FileDialog.LOAD)
        fileDialog.setFilenameFilter { _, name ->
            val lowercaseName = name.lowercase()
            lowercaseName.endsWith(".jpg") || 
            lowercaseName.endsWith(".jpeg") || 
            lowercaseName.endsWith(".png") || 
            lowercaseName.endsWith(".bmp") || 
            lowercaseName.endsWith(".gif")
        }
        fileDialog.isVisible = true
        
        val fileName = fileDialog.file
        val directory = fileDialog.directory
        
        return if (fileName != null && directory != null) {
            File(directory, fileName).absolutePath
        } else null
    }
    
    /**
     * Copies an image to the app's images directory and returns the relative path
     * @param sourceImagePath The source image file path
     * @param targetType Either "teacher", "scope", or "school"
     * @param entityId The ID of the teacher, scope, or school
     * @return The relative path to the copied image, or null if failed
     */
    fun copyImageToAppDirectory(
        sourceImagePath: String, 
        targetType: String, 
        entityId: String,
        configDirectory: String
    ): String? {
        return try {
            val sourceFile = File(sourceImagePath)
            if (!sourceFile.exists()) return null
            
            val extension = sourceFile.extension.lowercase()
            val targetFolder = when (targetType) {
                "teacher" -> TEACHERS_FOLDER
                "scope" -> SCOPES_FOLDER
                "school" -> SCHOOLS_FOLDER
                else -> return null
            }
            
            // Create images directory structure
            val imagesDir = Paths.get(configDirectory, IMAGES_FOLDER, targetFolder)
            Files.createDirectories(imagesDir)
            
            // Create unique filename
            val targetFileName = "${entityId}_${System.currentTimeMillis()}.$extension"
            val targetFile = imagesDir.resolve(targetFileName)
            
            // Copy file
            Files.copy(sourceFile.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING)
            
            // Return relative path
            "$IMAGES_FOLDER/$targetFolder/$targetFileName"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Deletes an image from the app's directory
     * @param imagePath The relative path to the image
     * @param configDirectory The config directory path
     */
    fun deleteImageFromAppDirectory(imagePath: String, configDirectory: String) {
        try {
            if (imagePath.isBlank()) return
            val imageFile = Paths.get(configDirectory, imagePath)
            if (Files.exists(imageFile)) {
                Files.delete(imageFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Loads an image from the given path
     * @param imagePath The relative or absolute path to the image
     * @param configDirectory The config directory path
     * @return ImageBitmap or null if loading failed
     */
    fun loadImageBitmap(imagePath: String, configDirectory: String): ImageBitmap? {
        return try {
            if (imagePath.isBlank()) return null
            
            val imageFile = if (File(imagePath).isAbsolute) {
                File(imagePath)
            } else {
                File(configDirectory, imagePath)
            }
            
            if (!imageFile.exists()) return null
            
            val bytes = Files.readAllBytes(imageFile.toPath())
            val skiaImage = Image.makeFromEncoded(bytes)
            skiaImage.toComposeImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Checks if an image file exists
     * @param imagePath The relative path to the image
     * @param configDirectory The config directory path
     * @return true if image exists, false otherwise
     */
    fun imageExists(imagePath: String, configDirectory: String): Boolean {
        return try {
            if (imagePath.isBlank()) return false
            val imageFile = Paths.get(configDirectory, imagePath)
            Files.exists(imageFile)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gets the full path to an image
     * @param imagePath The relative path to the image
     * @param configDirectory The config directory path
     * @return The full path to the image file
     */
    fun getFullImagePath(imagePath: String, configDirectory: String): String {
        return if (imagePath.isBlank()) {
            ""
        } else {
            Paths.get(configDirectory, imagePath).toString()
        }
    }
}