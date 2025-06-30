package de.frinshhd.orgraph.testing

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import java.awt.BasicStroke
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
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
    // Make nodes bigger and increase padding
    val nodeRadius = 60f // was 30f
    val nodeWidthMultiplier = 5f // was 3f
    val nodeHeightMultiplier = 2.5f // was 2f
    val fontSize = 28f
    val innerPadding = 32f // px, for text in oval

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
        val margin = 20f // Reduced margin so subnodes are closer to their parent
        val nodeR = nodeRadius

        // Define mind map border (canvas area)
        val borderLeft = 0f
        val borderTop = 0f
        val borderRight = 1000f
        val borderBottom = 800f

        // Recursively compute the bounding radius for each subtree
        fun computeSubtreeRadius(id: Int, visited: MutableSet<Int> = mutableSetOf()): Float {
            if (!visited.add(id)) return nodeR
            val node = nodeMap[id] ?: return nodeR
            if (node.children.isEmpty()) {
                subtreeRadii[id] = nodeR
                return nodeR
            }
            val childRadii = node.children.map { computeSubtreeRadius(it, visited) }
            val n = childRadii.size
            val totalArc = childRadii.sum() + n * (nodeR * 0.5f) // Use a much smaller margin for arc calculation
            val circleRadius = totalArc / (2 * Math.PI).toFloat() + nodeR * 1.2f // Only a little extra space
            subtreeRadii[id] = circleRadius
            return circleRadius
        }

        computeSubtreeRadius(rootId)

        // Helper: check if a circle at pos with radius r collides with any already placed node (using subtree radii)
        fun collidesWithPlaced(pos: Offset, r: Float, excludeId: Int? = null, parentId: Int? = null): Boolean {
            for ((otherId, otherPos) in placed) {
                if (excludeId != null && otherId == excludeId) continue
                // Allow overlap with direct parent
                if (parentId != null && otherId == parentId) continue
                val otherR = subtreeRadii[otherId] ?: nodeR
                if ((pos - otherPos).getDistance() < r + otherR + margin) return true
            }
            return false
        }

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

            val r = subtreeRadii[id] ?: nodeR
            // --- Improved: Calculate required radius based on subnode sizes ---
            val childRadii = node.children.map { subtreeRadii[it] ?: nodeR }
            val childDiameters = childRadii.map { it * 2 + margin }
            val totalArc = childDiameters.sum()
            val minCircleRadius = totalArc / (2 * Math.PI).toFloat() + nodeR * 1.2f
            var baseRadius = minCircleRadius
            val maxRadiusTries = 300
            var radiusTries = 0
            var allClear: Boolean
            var childPositions: List<Offset>
            do {
                allClear = true
                // Place each child at an angle proportional to its diameter
                var currentAngle = angle
                childPositions = List(n) { i ->
                    val theta = currentAngle
                    val childPos = pos + Offset(
                        cos(theta) * baseRadius,
                        sin(theta) * baseRadius
                    )
                    // Angle step proportional to child diameter
                    val angleStep = (childDiameters[i] / totalArc) * (2 * Math.PI).toFloat()
                    currentAngle += angleStep
                    childPos
                }
                // Check all pairs for collisions (siblings)
                for (i in 0 until n) {
                    val childId = node.children[i]
                    val childPos = childPositions[i]
                    val childR = childRadii[i]
                    // Border check
                    if (childPos.x - childR < borderLeft || childPos.x + childR > borderRight ||
                        childPos.y - childR < borderTop || childPos.y + childR > borderBottom
                    ) {
                        allClear = false
                    }
                    // Check with other siblings
                    for (j in 0 until n) {
                        if (i == j) continue
                        val otherPos = childPositions[j]
                        val otherR = childRadii[j]
                        if ((childPos - otherPos).getDistance() < childR + otherR + margin) {
                            allClear = false
                        }
                    }
                    // Check with all already placed nodes (not just siblings), but ignore parent
                    if (collidesWithPlaced(childPos, childR, childId, id)) {
                        allClear = false
                    }
                }
                if (!allClear) baseRadius += 5f // Increase more for faster spacing
                radiusTries++
            } while (!allClear && radiusTries < maxRadiusTries)

            // Place all children at the found radius, symmetrically
            var currentAngle = angle
            for ((i, childId) in node.children.withIndex()) {
                val theta = currentAngle
                val childPos = pos + Offset(
                    cos(theta) * baseRadius,
                    sin(theta) * baseRadius
                )
                val angleStep = (childDiameters[i] / totalArc) * (2 * Math.PI).toFloat()
                currentAngle += angleStep
                placeNode(childId, childPos, theta, visited)
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
    fun getNodeStage(id: Int, nodeMap: Map<Int, MindMapNode>, rootId: Int?, stage: Int = 0, visited: Set<Int> = emptySet()): Int? {
        if (id == rootId) return 0
        if (id in visited) return null // Prevent infinite recursion
        for ((_, node) in nodeMap) {
            if (node.children.contains(id)) {
                val parentStage = getNodeStage(node.id, nodeMap, rootId, stage + 1, visited + id)
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
                    Button(onClick = {
                        // Export mindmap as image with save dialog, auto-crop to fit all nodes and center main node
                        val fileChooser = JFileChooser()
                        fileChooser.dialogTitle = "Export Mindmap as Image"
                        fileChooser.selectedFile = File("mindmap.png")
                        val userSelection = fileChooser.showSaveDialog(null)
                        if (userSelection == JFileChooser.APPROVE_OPTION) {
                            val filePath = fileChooser.selectedFile.absolutePath
                            if (nodes.isNotEmpty()) {
                                // Calculate bounds
                                val minX = nodes.minOf { it.position.x - nodeRadius }
                                val minY = nodes.minOf { it.position.y - nodeRadius }
                                val maxX = nodes.maxOf { it.position.x + nodeRadius }
                                val maxY = nodes.maxOf { it.position.y + nodeRadius }
                                val padding = 40f
                                val width = (maxX - minX + 2 * padding).toInt()
                                val height = (maxY - minY + 2 * padding).toInt()
                                // Find main node (root)
                                val root = nodes.find { node ->
                                    nodes.none { it.children.contains(node.id) }
                                }
                                val rootPos = root?.position ?: Offset(0f, 0f)
                                // Calculate offset to fit all nodes (no centering for now)
                                val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                                val g2d = bufferedImage.createGraphics()
                                g2d.color = java.awt.Color.WHITE
                                g2d.fillRect(0, 0, width, height)
                                // Color palette for export (same as Compose)
                                val stageColors = listOf(
                                    java.awt.Color(0x90, 0xCA, 0xF9), // Blue
                                    java.awt.Color(0xA5, 0xD6, 0xA7), // Green
                                    java.awt.Color(0xFF, 0xF5, 0x9D), // Yellow
                                    java.awt.Color(0xFF, 0xAB, 0x91), // Orange
                                    java.awt.Color(0xCE, 0x93, 0xD8), // Purple
                                    java.awt.Color(0xB0, 0xBE, 0xC5), // Grey
                                    java.awt.Color(0xFF, 0xCD, 0xD2), // Red
                                    java.awt.Color(0x80, 0xCB, 0xC4)  // Teal
                                )
                                fun getNodeStage(id: Int, nodeMap: Map<Int, MindMapNode>, rootId: Int?, stage: Int = 0, visited: Set<Int> = emptySet()): Int? {
                                    if (id == rootId) return 0
                                    if (id in visited) return null // Prevent infinite recursion
                                    for ((_, node) in nodeMap) {
                                        if (node.children.contains(id)) {
                                            val parentStage = getNodeStage(node.id, nodeMap, rootId, stage + 1, visited + id)
                                            return parentStage?.plus(1)
                                        }
                                    }
                                    return null
                                }
                                val rootId = root?.id
                                val nodeMap = nodes.associateBy { it.id }
                                // Draw nodes and connections (match Compose design)
                                nodes.forEach { node ->
                                    val from = Point(
                                        ((node.position.x - minX + padding)).toInt(),
                                        ((node.position.y - minY + padding)).toInt()
                                    )
                                    node.children.forEach { childId ->
                                        val toNode = nodes.find { it.id == childId }
                                        if (toNode != null) {
                                            val to = Point(
                                                ((toNode.position.x - minX + padding)).toInt(),
                                                ((toNode.position.y - minY + padding)).toInt()
                                            )
                                            g2d.color = java.awt.Color.DARK_GRAY
                                            g2d.stroke = BasicStroke(2f)
                                            g2d.drawLine(from.x, from.y, to.x, to.y)
                                        }
                                    }
                                }
                                nodes.forEach { node ->
                                    val x = ((node.position.x - minX + padding) - nodeRadius * 1.5f).toInt()
                                    val y = ((node.position.y - minY + padding) - nodeRadius).toInt()
                                    val w = (nodeRadius * 3f).toInt()
                                    val h = (nodeRadius * 2f).toInt()
                                    val stage = getNodeStage(node.id, nodeMap, rootId) ?: 0
                                    val isSelected = selectedNodeId == node.id
                                    val color = if (isSelected) java.awt.Color.CYAN else if (node.id == rootId) java.awt.Color(0x19, 0x76, 0xD2) else stageColors[stage % stageColors.size]
                                    g2d.color = color
                                    g2d.fillOval(x, y, w, h)
                                    g2d.color = java.awt.Color.BLACK
                                    g2d.drawOval(x, y, w, h)
                                    g2d.font = java.awt.Font("Arial", java.awt.Font.PLAIN, 16)
                                    val fm = g2d.fontMetrics
                                    val labelWidth = fm.stringWidth(node.label)
                                    val labelX = x + w / 2 - labelWidth / 2
                                    val labelY = y + h / 2 + fm.ascent / 2.5 - 4
                                    g2d.color = java.awt.Color.BLACK
                                    g2d.drawString(node.label, labelX, labelY.toInt())
                                }
                                g2d.dispose()
                                ImageIO.write(bufferedImage, "png", File(filePath))
                            }
                        }
                    }) {
                        Text("Export as Image")
                    }
                })
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    // Remove pointerInput for zoom (mouse wheel and pinch)
                    // Only keep drag-to-pan
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offset += dragAmount
                            }
                        )
                    }
            ) {
                Box(Modifier.fillMaxSize()) {
                    // Draw connections in a single Canvas (for performance), now in screen coordinates
                    Canvas(
                        modifier = Modifier.matchParentSize()
                    ) {
                        nodes.forEach { node ->
                            val fromCenter = Offset(
                                node.position.x * scale + offset.x,
                                node.position.y * scale + offset.y
                            )
                            node.children.forEach { childId ->
                                val toNode = nodes.find { it.id == childId }
                                if (toNode != null) {
                                    val toCenter = Offset(
                                        toNode.position.x * scale + offset.x,
                                        toNode.position.y * scale + offset.y
                                    )
                                    drawLine(Color.DarkGray, fromCenter, toCenter, strokeWidth = 2f * scale)
                                }
                            }
                        }
                    }
                    // Draw each node as a separate Composable with clickable
                    nodes.forEach { node ->
                        val stage = getNodeStage(node.id, nodes.associateBy { it.id }, rootId) ?: 0
                        val color = if (node.id == rootId) Color(0xFF1976D2) else stageColors[stage % stageColors.size]
                        val isSelected = node.id == selectedNodeId
                        // --- Dynamic node size calculation ---
                        val font = Font().apply { size = fontSize * scale }
                        val textLine = TextLine.make(node.label, font)
                        val textWidth = textLine.width
                        val textHeight = font.size
                        val nodeWidth = textWidth + innerPadding * 2
                        val nodeHeight = textHeight + innerPadding * 2
                        Box(
                            modifier = Modifier
                                .offset {
                                    val scaled = Offset(
                                        node.position.x * scale + offset.x,
                                        node.position.y * scale + offset.y
                                    )
                                    IntOffset(
                                        (scaled.x - nodeWidth / 2).roundToInt(),
                                        (scaled.y - nodeHeight / 2).roundToInt()
                                    )
                                }
                                .size(nodeWidth.dp, nodeHeight.dp)
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
                                    val displayLine = textLine
                                    val displayWidth = textWidth
                                    val centerY = size.height / 2 + textHeight / 2.5f
                                    it.nativeCanvas.drawTextLine(
                                        displayLine,
                                        size.width / 2 - displayWidth / 2,
                                        centerY,
                                        Paint()
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
