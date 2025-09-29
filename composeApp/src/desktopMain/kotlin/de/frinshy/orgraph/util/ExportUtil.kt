package de.frinshy.orgraph.util

import androidx.compose.ui.geometry.Offset
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
import java.io.FileWriter
import javax.imageio.ImageIO

object ExportUtil {
    
    /**
     * Export mind map as SVG with exact positioning from canvas
     */
    /**
     * Export mind map as SVG with exact canvas matching
     */
    suspend fun exportMindMapAsSvgWithExactMatching(
        school: de.frinshy.orgraph.data.models.School,
        mindMapData: de.frinshy.orgraph.ui.components.MindMapNode,
        colorScheme: androidx.compose.material3.ColorScheme,
        width: Int = 1200,
        height: Int = 800,
        defaultFileName: String = "mindmap"
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val svgContent = generateExactSvgFromMindMapData(mindMapData, colorScheme, width, height)
                exportToSvg(svgContent, defaultFileName)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun generateExactSvgFromMindMapData(
        rootNode: de.frinshy.orgraph.ui.components.MindMapNode,
        colorScheme: androidx.compose.material3.ColorScheme,
        width: Int,
        height: Int
    ): String {
        val svg = StringBuilder()
        
        // SVG header with styling
        svg.append("""
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="$width" height="$height" xmlns="http://www.w3.org/2000/svg">
            <defs>
                <style>
                    .mindmap-text { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif; 
                        text-anchor: middle;
                        dominant-baseline: central;
                    }
                </style>
            </defs>
            
        """.trimIndent())
        
        // Draw connections first (so they appear behind nodes)
        drawSvgConnectionsRecursively(rootNode, colorScheme, svg)
        
        // Draw nodes on top
        drawSvgNodesRecursively(rootNode, colorScheme, svg)
        
        // Close SVG
        svg.append("</svg>")
        
        return svg.toString()
    }
    
    private fun drawSvgConnectionsRecursively(
        node: de.frinshy.orgraph.ui.components.MindMapNode, 
        colorScheme: androidx.compose.material3.ColorScheme,
        svg: StringBuilder
    ) {
        // Draw lines to children (matching canvas strokeWidth = 2.dp.toPx() and outline color)
        val connectionColor = colorToHex(colorScheme.outline.copy(alpha = 0.6f))
        node.children.forEach { child ->
            svg.append("""
                <line x1="${node.position.x}" y1="${node.position.y}" 
                      x2="${child.position.x}" y2="${child.position.y}" 
                      stroke="$connectionColor" stroke-width="2" />
                
            """.trimIndent())
            
            // Recursively draw child connections
            drawSvgConnectionsRecursively(child, colorScheme, svg)
        }
    }
    
    private fun drawSvgNodesRecursively(
        node: de.frinshy.orgraph.ui.components.MindMapNode, 
        colorScheme: androidx.compose.material3.ColorScheme,
        svg: StringBuilder
    ) {
        val nodeColor = colorToHex(node.color)
        val radius = node.size / 2f
        
        // Draw node circle (main fill)
        svg.append("""
            <circle cx="${node.position.x}" cy="${node.position.y}" r="$radius" 
                    fill="$nodeColor" />
            
        """.trimIndent())
        
        // Draw node border (matching canvas stroke with theme colors)
        val borderColor = when (node.type) {
            de.frinshy.orgraph.ui.components.NodeType.SCHOOL -> colorToHex(colorScheme.onPrimary)
            de.frinshy.orgraph.ui.components.NodeType.SCOPE -> colorToHex(colorScheme.onSecondary.copy(alpha = 0.8f))
            de.frinshy.orgraph.ui.components.NodeType.TEACHER -> colorToHex(colorScheme.onTertiary.copy(alpha = 0.6f))
        }
        
        svg.append("""
            <circle cx="${node.position.x}" cy="${node.position.y}" r="$radius" 
                    fill="none" stroke="$borderColor" stroke-width="2" />
            
        """.trimIndent())
        
        // Draw text label (matching canvas text colors)
        val textColor = when (node.type) {
            de.frinshy.orgraph.ui.components.NodeType.SCHOOL -> colorToHex(colorScheme.onPrimary)
            de.frinshy.orgraph.ui.components.NodeType.SCOPE -> colorToHex(colorScheme.onSecondary)
            de.frinshy.orgraph.ui.components.NodeType.TEACHER -> colorToHex(colorScheme.onTertiary)
        }
        
        val fontSize = when (node.type) {
            de.frinshy.orgraph.ui.components.NodeType.SCHOOL -> "16px"
            de.frinshy.orgraph.ui.components.NodeType.SCOPE -> "14px"
            de.frinshy.orgraph.ui.components.NodeType.TEACHER -> "12px"
        }
        
        val fontWeight = when (node.type) {
            de.frinshy.orgraph.ui.components.NodeType.SCHOOL -> "bold"
            de.frinshy.orgraph.ui.components.NodeType.SCOPE -> "500"
            de.frinshy.orgraph.ui.components.NodeType.TEACHER -> "normal"
        }
        
        svg.append("""
            <text x="${node.position.x}" y="${node.position.y}" 
                  class="mindmap-text" 
                  fill="$textColor" 
                  font-size="$fontSize" 
                  font-weight="$fontWeight">
                ${node.label}
            </text>
            
        """.trimIndent())
        
        // Draw children recursively
        node.children.forEach { child ->
            drawSvgNodesRecursively(child, colorScheme, svg)
        }
    }
    
    suspend fun exportMindMapAsSvgWithPositions(
        school: de.frinshy.orgraph.data.models.School,
        scopePositions: Map<String, Offset>,
        teacherPositions: Map<String, Offset>,
        width: Int = 1200,
        height: Int = 800,
        defaultFileName: String = "mindmap"
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val svgContent = generateSvgFromPositions(school, scopePositions, teacherPositions, width, height)
                exportToSvg(svgContent, defaultFileName)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun generateSvgFromPositions(
        school: de.frinshy.orgraph.data.models.School,
        scopePositions: Map<String, Offset>,
        teacherPositions: Map<String, Offset>,
        width: Int,
        height: Int
    ): String {
        val svg = StringBuilder()
        
        // SVG header with styling
        svg.append("""
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="$width" height="$height" xmlns="http://www.w3.org/2000/svg">
            <defs>
                <style>
                    .mindmap-text { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif; 
                        text-anchor: middle;
                        dominant-baseline: central;
                    }
                    .school-text { 
                        font-weight: bold; 
                        font-size: 16px; 
                        fill: white;
                    }
                    .scope-text { 
                        font-weight: 500; 
                        font-size: 14px; 
                        fill: white;
                    }
                    .teacher-text { 
                        font-weight: normal; 
                        font-size: 12px; 
                        fill: white;
                    }
                    .school-circle { 
                        fill: #6750A4; 
                        stroke: #483A5A; 
                        stroke-width: 2; 
                    }
                    .teacher-circle { 
                        fill: #9E7AC7; 
                        stroke: #6750A4; 
                        stroke-width: 1; 
                    }
                    .connection-line { 
                        stroke: #6750A4; 
                        stroke-width: 2; 
                        opacity: 0.6; 
                    }
                </style>
            </defs>
            
        """.trimIndent())
        
        val schoolCenterX = width / 2f
        val schoolCenterY = height / 2f
        
        // Draw connections from school to scopes
        school.scopes.forEach { scope ->
            val scopePos = scopePositions[scope.id]
            if (scopePos != null) {
                svg.append("""
                    <line x1="$schoolCenterX" y1="$schoolCenterY" 
                          x2="${scopePos.x}" y2="${scopePos.y}" 
                          class="connection-line" />
                    
                """.trimIndent())
            }
        }
        
        // Draw connections from scopes to teachers
        school.scopes.forEach { scope ->
            val scopePos = scopePositions[scope.id]
            if (scopePos != null) {
                val teachersInScope = school.getTeachersByScope(scope.id)
                teachersInScope.forEach teacherLoop@{ teacher ->
                    val teacherPos = teacherPositions[teacher.id] ?: return@teacherLoop
                    svg.append("""
                        <line x1="${scopePos.x}" y1="${scopePos.y}" 
                              x2="${teacherPos.x}" y2="${teacherPos.y}" 
                              class="connection-line" />
                        
                    """.trimIndent())
                }
            }
        }
        
        // Draw school node
        svg.append("""
            <circle cx="$schoolCenterX" cy="$schoolCenterY" r="50" class="school-circle" />
            <text x="$schoolCenterX" y="$schoolCenterY" class="mindmap-text school-text">
                ${school.name}
            </text>
            
        """.trimIndent())
        
        // Draw scope nodes
        school.scopes.forEach { scope ->
            val scopePos = scopePositions[scope.id]
            if (scopePos != null) {
                val scopeColor = colorToHex(scope.color)
                
                svg.append("""
                    <circle cx="${scopePos.x}" cy="${scopePos.y}" r="35" 
                            fill="$scopeColor" stroke="#483A5A" stroke-width="2" />
                    <text x="${scopePos.x}" y="${scopePos.y}" class="mindmap-text scope-text">
                        ${scope.name}
                    </text>
                    
                """.trimIndent())
            }
        }
        
        // Draw teacher nodes
        school.scopes.forEach { scope ->
            val teachersInScope = school.getTeachersByScope(scope.id)
            teachersInScope.forEach teacherLoop@{ teacher ->
                val teacherPos = teacherPositions[teacher.id] ?: return@teacherLoop
                
                svg.append("""
                    <circle cx="${teacherPos.x}" cy="${teacherPos.y}" r="25" class="teacher-circle" />
                    <text x="${teacherPos.x}" y="${teacherPos.y}" class="mindmap-text teacher-text">
                        ${teacher.name}
                    </text>
                    
                """.trimIndent())
            }
        }
        
        // Close SVG
        svg.append("</svg>")
        
        return svg.toString()
    }
    
    private fun colorToHex(color: androidx.compose.ui.graphics.Color): String {
        val r = (color.red * 255).toInt().toString(16).padStart(2, '0')
        val g = (color.green * 255).toInt().toString(16).padStart(2, '0')
        val b = (color.blue * 255).toInt().toString(16).padStart(2, '0')
        return "#$r$g$b"
    }
    
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
    
    suspend fun exportToSvg(svgContent: String, defaultFileName: String = "mindmap"): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val fileDialog = FileDialog(null as Frame?, "Export Mind Map as SVG", FileDialog.SAVE)
                fileDialog.file = "$defaultFileName.svg"
                fileDialog.setFilenameFilter { _, name ->
                    name.lowercase().endsWith(".svg")
                }
                fileDialog.isVisible = true
                
                val selectedFile = fileDialog.file
                val selectedDirectory = fileDialog.directory
                
                if (selectedFile != null && selectedDirectory != null) {
                    val file = File(selectedDirectory, selectedFile)
                    val finalFile = if (file.extension.lowercase() != "svg") {
                        File(file.parentFile, "${file.nameWithoutExtension}.svg")
                    } else {
                        file
                    }
                    
                    FileWriter(finalFile).use { writer ->
                        writer.write(svgContent)
                    }
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