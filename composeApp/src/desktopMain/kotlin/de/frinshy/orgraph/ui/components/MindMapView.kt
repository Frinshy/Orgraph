package de.frinshy.orgraph.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }
    val textMeasurer = rememberTextMeasurer()
    
    // Build mind map structure
    val mindMapData = remember(school) {
        buildMindMapData(school, textMeasurer)
    }
    
    // Layout nodes when canvas size changes
    LaunchedEffect(mindMapData, canvasSize) {
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            layoutNodes(mindMapData, canvasSize)
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
            canvasSize = size
            drawMindMap(mindMapData, this, textMeasurer)
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

private fun buildMindMapData(school: School, textMeasurer: TextMeasurer): MindMapNode {
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
        color = Color(0xFF6750A4), // Primary color
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
                color = Color(0xFF7D5260), // Tertiary color
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

private fun drawMindMap(rootNode: MindMapNode, drawScope: DrawScope, textMeasurer: TextMeasurer) {
    with(drawScope) {
        // Draw connections first
        drawConnectionsRecursively(rootNode)
        
        // Draw nodes on top
        drawNodesRecursively(rootNode, textMeasurer)
    }
}

private fun DrawScope.drawConnectionsRecursively(node: MindMapNode) {
    // Draw lines to children
    node.children.forEach { child ->
        drawLine(
            color = Color.Gray.copy(alpha = 0.6f),
            start = node.position,
            end = child.position,
            strokeWidth = 2.dp.toPx()
        )
        
        // Recursively draw child connections
        drawConnectionsRecursively(child)
    }
}

private fun DrawScope.drawNodesRecursively(node: MindMapNode, textMeasurer: TextMeasurer) {
    // Draw node circle
    drawCircle(
        color = node.color,
        radius = node.size / 2f,
        center = node.position
    )
    
    // Draw node border
    val borderColor = when (node.type) {
        NodeType.SCHOOL -> Color.White
        NodeType.SCOPE -> Color.White.copy(alpha = 0.8f)
        NodeType.TEACHER -> Color.White.copy(alpha = 0.6f)
    }
    
    drawCircle(
        color = borderColor,
        radius = node.size / 2f,
        center = node.position,
        style = Stroke(width = 2.dp.toPx())
    )
    
    // Draw text label
    val textStyle = when (node.type) {
        NodeType.SCHOOL -> TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        NodeType.SCOPE -> TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        NodeType.TEACHER -> TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White
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
        drawNodesRecursively(child, textMeasurer)
    }
}

// Export function for creating PNG
suspend fun exportMindMapToPng(
    school: School,
    fileName: String = "${school.name}_mindmap"
): Boolean {
    return ExportUtil.exportMindMapToPng(
        drawFunction = { textMeasurer ->
            // Use the exact same functions as the interactive view
            val mindMapData = buildMindMapData(school, textMeasurer)
            layoutNodes(mindMapData, size)
            drawMindMap(mindMapData, this, textMeasurer)
        },
        defaultFileName = fileName
    )
}