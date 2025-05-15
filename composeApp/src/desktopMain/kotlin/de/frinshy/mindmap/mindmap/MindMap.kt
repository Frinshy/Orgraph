package de.frinshy.mindmap.mindmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konyaco.fluent.component.Button
import com.konyaco.fluent.component.Text
import mindmap.composeapp.generated.resources.Res
import org.jetbrains.skia.FontWeight
import org.jetbrains.skia.TextBlob
import org.jetbrains.skia.TextLine
import org.jetbrains.skiko.currentSystemTheme
import kotlin.math.hypot

@Composable
fun MindMapUI() {
    val rootNodes = remember {
        mutableStateListOf(
            MindMapNode(text = "Root", position = Offset(400f, 300f))
        )
    }

    var selectedNode by remember { mutableStateOf<MindMapNode?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            InteractiveMindMap(
                nodes = rootNodes,
                selectedNode = selectedNode,
                onSelect = { selectedNode = it },
                onDrag = { node, dragAmount ->
                    node.position += dragAmount
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                selectedNode?.let { parentNode ->
                    var radius = 150f // Initial distance from the main bubble
                    val childCount = parentNode.children.size + 1
                    val angleStep = 360f / childCount

                    // Function to check if two bubbles collide
                    fun isColliding(pos1: Offset, pos2: Offset, minDistance: Float): Boolean {
                        return pos1.getDistance(pos2) < minDistance
                    }

                    // Get all existing nodes (including sub-bubbles of sub-bubbles)
                    val allNodes = rootNodes.flattened()

                    // Adjust positions of existing children
                    parentNode.children.forEachIndexed { index, child ->
                        val angle = Math.toRadians((index * angleStep).toDouble())
                        child.position = parentNode.position + Offset(
                            (radius * Math.cos(angle)).toFloat(),
                            (radius * Math.sin(angle)).toFloat()
                        )
                    }

                    // Add new child and ensure no collisions
                    val newChild = MindMapNode(
                        text = "Child",
                        position = parentNode.position
                    )
                    var angle = Math.toRadians((parentNode.children.size * angleStep).toDouble())
                    var newPosition: Offset
                    val minDistance = 100f // Minimum distance between bubbles
                    var attempts = 0 // Prevent infinite loops

                    do {
                        newPosition = parentNode.position + Offset(
                            (radius * Math.cos(angle)).toFloat(),
                            (radius * Math.sin(angle)).toFloat()
                        )
                        angle += Math.toRadians(10.0) // Increment angle to avoid collision
                        attempts++

                        // Increase radius if too many attempts
                        if (attempts % 36 == 0) {
                            radius += 50f
                        }
                    } while (allNodes.any { isColliding(it.position, newPosition, minDistance) } && attempts < 360)

                    if (attempts < 360) {
                        newChild.position = newPosition
                        parentNode.children.add(newChild)
                    }
                }
            }) {
                Text("Add Subbubble")
            }
        }
    }
}

@Composable
fun InteractiveMindMap(
    nodes: List<MindMapNode>,
    selectedNode: MindMapNode?,
    onSelect: (MindMapNode?) -> Unit,
    onDrag: (MindMapNode, Offset) -> Unit
) {
    val textMeasurer: androidx.compose.ui.text.TextMeasurer = rememberTextMeasurer()

    val textLayouts = remember(nodes) {
        nodes.flattened().associateWith { node ->
            val textStyle = TextStyle(
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            textMeasurer.measure(AnnotatedString(node.text), style = textStyle)
        }
    }

    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = { offset ->
                    val closest = nodes.flattened().findClosest(offset)
                    onSelect(closest)
                }
            )
        }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    val closest = nodes.flattened().findClosest(offset)
                    onSelect(closest)
                },
                onDrag = { change, dragAmount ->
                    selectedNode?.let {
                        onDrag(it, dragAmount)
                    }
                    change.consume()
                }
            )
        }
    ) {
        drawNodesRecursive(nodes, selectedNode, textLayouts)
    }
}

fun DrawScope.drawNodesRecursive(
    nodes: List<MindMapNode>,
    selectedNode: MindMapNode?,
    textLayouts: Map<MindMapNode, androidx.compose.ui.text.TextLayoutResult>
) {
    for (node in nodes) {
        for (child in node.children) {
            drawLine(
                color = Color.Gray,
                start = node.position,
                end = child.position,
                strokeWidth = 3f
            )
            drawNodesRecursive(listOf(child), selectedNode, textLayouts)
        }

        val isSelected = node.id == selectedNode?.id
        drawCircle(
            color = if (isSelected) Color.Red else Color.Cyan,
            center = node.position,
            radius = 40f
        )

        val textLayoutResult = textLayouts[node] ?: continue
        drawText(
            textLayoutResult,
            topLeft = node.position - Offset(textLayoutResult.size.width / 2f, 50f)
        )
    }
}
fun List<MindMapNode>.flattened(): List<MindMapNode> {
    return this.flatMap { listOf(it) + it.children.flattened() }
}

fun List<MindMapNode>.findClosest(offset: Offset): MindMapNode? {
    return this.minByOrNull { it.position.getDistance(offset) }
}

fun Offset.getDistance(other: Offset): Float {
    return hypot((x - other.x), (y - other.y))
}