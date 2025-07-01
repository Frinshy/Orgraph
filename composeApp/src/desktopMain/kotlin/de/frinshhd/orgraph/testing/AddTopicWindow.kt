package de.frinshhd.orgraph.testing

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.ChevronRight
import com.konyaco.fluent.icons.regular.Delete
import com.konyaco.fluent.icons.regular.ExpandUpRight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.toBitmap
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.cos
import kotlin.math.sin

data class TopicNode(
    val name: String,
    val image: ImageBitmap? = null,
    val children: MutableList<TopicNode> = mutableStateListOf()
)

suspend fun loadImageBitmap(source: String): ImageBitmap? = withContext(Dispatchers.IO) {
    try {
        return@withContext if (source.startsWith("http://") || source.startsWith("https://")) {
            val img = ImageIO.read(URL(source))
            (img as? java.awt.image.BufferedImage)?.toBitmap()?.asImageBitmap()
        } else {
            loadImageBitmap(File(source).inputStream())
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun AddTopicWindow(onClose: (List<TopicNode>) -> Unit) {
    // Only one root topic, always present
    var rootTopic by remember { mutableStateOf(TopicNode("Main")) }
    var showMindMap by remember { mutableStateOf(false) }
    var rootName by remember { mutableStateOf(rootTopic.name) }

    Window(onCloseRequest = { onClose(listOf(rootTopic)) }, title = "Add Topics & Subtopics") {
        MaterialTheme {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Edit Root Topic", style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
                    Button(onClick = { showMindMap = !showMindMap }) {
                        Text(if (showMindMap) "Edit List" else "Show Mindmap")
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (showMindMap) {
                    Box(modifier = Modifier.weight(1f, fill = true)) {
                        MindMapView(listOf(rootTopic))
                    }
                } else {
                    // Root topic name editor
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = rootName,
                            onValueChange = {
                                rootName = it
                                rootTopic = rootTopic.copy(name = it)
                            },
                            label = { Text("Root Topic Name") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.weight(1f, fill = true)) {
                        TopicTreeEditor(rootTopic.children)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { onClose(listOf(rootTopic)) },
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Done") }
            }
        }
    }
}

@Composable
fun TopicTreeEditor(nodes: MutableList<TopicNode>, isRoot: Boolean = false) {
    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
        itemsIndexed(nodes) { idx, node ->
            TopicNodeEditor(
                node = node,
                isRoot = false,
                onDelete = { nodes.removeAt(idx) },
                onImageChange = { img -> nodes[idx] = nodes[idx].copy(image = img) }
            )
        }
        // Add subtopic input at the end of the list for adding new subtopics
        item {
            var subtopicName by remember { mutableStateOf("") }
            var imageSource by remember { mutableStateOf("") }
            var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
            val coroutineScope = rememberCoroutineScope()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 32.dp, top = 8.dp)
            ) {
                TextField(
                    value = subtopicName,
                    onValueChange = { subtopicName = it },
                    label = { Text("Add Subtopic") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = imageSource,
                    onValueChange = {
                        imageSource = it
                        if (it.isNotBlank()) {
                            coroutineScope.launch {
                                imageBitmap = loadImageBitmap(it)
                            }
                        } else {
                            imageBitmap = null
                        }
                    },
                    label = { Text("Image Path or URL") },
                    modifier = Modifier.width(180.dp)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (subtopicName.isNotBlank()) {
                        nodes.add(TopicNode(subtopicName, imageBitmap))
                        subtopicName = ""
                        imageSource = ""
                        imageBitmap = null
                    }
                }) { Text("Add") }
            }
        }
    }
}

@Composable
fun TopicNodeEditor(
    node: TopicNode,
    isRoot: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onImageChange: ((ImageBitmap?) -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(true) }
    var imageSource by remember { mutableStateOf("") }
    var imageBitmap by remember { mutableStateOf(node.image) }
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandUpRight else Icons.Default.ChevronRight,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            if (imageBitmap != null) {
                // Scale the image preview in the node editor to match the mindmap node size
                androidx.compose.foundation.Image(
                    bitmap = imageBitmap!!,
                    contentDescription = null,
                    modifier = Modifier.size(90.dp) // match mindmap node image size
                )
            }
            Text(node.name, style = MaterialTheme.typography.subtitle1)
            Spacer(Modifier.width(8.dp))
            if (!isRoot && onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            if (onImageChange != null) {
                TextField(
                    value = imageSource,
                    onValueChange = {
                        imageSource = it
                        if (it.isNotBlank()) {
                            coroutineScope.launch {
                                imageBitmap = loadImageBitmap(it)
                                onImageChange(imageBitmap)
                            }
                        } else {
                            imageBitmap = null
                            onImageChange(null)
                        }
                    },
                    label = { Text("Image Path or URL") },
                    modifier = Modifier.width(180.dp)
                )
            }
        }
        if (expanded) {
            TopicTreeEditor(node.children)
        }
    }
}

@Composable
fun MindMapNodeContent(
    node: TopicNode,
    scale: Float,
    isMainNode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val paddingH = 8f.toDp()
    val paddingV = 8f.toDp()

    Column(
        modifier = modifier
            .background(
                color = Color(0xFF90CAF9),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = paddingH, vertical = paddingV)
            .wrapContentWidth(Alignment.CenterHorizontally)
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (node.image != null) {
            Image(
                bitmap = node.image,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .heightIn(max = 80.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = node.name,
            fontSize = 16.sp * scale,
            fontWeight = if (isMainNode) FontWeight.Bold else FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

/**
 * Recursively draws a mind map node, its connecting lines, and its children.
 *
 * The drawing order is:
 *   1. Draw lines from this node to its children (behind all nodes)
 *   2. Draw all child nodes (recursively)
 *   3. Draw this node's content (on top)
 *
 * @param node The topic node to render
 * @param x The x position (top-left) for this node
 * @param y The y position (top-left) for this node
 * @param nodeRadius The base radius for child placement
 * @param angle The starting angle for child placement
 * @param spread The angular spread for child placement
 * @param depth The current depth in the tree
 * @param textMeasurer Used for text layout
 * @param scale The scale factor for layout
 * @param nodeCenters Shared map to store each node's center after measurement
 */
@Composable
fun MindMapNodeAligned(
    node: TopicNode,
    x: Float,
    y: Float,
    nodeRadius: Float,
    angle: Float,
    spread: Float,
    depth: Int,
    textMeasurer: TextMeasurer,
    scale: Float,
    nodeCenters: MutableMap<TopicNode, Offset>
) {
    val childCount = node.children.size
    var nodeSize by remember { mutableStateOf(IntSize.Zero) }
    val baseX = x
    val baseY = y

    // --- Dynamic child radius to avoid collisions ---
    // Only apply for the main node (depth == 0)
    var childRadius = 220f * scale + nodeRadius
    val angleStep = if (childCount > 0) spread / childCount else 0f
    val childNodeSizes = remember { mutableStateListOf<IntSize>() }
    val mainNodeSize = nodeSize
    val maxAttempts = 20
    var collisionDetected: Boolean
    var attempt = 0
    do {
        collisionDetected = false
        val childCenters = node.children.mapIndexed { i, _ ->
            val theta = Math.toRadians((angle + angleStep * i - spread / 2 + angleStep / 2).toDouble())
            val childX = baseX + childRadius * cos(theta).toFloat()
            val childY = baseY + childRadius * sin(theta).toFloat()
            Offset(childX, childY)
        }
        if (depth == 0 && mainNodeSize.width > 0 && mainNodeSize.height > 0 && childNodeSizes.size == childCount) {
            for (i in 0 until childCount) {
                val childCenter = childCenters[i]
                val childSize = childNodeSizes[i]
                val childRect = androidx.compose.ui.geometry.Rect(
                    childCenter.x,
                    childCenter.y,
                    childCenter.x + childSize.width,
                    childCenter.y + childSize.height
                )
                val mainRect = androidx.compose.ui.geometry.Rect(
                    baseX,
                    baseY,
                    baseX + mainNodeSize.width,
                    baseY + mainNodeSize.height
                )
                if (childRect.overlaps(mainRect)) {
                    collisionDetected = true
                    break
                }
            }
        }
        if (collisionDetected) childRadius += 40f * scale
        attempt++
    } while (collisionDetected && attempt < maxAttempts)

    // Draw lines from this node to its children (behind all nodes)
    if (node.children.isNotEmpty() && nodeCenters.containsKey(node) && node.children.all { nodeCenters.containsKey(it) }) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val parentCenter = nodeCenters[node]!!
            val childCenters = node.children.map { nodeCenters[it]!! }
            drawMindMapNodes(
                parent = parentCenter,
                children = childCenters
            )
        }
    }
    // Draw all child nodes recursively
    node.children.forEachIndexed { i, child ->
        val theta = Math.toRadians((angle + angleStep * i - spread / 2 + angleStep / 2).toDouble())
        val childX = baseX + childRadius * cos(theta).toFloat()
        val childY = baseY + childRadius * sin(theta).toFloat()
        MindMapNodeAligned(
            node = child,
            x = childX,
            y = childY,
            nodeRadius = nodeRadius,
            angle = angle + angleStep * i - spread / 2 + angleStep / 2,
            spread = 120f,
            depth = depth + 1,
            textMeasurer = textMeasurer,
            scale = scale,
            nodeCenters = nodeCenters
        )
    }
    // Draw this node's content and record its center after measurement
    Box(
        modifier = Modifier
            .absoluteOffset { IntOffset(baseX.toInt(), baseY.toInt()) }
            .onGloballyPositioned { coordinates ->
                nodeSize = coordinates.size
                nodeCenters[node] = Offset(
                    baseX + nodeSize.width / 2f,
                    baseY + nodeSize.height / 2f
                )
                if (depth == 0) {
                    // Store child node sizes for collision detection
                    childNodeSizes.clear()
                    childNodeSizes.addAll(node.children.map { _ -> coordinates.size })
                }
            }
    ) {
        MindMapNodeContent(node = node, scale = 1f, isMainNode = (depth == 0))
    }
}

// Extension functions to convert Int and Float to Dp
fun Int.toDp(): Dp = this.dp
fun Float.toDp(): Dp = this.dp

fun DrawScope.drawMindMapNodes(
    parent: Offset,
    children: List<Offset>
) {
    // Draw only lines between parent and each child
    children.forEach { child ->
        drawLine(
            color = Color.Black,
            start = parent,
            end = child,
            strokeWidth = 1.5f
        )
    }
}

@Composable
fun MindMapView(topics: List<TopicNode>) {
    val textMeasurer = rememberTextMeasurer()
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var dragStart by remember { mutableStateOf(Offset.Zero) }
    val scale = 1f
    val nodeCenters = remember { mutableStateMapOf<TopicNode, Offset>() }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.pressed }) {
                            val dragChange = event.changes.firstOrNull { it.pressed }
                            if (dragChange != null) {
                                val dragPosition = dragChange.position
                                if (!isDragging) {
                                    isDragging = true
                                    dragStart = dragPosition
                                    lastOffset = offset
                                } else {
                                    val dragDelta = dragPosition - dragStart
                                    offset = lastOffset + dragDelta
                                }
                            }
                        } else if (isDragging) {
                            isDragging = false
                        }
                    }
                }
            }
    ) {
        val centerX = maxWidth.value / 2f
        val centerY = maxHeight.value / 2f
        val nodeRadius = 40f * scale
        if (topics.isNotEmpty()) {
            MindMapNodeAligned(
                node = topics[0],
                x = centerX + offset.x,
                y = centerY + offset.y,
                nodeRadius = nodeRadius,
                angle = -90f,
                spread = 360f,
                depth = 0,
                textMeasurer = textMeasurer,
                scale = scale,
                nodeCenters = nodeCenters
            )
        }
    }
}

@Preview
@Composable
fun PreviewAddTopicWindow() {
    AddTopicWindow(onClose = {})
}

fun main() = application {
    AddTopicWindow { topics ->
        println("Topics added: $topics")
        exitApplication()
    }
}