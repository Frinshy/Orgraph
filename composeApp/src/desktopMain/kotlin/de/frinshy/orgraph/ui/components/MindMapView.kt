package de.frinshy.orgraph.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.frinshy.orgraph.data.models.School
import de.frinshy.orgraph.util.ExportUtil
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
    val type: NodeType = NodeType.SCOPE
)

enum class NodeType {
    SCHOOL, SCOPE, TEACHER
}

@Composable
fun MindMapView(
    school: School,
    modifier: Modifier = Modifier,
    onPositionsReady: ((scopePositions: Map<String, Offset>, teacherPositions: Map<String, Offset>) -> Unit)? = null,
    onMindMapDataReady: ((mindMapData: MindMapNode) -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }
    val textMeasurer = rememberTextMeasurer()
    
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
                    translationX = panOffset.x,
                    translationY = panOffset.y
                )
                .pointerInput(Unit) {
                    detectTransformGestures(
                        panZoomLock = true
                    ) { _, pan, zoom, _ ->
                        // Handle pan
                        panOffset += pan / scale
                        
                        // Handle zoom with constraints
                        val newScale = (scale * zoom).coerceIn(0.3f, 3f)
                        scale = newScale
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        panOffset += dragAmount / scale
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
                
                drawMindMap(mindMapData, this, textMeasurer, colorScheme)
            }
        }
        
        // Legend
        MindMapLegend(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
        
        // Zoom controls (optional)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "Zoom: ${(scale * 100).toInt()}%",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "Drag to pan • Pinch to zoom",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun MindMapLegend(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Legend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "School",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
                Text(
                    text = "Scopes",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary)
                )
                Text(
                    text = "Teachers",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun buildMindMapData(school: School, textMeasurer: TextMeasurer, colorScheme: ColorScheme): MindMapNode {
    // Root node (school)
    val schoolTextStyle = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
    val schoolTextResult = textMeasurer.measure(school.name, schoolTextStyle)
    val schoolSize = maxOf(
        schoolTextResult.size.width + 32f,
        schoolTextResult.size.height + 32f,
        80f // minimum size
    )
    
    val rootNode = MindMapNode(
        id = school.id,
        label = school.name,
        color = colorScheme.primary, // Use theme primary color
        level = 0,
        size = schoolSize,
        type = NodeType.SCHOOL
    )
    
    // Scope nodes with teacher children
    val scopeNodes = school.getScopesWithTeachers().map { (scope, teachers) ->
        // Calculate scope size based on text
        val scopeTextStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        val scopeTextResult = textMeasurer.measure(scope.name, scopeTextStyle)
        val scopeSize = maxOf(
            scopeTextResult.size.width + 24f,
            scopeTextResult.size.height + 24f,
            60f // minimum size
        )
        
        val teacherNodes = teachers.map { teacher ->
            // Calculate teacher size based on text
            val teacherTextStyle = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
            val teacherTextResult = textMeasurer.measure(teacher.name, teacherTextStyle)
            val teacherSize = maxOf(
                teacherTextResult.size.width + 20f,
                teacherTextResult.size.height + 20f,
                40f // minimum size
            )
            
            MindMapNode(
                id = teacher.id,
                label = teacher.name,
                color = colorScheme.tertiary, // Use theme tertiary color
                level = 2,
                size = teacherSize,
                type = NodeType.TEACHER
            )
        }
        
        MindMapNode(
            id = scope.id,
            label = scope.name,
            color = scope.color,
            level = 1,
            children = teacherNodes,
            size = scopeSize,
            type = NodeType.SCOPE
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

private fun drawMindMap(rootNode: MindMapNode, drawScope: DrawScope, textMeasurer: TextMeasurer, colorScheme: ColorScheme) {
    with(drawScope) {
        // Draw connections first
        drawConnectionsRecursively(rootNode, colorScheme)
        
        // Draw nodes on top
        drawNodesRecursively(rootNode, textMeasurer, colorScheme)
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

private fun DrawScope.drawNodesRecursively(node: MindMapNode, textMeasurer: TextMeasurer, colorScheme: ColorScheme) {
    // Draw node circle
    drawCircle(
        color = node.color,
        radius = node.size / 2f,
        center = node.position
    )
    
    // Draw node border with theme-aware colors
    val borderColor = when (node.type) {
        NodeType.SCHOOL -> colorScheme.onPrimary
        NodeType.SCOPE -> colorScheme.onSecondary.copy(alpha = 0.8f)
        NodeType.TEACHER -> colorScheme.onTertiary.copy(alpha = 0.6f)
    }
    
    drawCircle(
        color = borderColor,
        radius = node.size / 2f,
        center = node.position,
        style = Stroke(width = 2.dp.toPx())
    )
    
    // Draw text label with theme-aware colors
    val textStyle = when (node.type) {
        NodeType.SCHOOL -> TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onPrimary
        )
        NodeType.SCOPE -> TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSecondary
        )
        NodeType.TEACHER -> TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = colorScheme.onTertiary
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
    
    drawText(
        textLayoutResult = textResult,
        topLeft = textOffset
    )
    
    // Draw children recursively
    node.children.forEach { child ->
        drawNodesRecursively(child, textMeasurer, colorScheme)
    }
}

// Export function for creating PNG with exact canvas matching
suspend fun exportMindMapToPngExact(
    school: School,
    colorScheme: ColorScheme,
    fileName: String = "${school.name}_mindmap"
): Boolean {
    return ExportUtil.exportMindMapToPng(
        drawFunction = { textMeasurer ->
            // Use the exact same functions as the interactive view with current theme
            val mindMapData = buildMindMapData(school, textMeasurer, colorScheme)
            layoutNodes(mindMapData, size)
            drawMindMap(mindMapData, this, textMeasurer, colorScheme)
        },
        defaultFileName = fileName
    )
}

// Export function for creating PNG (legacy)
suspend fun exportMindMapToPng(
    school: School,
    fileName: String = "${school.name}_mindmap"
): Boolean {
    // Create a default light color scheme for export
    val exportColorScheme = lightColorScheme()
    
    return ExportUtil.exportMindMapToPng(
        drawFunction = { textMeasurer ->
            // Use the exact same functions as the interactive view
            val mindMapData = buildMindMapData(school, textMeasurer, exportColorScheme)
            layoutNodes(mindMapData, size)
            drawMindMap(mindMapData, this, textMeasurer, exportColorScheme)
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