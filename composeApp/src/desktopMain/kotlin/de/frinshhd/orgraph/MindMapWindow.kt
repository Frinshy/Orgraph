package de.frinshhd.orgraph

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.skia.Font
import org.jetbrains.skia.TextLine
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * MindMap Editor Instructions:
 *
 * - Zoom in and out: Use your mouse wheel to zoom the mind map in and out.
 * - Pan/navigate: Click and drag with your mouse to move around the mind map.
 */

// Represents a single node in the mind map
data class MindMapNode(
    val id: Int,
    var label: String,
    var position: Offset,
    val children: MutableList<Int> = mutableListOf()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMapWindow() {
    var nodes by remember { mutableStateOf(listOf<MindMapNode>()) }
    var nextId by remember { mutableStateOf(0) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var selectedNodeId by remember { mutableStateOf<Int?>(null) }
    val nodeRadius = 30f

    fun isOverlapping(pos: Offset, excludeId: Int? = null): Boolean {
        return nodes.any { node ->
            (excludeId == null || node.id != excludeId) && (node.position - pos).getDistance() < nodeRadius * 2f
        }
    }

    fun findFreePosition(near: Offset): Offset {
        val step = 40f
        for (angle in 0 until 360 step 15) {
            val rad = Math.toRadians(angle.toDouble())
            val candidate = near + Offset((cos(rad) * step).toFloat(), (sin(rad) * step).toFloat())
            if (!isOverlapping(candidate)) return candidate
        }
        // fallback: spiral out
        var r = step * 2
        while (r < 1000f) {
            for (angle in 0 until 360 step 15) {
                val rad = Math.toRadians(angle.toDouble())
                val candidate = near + Offset((cos(rad) * r).toFloat(), (sin(rad) * r).toFloat())
                if (!isOverlapping(candidate)) return candidate
            }
            r += step
        }
        return near // fallback
    }

    // --- True non-overlapping circle-packing mindmap layout ---
    fun layoutPackedMindMap(
        nodes: List<MindMapNode>,
        rootId: Int?,
        center: Offset
    ): List<MindMapNode> {
        if (rootId == null) return nodes
        val nodeMap = nodes.associateBy { it.id }
        val placed = mutableMapOf<Int, Offset>()
        val subtreeRadii = mutableMapOf<Int, Float>()
        val margin = 10f
        val nodeR = nodeRadius

        // Define mind map border (canvas area)
        val borderLeft = 0f
        val borderTop = 0f
        val borderRight = 1000f
        val borderBottom = 800f

        // Recursively compute the bounding radius for each subtree
        // Distribute subnodes evenly around their parent, dynamically adjust distance to avoid collisions with subsubnodes and ensure even, larger spacing
        fun computeSubtreeRadius(id: Int, visited: MutableSet<Int> = mutableSetOf()): Float {
            if (!visited.add(id)) return nodeR
            val node = nodeMap[id] ?: return nodeR
            if (node.children.isEmpty()) {
                subtreeRadii[id] = nodeR
                return nodeR
            }
            val childRadii = node.children.map { computeSubtreeRadius(it, visited) }
            val n = childRadii.size
            val angleStep = (2 * Math.PI / n).toFloat()
            // Arrange children in a circle, sum their subtree radii plus margin
            val totalArc = childRadii.sum() + n * margin
            val circleRadius = totalArc / (2 * Math.PI).toFloat() + nodeR + margin * 2
            subtreeRadii[id] = circleRadius
            return circleRadius
        }

        computeSubtreeRadius(rootId)

        // Place nodes recursively
        fun placeNode(
            id: Int,
            pos: Offset,
            angle: Float,
            visited: MutableSet<Int>,
            parentBaseRadius: Float = 0f
        ) {
            if (!visited.add(id)) return
            val node = nodeMap[id] ?: return
            val n = node.children.size
            if (n == 0) {
                placed[id] = pos
                return
            }

            val margin = 20f
            val r = subtreeRadii[id] ?: nodeR
            var baseRadius = nodeR + margin
            val maxRadiusTries = 100
            var radiusTries = 0
            var allClear: Boolean
            var childPositions: List<Offset>
            do {
                allClear = true
                childPositions = List(n) { i ->
                    val childAngle = (2 * Math.PI * i / n).toFloat() + angle
                    pos + Offset(
                        cos(childAngle) * baseRadius,
                        sin(childAngle) * baseRadius
                    )
                }
                // Check all pairs for collisions (only siblings), and border crossing
                for ((i, childId) in node.children.withIndex()) {
                    val childPos = childPositions[i]
                    val r = subtreeRadii[childId] ?: nodeR
                    // Border check
                    if (childPos.x - r < borderLeft || childPos.x + r > borderRight ||
                        childPos.y - r < borderTop || childPos.y + r > borderBottom
                    ) {
                        allClear = false
                    }
                    // Check with other siblings (subtree-to-subtree)
                    for (j in 0 until n) {
                        if (i == j) continue
                        val otherPos = childPositions[j]
                        val otherR = subtreeRadii[node.children[j]] ?: nodeR
                        val minDist = r + otherR + margin
                        if ((childPos - otherPos).getDistance() < minDist) {
                            allClear = false
                        }
                    }
                }
                if (!allClear) baseRadius += 5f
                radiusTries++
            } while (!allClear && radiusTries < maxRadiusTries)

            // Place all children at the found radius, symmetrically
            for ((i, childId) in node.children.withIndex()) {
                val childAngle = (2 * Math.PI * i / n).toFloat() + angle
                val childPos = pos + Offset(
                    cos(childAngle) * baseRadius,
                    sin(childAngle) * baseRadius
                )
                placeNode(childId, childPos, childAngle, visited)
            }
            placed[id] = pos
        }

        placeNode(rootId, center, 0f, mutableSetOf())
        return nodes.map { n -> n.copy(position = placed[n.id] ?: n.position) }
    }

    // Find root node (no parent)
    fun findRootId(nodes: List<MindMapNode>): Int? {
        val allIds = nodes.map { it.id }.toSet()
        val childIds = nodes.flatMap { it.children }.toSet()
        return (allIds - childIds).firstOrNull()
    }

    // Color palette for stages
    val stageColors = listOf(
        Color(0xFF90CAF9), // Blue
        Color(0xFFA5D6A7), // Green
        Color(0xFFFFF59D), // Yellow
        Color(0xFFFFAB91), // Orange
        Color(0xFFCE93D8), // Purple
        Color(0xFFB0BEC5), // Grey
        Color(0xFFFFCDD2), // Red
        Color(0xFF80CBC4)  // Teal
    )

    // Helper to get stage (depth) of a node
    fun getNodeStage(id: Int, nodeMap: Map<Int, MindMapNode>, rootId: Int?, stage: Int = 0): Int? {
        if (id == rootId) return 0
        for ((_, node) in nodeMap) {
            if (node.children.contains(id)) {
                val parentStage = getNodeStage(node.id, nodeMap, rootId, stage + 1)
                return parentStage?.plus(1)
            }
        }
        return null
    }

    // Only allow one main node (root) that can't be removed
    val rootId = remember(nodes) { findRootId(nodes) ?: 0 }
    val centerScreen = Offset(500f, 400f)
    LaunchedEffect(Unit) {
        if (nodes.isEmpty()) {
            nodes = listOf(MindMapNode(id = 0, label = "Main Node", position = centerScreen))
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("MindMap Editor") }, actions = {
                    Button(onClick = {
                        // Always allow adding to root, even if it's the only node
                        val parentId = selectedNodeId ?: rootId
                        val newNode = MindMapNode(
                            id = nextId++,
                            label = "Node $nextId",
                            position = Offset.Zero
                        )
                        var newNodes = nodes + newNode
                        newNodes = newNodes.map {
                            if (it.id == parentId) it.copy(children = (it.children + newNode.id) as MutableList<Int>)
                            else it
                        }
                        nodes = layoutPackedMindMap(newNodes, rootId, centerScreen)
                    }) {
                        Text("Add Node")
                    }
                    Button(onClick = {
                        if (selectedNodeId != null && selectedNodeId != rootId) {
                            nodes = nodes.filter { it.id != selectedNodeId }
                                .map { it.copy(children = it.children.filter { childId -> childId != selectedNodeId } as MutableList<Int>) }
                        }
                        selectedNodeId = null
                    }) {
                        Text("Remove Node")
                    }
                    Button(onClick = {
                        nodes = layoutPackedMindMap(nodes, rootId, centerScreen)
                    }) {
                        Text("Re-layout")
                    }
                    Button(onClick = {
                        offset = Offset.Zero
                        scale = 1f
                    }) {
                        Text("Reset View")
                    }
                    Button(onClick = {
                        // Reset the whole mindmap: clear all nodes and add a new root node
                        nodes = listOf(MindMapNode(id = 0, label = "Main Node", position = centerScreen))
                        nextId = 1
                        selectedNodeId = null
                        offset = Offset.Zero
                        scale = 1f
                    }) {
                        Text("Reset Mindmap")
                    }
                })
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    // Mouse wheel zoom (should be first for Compose event priority)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                                if (abs(scrollDelta) > 0.1f) { // Only zoom if scrollDelta is significant
                                    val mouse = event.changes.first().position
                                    val oldScale = scale
                                    val zoomFactor = 1.1f
                                    val newScale =
                                        (if (scrollDelta > 0) scale * zoomFactor else scale / zoomFactor).coerceIn(
                                            0.05f,
                                            5f
                                        )
                                    val mouseWorld = (mouse - offset) / oldScale
                                    offset += (mouseWorld * (oldScale - newScale))
                                    scale = newScale
                                }
                            }
                        }
                    }
                    // Mouse drag pan (move mindmap)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offset += dragAmount
                            }
                        )
                    }
                    // Pinch zoom and pan (touchpad/trackpad)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            offset += pan
                            scale = (scale * zoom).coerceIn(0.05f, 5f)
                        }
                    }
            ) {
                Box(Modifier.fillMaxSize()) {
                    // Draw connections in a single Canvas (for performance), now in screen coordinates
                    Canvas(
                        modifier = Modifier.matchParentSize()
                    ) {
                        nodes.forEach { node ->
                            val from = Offset(
                                node.position.x * scale + offset.x,
                                node.position.y * scale + offset.y
                            )
                            node.children.forEach { childId ->
                                val toNode = nodes.find { it.id == childId }
                                if (toNode != null) {
                                    val to = Offset(
                                        toNode.position.x * scale + offset.x,
                                        toNode.position.y * scale + offset.y
                                    )
                                    drawLine(Color.DarkGray, from, to, strokeWidth = 2f * scale)
                                }
                            }
                        }
                    }
                    // Draw each node as a separate Composable with clickable
                    nodes.forEach { node ->
                        val stage = getNodeStage(node.id, nodes.associateBy { it.id }, rootId) ?: 0
                        val color = if (node.id == rootId) Color(0xFF1976D2) else stageColors[stage % stageColors.size]
                        val isSelected = node.id == selectedNodeId
                        Box(
                            modifier = Modifier
                                .offset {
                                    // Apply pan/zoom to node position
                                    val scaled = Offset(
                                        node.position.x * scale + offset.x,
                                        node.position.y * scale + offset.y
                                    )
                                    IntOffset(
                                        scaled.x.roundToInt() - (nodeRadius * scale * 1.5f).roundToInt(),
                                        scaled.y.roundToInt() - (nodeRadius * scale).roundToInt()
                                    )
                                }
                                .size((nodeRadius * 3f * scale).dp, (nodeRadius * 2f * scale).dp)
                                .pointerInput(node.id, scale, offset) {
                                    detectTapGestures {
                                        selectedNodeId = node.id
                                    }
                                }
                        ) {
                            Canvas(Modifier.fillMaxSize()) {
                                drawOval(
                                    color = if (isSelected) Color.Cyan else color,
                                    topLeft = Offset.Zero,
                                    size = Size(size.width, size.height)
                                )
                                drawIntoCanvas {
                                    val font = Font().apply { size = 16f * scale }
                                    val textLine = TextLine.make(node.label, font)
                                    val textWidth = textLine.width
                                    val textHeight = font.size
                                    val centerX = size.width / 2 - textWidth / 2
                                    val centerY = size.height / 2 + textHeight / 2.5f
                                    it.nativeCanvas.drawTextLine(
                                        textLine,
                                        centerX,
                                        centerY,
                                        org.jetbrains.skia.Paint()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    MindMapWindow()
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "MindMap App") {
        MindMapWindow()
    }
}
