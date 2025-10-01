package de.frinshy.orgraph.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.util.ExportUtil
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class MindMapNode(
    val id: String,
    val label: String,
    val color: Color,
    val level: Int,
    var position: Offset = Offset.Zero,
    var targetPosition: Offset = Offset.Zero,
    val children: List<MindMapNode> = emptyList(),
    var size: Float = 60f,
    val type: NodeType = NodeType.SCOPE,
    val imagePath: String = "" // Path to the background image
)

enum class NodeType {
    SCHOOL, SCOPE, TEACHER
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MindMapView(
    school: School,
    modifier: Modifier = Modifier,
    configDirectory: String = "", // Add config directory parameter
    onPositionsReady: ((scopePositions: Map<String, Offset>, teacherPositions: Map<String, Offset>) -> Unit)? = null,
    onMindMapDataReady: ((mindMapData: MindMapNode) -> Unit)? = null,
    onSchoolClick: (() -> Unit)? = null,
    onScopeClick: ((scopeId: String) -> Unit)? = null,
    onTeacherClick: ((teacherId: String) -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }
    val textMeasurer = rememberTextMeasurer()
    
    // Image cache for better performance
    val imageCache = remember { mutableMapOf<String, ImageBitmap?>() }
    
    // Build mind map structure with theme-aware colors
    val mindMapData = remember(school, colorScheme) {
        buildMindMapData(school, textMeasurer, colorScheme)
    }
    
    // Layout nodes immediately when canvas size is available or when mind map data changes
    LaunchedEffect(mindMapData, canvasSize) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            layoutNodes(mindMapData, canvasSize)
            // Call the callbacks with extracted data
            onPositionsReady?.let { callback ->
                val (scopePositions, teacherPositions) = extractPositionsFromMindMapData(mindMapData)
                callback(scopePositions, teacherPositions)
            }
            onMindMapDataReady?.invoke(mindMapData)
        }
    }
    
    // Additional effect to handle theme changes specifically
    LaunchedEffect(colorScheme) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            layoutNodes(mindMapData, canvasSize)
            // Call the callbacks with extracted data
            onPositionsReady?.let { callback ->
                val (scopePositions, teacherPositions) = extractPositionsFromMindMapData(mindMapData)
                callback(scopePositions, teacherPositions)
            }
            onMindMapDataReady?.invoke(mindMapData)
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = panOffset.x * scale,
                    translationY = panOffset.y * scale
                )
                .pointerInput(Unit) {
                    detectTransformGestures(
                        panZoomLock = true
                    ) { _, pan, zoom, _ ->
                        // Handle pan with constant sensitivity
                        panOffset += pan
                        
                        // Handle zoom with constraints
                        val newScale = (scale * zoom).coerceIn(0.3f, 3f)
                        scale = newScale
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        // Panning sensitivity now constant due to scale compensation in graphicsLayer
                        panOffset += dragAmount
                    }
                }
                .pointerInput(Unit) {
                    // Handle clicks on nodes
                    detectTapGestures { tapOffset ->
                        // Transform tap offset to world coordinates
                        val transformedOffset = Offset(
                            (tapOffset.x - panOffset.x * scale) / scale,
                            (tapOffset.y - panOffset.y * scale) / scale
                        )
                        
                        // Check which node was clicked
                        val clickedNode = findClickedNode(mindMapData, transformedOffset)
                        clickedNode?.let { node ->
                            when (node.type) {
                                NodeType.SCHOOL -> onSchoolClick?.invoke()
                                NodeType.SCOPE -> onScopeClick?.invoke(node.id)
                                NodeType.TEACHER -> onTeacherClick?.invoke(node.id)
                            }
                        }
                    }
                }
                .onPointerEvent(PointerEventType.Scroll) { event ->
                    val scrollDelta = event.changes.first().scrollDelta
                    val mousePosition = event.changes.first().position
                    
                    // Determine zoom direction and factor
                    val zoomFactor = if (scrollDelta.y > 0) 0.9f else 1.1f
                    val newScale = (scale * zoomFactor).coerceIn(0.3f, 3f)
                    
                    if (newScale != scale) {
                        // Standard zoom-to-cursor implementation
                        // Before zoom: mouse points to some world coordinate
                        // After zoom: same world coordinate should still be under mouse
                        
                        // Current transformation: screen = world * scale + pan * scale
                        // So: world = (screen - pan * scale) / scale
                        val worldBeforeX = (mousePosition.x - panOffset.x * scale) / scale
                        val worldBeforeY = (mousePosition.y - panOffset.y * scale) / scale
                        
                        // Update scale
                        scale = newScale
                        
                        // After zoom: screen = world * newScale + newPan * newScale
                        // We want: mousePosition = worldBefore * newScale + newPan * newScale
                        // So: newPan = (mousePosition - worldBefore * newScale) / newScale
                        panOffset = Offset(
                            (mousePosition.x - worldBeforeX * newScale) / newScale,
                            (mousePosition.y - worldBeforeY * newScale) / newScale
                        )
                    }
                }
        ) {
            // Update canvas size and ensure nodes are laid out before drawing
            if (canvasSize != size) {
                canvasSize = size
            }
            
            // Ensure nodes are properly laid out before drawing
            if (size.width > 0 && size.height > 0) {
                // Check if nodes need layout (recursive check for all nodes)
                fun needsLayoutCheck(node: MindMapNode): Boolean {
                    if (node.position == Offset.Zero) return true
                    return node.children.any { needsLayoutCheck(it) }
                }
                
                if (needsLayoutCheck(mindMapData)) {
                    layoutNodes(mindMapData, size)
                }
                
                drawMindMap(mindMapData, this, textMeasurer, colorScheme, imageCache, configDirectory)
            }
        }
        
        // Zoom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zoom in button
            FloatingActionButton(
                onClick = {
                    val newScale = (scale * 1.2f).coerceAtMost(3f)
                    scale = newScale
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In"
                )
            }
            
            // Zoom out button
            FloatingActionButton(
                onClick = {
                    val newScale = (scale * 0.8f).coerceAtLeast(0.3f)
                    scale = newScale
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out"
                )
            }
            
            // Reset zoom button
            FloatingActionButton(
                onClick = {
                    scale = 1f
                    panOffset = Offset.Zero
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = "Reset View"
                )
            }
            
            // Zoom level indicator
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "${(scale * 100).toInt()}%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper function to find which node was clicked
private fun findClickedNode(rootNode: MindMapNode, clickOffset: Offset): MindMapNode? {
    // Check the root node first
    val radius = rootNode.size / 2f
    val distance = (clickOffset - rootNode.position).getDistance()
    if (distance <= radius) {
        return rootNode
    }
    
    // Recursively check all child nodes
    rootNode.children.forEach { child ->
        val clickedChild = findClickedNode(child, clickOffset)
        if (clickedChild != null) {
            return clickedChild
        }
    }
    
    return null
}

private fun buildMindMapData(school: School, textMeasurer: TextMeasurer, colorScheme: ColorScheme): MindMapNode {
    // Root node (school)
    val schoolTextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
    val schoolTextResult = textMeasurer.measure(school.name, schoolTextStyle)
    val schoolSize = maxOf(
        schoolTextResult.size.width + 48f, // Increased padding for prominence
        schoolTextResult.size.height + 48f,
        120f // Larger minimum size - 50% bigger than largest scope/teacher
    )
    
    val rootNode = MindMapNode(
        id = school.id,
        label = school.name,
        color = colorScheme.primary, // Use theme primary color
        level = 0,
        size = schoolSize,
        type = NodeType.SCHOOL,
        imagePath = school.backgroundImage
    )
    
    // Scope nodes with teacher children
    val scopeNodes = school.getScopesWithTeachers().map { (scope, teachers) ->
        // Calculate scope size based on text and image availability
        val scopeTextStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        val scopeTextResult = textMeasurer.measure(scope.name, scopeTextStyle)
        
        // Larger size if image is available for better visual impact
        val hasImage = scope.backgroundImage.isNotEmpty()
        val scopeSize = maxOf(
            scopeTextResult.size.width + (if (hasImage) 32f else 24f),
            scopeTextResult.size.height + (if (hasImage) 32f else 24f),
            if (hasImage) 80f else 60f // Larger minimum for images
        )
        
        val teacherNodes = teachers.map { teacher ->
            // Calculate teacher size based on text and image availability
            val teacherTextStyle = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
            val teacherTextResult = textMeasurer.measure(teacher.name, teacherTextStyle)
            
            // Larger size if image is available for better visual impact
            val hasImage = teacher.backgroundImage.isNotEmpty()
            val teacherSize = maxOf(
                teacherTextResult.size.width + (if (hasImage) 32f else 20f),
                teacherTextResult.size.height + (if (hasImage) 32f else 20f),
                if (hasImage) 60f else 40f // Larger minimum for images
            )
            
            MindMapNode(
                id = teacher.id,
                label = teacher.name,
                color = colorScheme.tertiary, // Use theme tertiary color
                level = 2,
                size = teacherSize,
                type = NodeType.TEACHER,
                imagePath = teacher.backgroundImage
            )
        }
        
        MindMapNode(
            id = scope.id,
            label = scope.name,
            color = scope.color,
            level = 1,
            children = teacherNodes,
            size = scopeSize,
            type = NodeType.SCOPE,
            imagePath = scope.backgroundImage
        )
    }
    
    return rootNode.copy(children = scopeNodes)
}

private fun layoutNodes(rootNode: MindMapNode, canvasSize: Size) {
    val centerX = canvasSize.width / 2f
    val centerY = canvasSize.height / 2f
    
    // Position root node at center
    rootNode.position = Offset(centerX, centerY)
    rootNode.targetPosition = Offset(centerX, centerY)
    
    if (rootNode.children.isEmpty()) return
    
    // Layout scope nodes in a circle around the root
    val scopeCount = rootNode.children.size
    val baseRadius = minOf(canvasSize.width, canvasSize.height) * 0.25f
    
    rootNode.children.forEachIndexed { index, scopeNode ->
        val angle = (2 * PI * index / scopeCount).toFloat()
        
        // Calculate radius based on scope size to avoid overlap
        val dynamicRadius = baseRadius + scopeNode.size / 2f
        
        val x = centerX + cos(angle) * dynamicRadius
        val y = centerY + sin(angle) * dynamicRadius
        scopeNode.position = Offset(x, y)
        scopeNode.targetPosition = Offset(x, y)
        
        // Layout teacher nodes around each scope
        if (scopeNode.children.isNotEmpty()) {
            val teacherCount = scopeNode.children.size
            val teacherRadius = scopeNode.size / 2f + 60f
            
            scopeNode.children.forEachIndexed { teacherIndex, teacherNode ->
                val teacherAngle = if (teacherCount == 1) {
                    angle // Single teacher goes directly outward
                } else {
                    angle + (2 * PI * teacherIndex / teacherCount - PI / 2).toFloat() * 0.5f
                }
                
                val teacherX = x + cos(teacherAngle) * teacherRadius
                val teacherY = y + sin(teacherAngle) * teacherRadius
                teacherNode.position = Offset(teacherX, teacherY)
                teacherNode.targetPosition = Offset(teacherX, teacherY)
            }
        }
    }
}

private fun drawMindMap(
    rootNode: MindMapNode, 
    drawScope: DrawScope, 
    textMeasurer: TextMeasurer, 
    colorScheme: ColorScheme,
    imageCache: MutableMap<String, ImageBitmap?>,
    configDirectory: String
) {
    with(drawScope) {
        // Draw connections first
        drawConnectionsRecursively(rootNode, colorScheme)
        
        // Draw nodes on top
        drawNodesRecursively(rootNode, textMeasurer, colorScheme, imageCache, configDirectory)
    }
}

private fun DrawScope.drawConnectionsRecursively(node: MindMapNode, colorScheme: ColorScheme) {
    // Draw lines to children
    node.children.forEach { child ->
        drawLine(
            color = colorScheme.outline.copy(alpha = 0.6f), // Use theme outline color
            start = node.position,
            end = child.position,
            strokeWidth = 2.dp.toPx()
        )
        
        // Recursively draw child connections
        drawConnectionsRecursively(child, colorScheme)
    }
}

// Helper function to load and cache images
private fun loadImageFromCache(
    imagePath: String, 
    cache: MutableMap<String, ImageBitmap?>,
    configDirectory: String
): ImageBitmap? {
    if (imagePath.isEmpty()) {
        println("DEBUG: Empty image path")
        return null
    }
    
    println("DEBUG: Attempting to load image: $imagePath")
    
    return cache.getOrPut(imagePath) {
        try {
            // Resolve relative path against config directory
            val file = if (File(imagePath).isAbsolute) {
                File(imagePath)
            } else {
                File(configDirectory, imagePath)
            }
            
            println("DEBUG: Resolved path: ${file.absolutePath}")
            println("DEBUG: File exists: ${file.exists()}")
            
            if (file.exists()) {
                val bitmap = loadImageBitmap(file.inputStream())
                println("DEBUG: Successfully loaded image: $imagePath")
                bitmap
            } else {
                println("DEBUG: File does not exist: ${file.absolutePath}")
                null
            }
        } catch (e: Exception) {
            println("DEBUG: Failed to load image: $imagePath - ${e.message}")
            e.printStackTrace()
            null
        }
    }
}

private fun DrawScope.drawNodesRecursively(
    node: MindMapNode, 
    textMeasurer: TextMeasurer, 
    colorScheme: ColorScheme,
    imageCache: MutableMap<String, ImageBitmap?>,
    configDirectory: String
) {
    val radius = node.size / 2f
    
    println("DEBUG: Drawing node '${node.label}' with imagePath: '${node.imagePath}'")
    
    // Load image if available
    val image = if (node.imagePath.isNotEmpty()) {
        loadImageFromCache(node.imagePath, imageCache, configDirectory)
    } else {
        println("DEBUG: No image path for node '${node.label}'")
        null
    }
    
    println("DEBUG: Image loaded for '${node.label}': ${image != null}")
    
    // Draw image background with Material 3 Expressive design
    if (image != null) {
        // Create a circular clip path for the image
        val circlePath = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    offset = Offset(node.position.x - radius, node.position.y - radius),
                    size = Size(radius * 2f, radius * 2f)
                )
            )
        }
        
        clipPath(circlePath) {
            // Draw the image scaled to fit the circle
            val imageSize = IntSize(
                width = (radius * 2f).toInt(),
                height = (radius * 2f).toInt()
            )
            drawImage(
                image = image,
                dstOffset = IntOffset(
                    x = (node.position.x - radius).toInt(),
                    y = (node.position.y - radius).toInt()
                ),
                dstSize = imageSize
                // Removed alpha to show image at full opacity
            )
        }
        // Removed the gradient overlay for cleaner image display
    } else {
        // Draw solid color circle with Material 3 gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    node.color.copy(alpha = 0.9f),
                    node.color.copy(alpha = 1f),
                    node.color.copy(alpha = 0.8f)
                ),
                center = node.position,
                radius = radius
            ),
            radius = radius,
            center = node.position
        )
    }
    
    // Draw enhanced border with Material 3 expressive styling
    val borderColors = when (node.type) {
        NodeType.SCHOOL -> listOf(
            colorScheme.onPrimary,
            colorScheme.primary.copy(alpha = 0.8f)
        )
        NodeType.SCOPE -> listOf(
            colorScheme.onSecondary.copy(alpha = 0.9f),
            colorScheme.secondary.copy(alpha = 0.7f)
        )
        NodeType.TEACHER -> listOf(
            colorScheme.onTertiary.copy(alpha = 0.8f),
            colorScheme.tertiary.copy(alpha = 0.6f)
        )
    }
    
    // Outer border with gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = borderColors,
            center = node.position,
            radius = radius + 2.dp.toPx()
        ),
        radius = radius,
        center = node.position,
        style = Stroke(width = 3.dp.toPx()) // Thicker border for M3 expressive
    )
    
    // Inner highlight for depth
    drawCircle(
        color = colorScheme.surface.copy(alpha = 0.3f),
        radius = radius,
        center = node.position,
        style = Stroke(width = 1.dp.toPx())
    )
    
    // Draw text label with enhanced styling
    val textStyle = when (node.type) {
        NodeType.SCHOOL -> TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (image != null) Color.White else colorScheme.onPrimary
        )
        NodeType.SCOPE -> TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold, // Enhanced weight for M3
            color = if (image != null) Color.White else colorScheme.onSecondary
        )
        NodeType.TEACHER -> TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium, // Enhanced weight for M3
            color = if (image != null) Color.White else colorScheme.onTertiary
        )
    }
    
    val textResult = textMeasurer.measure(
        text = node.label,
        style = textStyle
    )
    
    val textOffset = Offset(
        x = node.position.x - textResult.size.width / 2f,
        y = node.position.y - textResult.size.height / 2f
    )
    
    // For images, draw a semi-transparent background behind text for better readability
    if (image != null) {
        val backgroundPadding = 4.dp.toPx()
        val backgroundRect = androidx.compose.ui.geometry.Rect(
            offset = textOffset - Offset(backgroundPadding, backgroundPadding),
            size = Size(
                textResult.size.width + backgroundPadding * 2,
                textResult.size.height + backgroundPadding * 2
            )
        )
        
        // Draw rounded rectangle background
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.7f),
            topLeft = backgroundRect.topLeft,
            size = backgroundRect.size,
            cornerRadius = CornerRadius(4.dp.toPx())
        )
    }
    
    drawText(
        textLayoutResult = textResult,
        topLeft = textOffset
    )
    
    // Draw children recursively
    node.children.forEach { child ->
        drawNodesRecursively(child, textMeasurer, colorScheme, imageCache, configDirectory)
    }
}

// Export function for creating PNG with exact canvas matching
suspend fun exportMindMapToPngExact(
    school: School,
    colorScheme: ColorScheme,
    configDirectory: String,
    fileName: String = "${school.name}_mindmap"
): Boolean {
    return ExportUtil.exportMindMapToPng(
        drawFunction = { textMeasurer ->
            // Use the exact same functions as the interactive view with current theme
            val mindMapData = buildMindMapData(school, textMeasurer, colorScheme)
            val imageCache = mutableMapOf<String, ImageBitmap?>() // Fresh cache for export
            layoutNodes(mindMapData, size)
            drawMindMap(mindMapData, this, textMeasurer, colorScheme, imageCache, configDirectory)
        },
        defaultFileName = fileName
    )
}

// Export function for creating PNG (legacy)
suspend fun exportMindMapToPng(
    school: School,
    configDirectory: String,
    fileName: String = "${school.name}_mindmap"
): Boolean {
    // Create a default light color scheme for export
    val exportColorScheme = lightColorScheme()
    
    return ExportUtil.exportMindMapToPng(
        drawFunction = { textMeasurer ->
            // Use the exact same functions as the interactive view
            val mindMapData = buildMindMapData(school, textMeasurer, exportColorScheme)
            val imageCache = mutableMapOf<String, ImageBitmap?>() // Fresh cache for export
            layoutNodes(mindMapData, size)
            drawMindMap(mindMapData, this, textMeasurer, exportColorScheme, imageCache, configDirectory)
        },
        defaultFileName = fileName
    )
}

// Export function for creating SVG with exact canvas matching
suspend fun exportMindMapToSvgExact(
    school: School,
    mindMapData: MindMapNode,
    colorScheme: androidx.compose.material3.ColorScheme,
    fileName: String = "${school.name}_mindmap"
): Boolean {
    return ExportUtil.exportMindMapAsSvgWithExactMatching(
        school = school,
        mindMapData = mindMapData,
        colorScheme = colorScheme,
        defaultFileName = fileName
    )
}

// Export function for creating SVG with exact positions
suspend fun exportMindMapToSvg(
    school: School,
    scopePositions: Map<String, Offset>,
    teacherPositions: Map<String, Offset>,
    fileName: String = "${school.name}_mindmap"
): Boolean {
    return ExportUtil.exportMindMapAsSvgWithPositions(
        school = school,
        scopePositions = scopePositions,
        teacherPositions = teacherPositions,
        defaultFileName = fileName
    )
}

// Helper function to extract positions from mind map data
fun extractPositionsFromMindMapData(rootNode: MindMapNode): Pair<Map<String, Offset>, Map<String, Offset>> {
    val scopePositions = mutableMapOf<String, Offset>()
    val teacherPositions = mutableMapOf<String, Offset>()
    
    // Extract scope positions (direct children of root)
    rootNode.children.forEach { scopeNode ->
        scopePositions[scopeNode.id] = scopeNode.position
        
        // Extract teacher positions (children of scopes)
        scopeNode.children.forEach { teacherNode ->
            teacherPositions[teacherNode.id] = teacherNode.position
        }
    }
    
    return Pair(scopePositions, teacherPositions)
}