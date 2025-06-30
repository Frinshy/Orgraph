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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
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
import kotlin.math.max
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
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val nodeTextLayout = textMeasurer.measure(
        text = node.name,
        style = TextStyle(
            color = Color.Black,
            fontSize = 16.sp * scale,
            fontWeight = FontWeight.Bold
        )
    )
    val paddingH = (20f * scale).toDp()
    val paddingV = (12f * scale).toDp()
    val nodeWidth = (max(nodeTextLayout.size.width.toFloat(), node.image?.width?.toFloat() ?: 0f) + paddingH.value * 2).dp
    Column(
        modifier = modifier
            .width(nodeWidth)
            .wrapContentHeight()
            .padding(horizontal = paddingH, vertical = paddingV)
            .background(
                color = Color(0xFF90CAF9),
                shape = MaterialTheme.shapes.medium
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (node.image != null) {
            Image(
                bitmap = node.image,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = node.name,
            fontSize = 16.sp * scale,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

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
    scale: Float
) {
    val childCount = node.children.size
    val childRadius = 220f * scale + nodeRadius
    val angleStep = if (childCount > 0) spread / childCount else 0f
    var nodeSize by remember { mutableStateOf(IntSize.Zero) }
    val baseX = x
    val baseY = y
    // Calculate child positions (do not apply scale to position, only to node size)
    val childCenters = node.children.mapIndexed { i, _ ->
        val theta = Math.toRadians((angle + angleStep * i - spread / 2 + angleStep / 2).toDouble())
        val childX = baseX + childRadius * cos(theta).toFloat()
        val childY = baseY + childRadius * sin(theta).toFloat()
        Offset(childX, childY)
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        // Draw lines to children
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMindMapNodes(
                parent = Offset(baseX + nodeSize.width / 2f, baseY + nodeSize.height / 2f),
                children = childCenters
            )
        }
        // Draw node content (apply scale only to node size, not position)
        Box(
            modifier = Modifier
                .absoluteOffset { IntOffset(baseX.toInt(), baseY.toInt()) }
                // Remove scale from graphicsLayer so node size stays constant when zooming
                .onGloballyPositioned { coordinates ->
                    nodeSize = coordinates.size
                }
        ) {
            MindMapNodeContent(node = node, scale = 1f) // Always use scale = 1f for constant node size
        }
    }
    // Draw children
    node.children.forEachIndexed { i, child ->
        val childOffset = childCenters.getOrNull(i) ?: return@forEachIndexed
        MindMapNodeAligned(
            node = child,
            x = childOffset.x,
            y = childOffset.y,
            nodeRadius = nodeRadius,
            angle = angle + angleStep * i - spread / 2 + angleStep / 2,
            spread = 120f,
            depth = depth + 1,
            textMeasurer = textMeasurer,
            scale = scale
        )
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
    // Remove zoom: scale is fixed
    val scale = 1f

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        // --- Drag to pan ---
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
                scale = scale
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