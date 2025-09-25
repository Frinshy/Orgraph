package de.frinshy.orgraph.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.frinshy.orgraph.data.models.School
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class MindMapNode(
    val id: String,
    val label: String,
    val color: Color,
    val level: Int,
    var position: Offset = Offset.Zero,
    var targetPosition: Offset = Offset.Zero,
    val children: List<MindMapNode> = emptyList(),
    val size: Float = 60f,
    val type: NodeType = NodeType.SUBJECT
)

enum class NodeType {
    SCHOOL, SUBJECT, TEACHER
}

@Composable
fun MindMapView(
    school: School,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val textMeasurer = rememberTextMeasurer()
    
    // Build mind map structure
    val mindMapData = remember(school) {
        buildMindMapData(school)
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
                .pointerInput(Unit) {
                    detectDragGestures { _, _ ->
                        // Handle drag interactions if needed
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
    }
}

@Composable
fun MindMapLegend(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Legend",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            LegendItem(
                color = MaterialTheme.colorScheme.primary,
                label = "School"
            )
            
            LegendItem(
                color = MaterialTheme.colorScheme.secondary,
                label = "Subjects"
            )
            
            LegendItem(
                color = MaterialTheme.colorScheme.tertiary,
                label = "Teachers"
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun buildMindMapData(school: School): MindMapNode {
    // Root node (school)
    val rootNode = MindMapNode(
        id = school.id,
        label = school.name,
        color = Color(0xFF6750A4), // Primary color
        level = 0,
        size = 80f,
        type = NodeType.SCHOOL
    )
    
    // Subject nodes with teacher children
    val subjectNodes = school.getSubjectsWithTeachers().map { (subject, teachers) ->
        val teacherNodes = teachers.map { teacher ->
            MindMapNode(
                id = teacher.id,
                label = teacher.name,
                color = Color(0xFF7D5260), // Tertiary color
                level = 2,
                size = 40f,
                type = NodeType.TEACHER
            )
        }
        
        MindMapNode(
            id = subject.id,
            label = subject.name,
            color = subject.color,
            level = 1,
            children = teacherNodes,
            size = 60f,
            type = NodeType.SUBJECT
        )
    }
    
    return rootNode.copy(children = subjectNodes)
}

private fun layoutNodes(rootNode: MindMapNode, canvasSize: Size) {
    val centerX = canvasSize.width / 2f
    val centerY = canvasSize.height / 2f
    
    // Position root node at center
    rootNode.position = Offset(centerX, centerY)
    rootNode.targetPosition = Offset(centerX, centerY)
    
    // Layout subject nodes in a circle around the root
    val subjectCount = rootNode.children.size
    val subjectRadius = min(canvasSize.width, canvasSize.height) * 0.3f
    
    rootNode.children.forEachIndexed { index, subjectNode ->
        val angle = (2 * PI * index / subjectCount).toFloat()
        val x = centerX + cos(angle) * subjectRadius
        val y = centerY + sin(angle) * subjectRadius
        subjectNode.position = Offset(x, y)
        subjectNode.targetPosition = Offset(x, y)
        
        // Layout teacher nodes around each subject
        val teacherCount = subjectNode.children.size
        val teacherRadius = 120f
        
        subjectNode.children.forEachIndexed { teacherIndex, teacherNode ->
            val teacherAngle = angle + (2 * PI * teacherIndex / maxOf(teacherCount, 1)).toFloat() * 0.3f
            val teacherX = x + cos(teacherAngle) * teacherRadius
            val teacherY = y + sin(teacherAngle) * teacherRadius
            teacherNode.position = Offset(teacherX, teacherY)
            teacherNode.targetPosition = Offset(teacherX, teacherY)
        }
    }
}

private fun drawMindMap(rootNode: MindMapNode, drawScope: DrawScope, textMeasurer: TextMeasurer) {
    with(drawScope) {
        // Draw connections first
        drawConnectionsRecursively(rootNode)
        
        // Draw nodes with text
        drawNodesRecursively(rootNode, textMeasurer)
    }
}

private fun DrawScope.drawConnectionsRecursively(node: MindMapNode) {
    node.children.forEach { child ->
        drawLine(
            color = Color.Gray.copy(alpha = 0.6f),
            start = node.position,
            end = child.position,
            strokeWidth = 2.dp.toPx()
        )
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
    drawCircle(
        color = Color.White,
        radius = node.size / 2f,
        center = node.position,
        style = Stroke(width = 2.dp.toPx())
    )
    
    // Draw text label
    val textStyle = when (node.type) {
        NodeType.SCHOOL -> TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        NodeType.SUBJECT -> TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
        NodeType.TEACHER -> TextStyle(
            fontSize = 10.sp,
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
    
    // Draw children
    node.children.forEach { child ->
        drawNodesRecursively(child, textMeasurer)
    }
}