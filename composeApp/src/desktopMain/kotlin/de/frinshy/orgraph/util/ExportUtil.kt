package de.frinshy.orgraph.util

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object ExportUtil {
    
    suspend fun exportMindMapToPng(
        drawFunction: DrawScope.(TextMeasurer) -> Unit,
        width: Int = 2560,  // Higher resolution for crisp output
        height: Int = 1440,
        defaultFileName: String = "mindmap"
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Create a bitmap and draw the mind map with high density for crisp rendering
                val bitmap = ImageBitmap(width, height)
                val canvas = androidx.compose.ui.graphics.Canvas(bitmap)
                
                // Use higher density for crisp rendering (2x for retina-like quality)
                val density = Density(2f)
                val drawScope = CanvasDrawScope()
                
                // Create TextMeasurer for export with high density
                val textMeasurer = TextMeasurer(
                    defaultDensity = density,
                    defaultLayoutDirection = LayoutDirection.Ltr,
                    defaultFontFamilyResolver = createFontFamilyResolver()
                )
                
                drawScope.draw(
                    density = density,
                    layoutDirection = LayoutDirection.Ltr,
                    canvas = canvas,
                    size = Size(width.toFloat(), height.toFloat())
                ) {
                    // Keep background transparent - no background fill
                    // Draw the mind map with TextMeasurer
                    drawFunction(textMeasurer)
                }
                
                exportToPng(bitmap, defaultFileName)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    suspend fun exportToPng(bitmap: ImageBitmap, defaultFileName: String = "mindmap"): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Use native system file dialog
                val fileDialog = FileDialog(null as Frame?, "Export Mind Map as PNG", FileDialog.SAVE)
                fileDialog.file = "$defaultFileName.png"
                fileDialog.setFilenameFilter { _, name ->
                    name.lowercase().endsWith(".png")
                }
                fileDialog.isVisible = true
                
                val selectedFile = fileDialog.file
                val selectedDirectory = fileDialog.directory
                
                if (selectedFile != null && selectedDirectory != null) {
                    val file = File(selectedDirectory, selectedFile)
                    val finalFile = if (file.extension.lowercase() != "png") {
                        File(file.parentFile, "${file.nameWithoutExtension}.png")
                    } else {
                        file
                    }
                    
                    val awtImage = bitmap.toAwtImage()
                    val bufferedImage = BufferedImage(
                        awtImage.width,
                        awtImage.height,
                        BufferedImage.TYPE_INT_ARGB
                    )
                    
                    val g2d = bufferedImage.createGraphics()
                    
                    // Enable high-quality rendering hints for crisp, anti-aliased output
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
                    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
                    g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
                    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
                    
                    g2d.drawImage(awtImage, 0, 0, null)
                    g2d.dispose()
                    
                    ImageIO.write(bufferedImage, "PNG", finalFile)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}