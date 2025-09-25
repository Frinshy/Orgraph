package de.frinshhd.orgraph.mindmap

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Main composable for rendering the mindmap with ELK-based layout
 */
@Composable
fun ElkMindMapCanvas(
    mindMapNode: MindMapNode,
    algorithm: LayoutAlgorithm = LayoutAlgorithm.RADIAL,
    modifier: Modifier = Modifier
) {
    var layout by remember(mindMapNode, algorithm) { mutableStateOf<MindMapLayout?>(null) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }

    val layoutEngine = remember { ElkMindMapLayoutEngine() }
    val textMeasurer = rememberTextMeasurer()

    // Calculate layout when mindmap changes
    LaunchedEffect(mindMapNode, algorithm) {
        layout = layoutEngine.layout(mindMapNode, algorithm)
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Algorithm selection buttons
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LayoutAlgorithm.values().forEach { algo ->
                Button(
                    onClick = {
                        layout = layoutEngine.layout(mindMapNode, algo)
                    },
                    colors = if (algorithm == algo) {
                        ButtonDefaults.buttonColors()
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text(algo.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }

        // Reset view button
        Button(
            onClick = {
                panOffset = Offset.Zero
                scale = 1f
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Reset View")
        }

        // Main canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        panOffset += pan
                        scale = (scale * zoom).coerceIn(0.1f, 3f)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        panOffset += dragAmount
                    }
                }
        ) {
            layout?.let { currentLayout ->
                drawMindMap(
                    layout = currentLayout,
                    mindMapNode = mindMapNode,
                    textMeasurer = textMeasurer,
                    panOffset = panOffset,
                    scale = scale
                )
            }
        }
    }
}

/**
 * Draws the complete mindmap including nodes and edges
 */
private fun DrawScope.drawMindMap(
    layout: MindMapLayout,
    mindMapNode: MindMapNode,
    textMeasurer: TextMeasurer,
    panOffset: Offset,
    scale: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2

    // Calculate offset to center the mindmap
    val layoutCenterX = layout.bounds.x + layout.bounds.width / 2
    val layoutCenterY = layout.bounds.y + layout.bounds.height / 2
    val layoutOffset = Offset(
        centerX - layoutCenterX * scale + panOffset.x,
        centerY - layoutCenterY * scale + panOffset.y
    )

    translate(layoutOffset.x, layoutOffset.y) {
        scale(scale) {
            // Draw edges first (behind nodes)
            layout.edges.forEach { edge ->
                drawEdge(edge, layout.nodes)
            }

            // Draw nodes on top
            layout.nodes.forEach { layoutNode ->
                val mindMapNodeData = findMindMapNode(mindMapNode, layoutNode.id)
                drawNode(layoutNode, mindMapNodeData, textMeasurer)
            }
        }
    }
}

/**
 * Draws a single edge between two nodes
 */
private fun DrawScope.drawEdge(
    edge: LayoutEdge,
    nodes: List<LayoutNode>
) {
    val sourceNode = nodes.find { it.id == edge.sourceId }
    val targetNode = nodes.find { it.id == edge.targetId }

    if (sourceNode != null && targetNode != null) {
        val startX = sourceNode.x + sourceNode.width / 2
        val startY = sourceNode.y + sourceNode.height / 2
        val endX = targetNode.x + targetNode.width / 2
        val endY = targetNode.y + targetNode.height / 2

        if (edge.points.isNotEmpty()) {
            // Draw curved path using bend points
            val path = Path()
            path.moveTo(startX, startY)

            edge.points.forEach { (x, y) ->
                path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = Color.Gray,
                style = Stroke(width = 2.dp.toPx())
            )
        } else {
            // Draw straight line
            drawLine(
                color = Color.Gray,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

/**
 * Draws a single node with text and optional image
 */
private fun DrawScope.drawNode(
    layoutNode: LayoutNode,
    mindMapNodeData: MindMapNode?,
    textMeasurer: TextMeasurer
) {
    val isRootNode = mindMapNodeData?.id == mindMapNodeData?.id // Simplified check
    val nodeColor = if (isRootNode) Color(0xFF4CAF50) else Color(0xFF2196F3)
    val textColor = Color.White

    // Draw node background
    drawRoundRect(
        color = nodeColor,
        topLeft = Offset(layoutNode.x, layoutNode.y),
        size = Size(layoutNode.width, layoutNode.height),
        cornerRadius = CornerRadius(8.dp.toPx())
    )

    // Draw node border
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(layoutNode.x, layoutNode.y),
        size = Size(layoutNode.width, layoutNode.height),
        cornerRadius = CornerRadius(8.dp.toPx()),
        style = Stroke(width = 1.dp.toPx())
    )

    // Draw text
    val text = mindMapNodeData?.text ?: layoutNode.label
    val textStyle = TextStyle(
        color = textColor,
        fontSize = 12.sp,
        fontWeight = if (isRootNode) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center
    )

    val textLayoutResult = textMeasurer.measure(text, textStyle)
    val textX = layoutNode.x + (layoutNode.width - textLayoutResult.size.width) / 2
    val textY = layoutNode.y + (layoutNode.height - textLayoutResult.size.height) / 2

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(textX, textY)
    )
}

/**
 * Helper function to find the original MindMapNode data by ID
 */
private fun findMindMapNode(root: MindMapNode, id: String): MindMapNode? {
    if (root.id == id) return root

    root.children.forEach { child ->
        val found = findMindMapNode(child, id)
        if (found != null) return found
    }

    return null
}
