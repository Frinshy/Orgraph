package de.frinshhd.orgraph.mindmap

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Layout result after ELK processing
 */
data class LayoutNode(
    val id: String,
    val label: String,
    val x: Float,
    val y: Float,
    val width: Float = 100f,
    val height: Float = 60f,
    val image: ImageBitmap? = null
)

/**
 * Edge between two nodes
 */
data class LayoutEdge(
    val sourceId: String,
    val targetId: String,
    val points: List<Pair<Float, Float>> = emptyList()
)

/**
 * Complete layout result
 */
data class MindMapLayout(
    val nodes: List<LayoutNode>,
    val edges: List<LayoutEdge>,
    val bounds: LayoutBounds
)

/**
 * Bounding box for the entire layout
 */
data class LayoutBounds(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

/**
 * Layout algorithm options
 */
enum class LayoutAlgorithm {
    RADIAL,
    LAYERED,
    FORCE
}
