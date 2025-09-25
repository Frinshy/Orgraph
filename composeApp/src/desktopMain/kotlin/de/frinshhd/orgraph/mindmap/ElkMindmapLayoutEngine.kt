package de.frinshhd.orgraph.mindmap

import org.eclipse.elk.core.RecursiveGraphLayoutEngine
import org.eclipse.elk.core.util.BasicProgressMonitor
import org.eclipse.elk.graph.ElkGraphFactory
import org.eclipse.elk.graph.ElkNode
import org.eclipse.elk.graph.ElkEdge
import org.eclipse.elk.core.options.CoreOptions
import org.eclipse.elk.core.options.Direction
import org.eclipse.elk.alg.layered.options.LayeredOptions
import org.eclipse.elk.alg.force.options.ForceOptions
import org.eclipse.elk.alg.radial.options.RadialOptions

/**
 * Layout engine that uses ELK (Eclipse Layout Kernel) to automatically position mindmap nodes
 */
class ElkMindMapLayoutEngine {

    private val layoutEngine = RecursiveGraphLayoutEngine()

    /**
     * Converts a MindMapNode tree to ELK layout and returns positioned nodes and edges
     */
    fun layout(
        root: MindMapNode,
        algorithm: LayoutAlgorithm = LayoutAlgorithm.RADIAL,
        canvasWidth: Float = 1200f,
        canvasHeight: Float = 800f
    ): MindMapLayout {
        // Create ELK graph
        val elkGraph = createElkGraph(root, algorithm)

        // Run layout algorithm
        layoutEngine.layout(elkGraph, BasicProgressMonitor())

        // Extract results
        return extractLayout(elkGraph)
    }

    private fun createElkGraph(root: MindMapNode, algorithm: LayoutAlgorithm): ElkNode {
        val factory = ElkGraphFactory.eINSTANCE
        val graph = factory.createElkNode()

        // Configure layout algorithm
        when (algorithm) {
            LayoutAlgorithm.RADIAL -> {
                graph.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.radial")
                graph.setProperty(RadialOptions.RADIUS, 150.0)
            }
            LayoutAlgorithm.LAYERED -> {
                graph.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.layered")
                graph.setProperty(CoreOptions.DIRECTION, Direction.RIGHT)
                graph.setProperty(LayeredOptions.SPACING_NODE_NODE, 80.0)
                graph.setProperty(LayeredOptions.SPACING_NODE_NODE_BETWEEN_LAYERS, 120.0)
            }
            LayoutAlgorithm.FORCE -> {
                graph.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.force")
                graph.setProperty(ForceOptions.ITERATIONS, 300)
                graph.setProperty(ForceOptions.REPULSIVE_POWER, 2)
            }
        }

        // Set basic graph properties
        graph.setProperty(CoreOptions.SPACING_NODE_NODE, 50.0)
        graph.setProperty(CoreOptions.SPACING_EDGE_NODE, 20.0)

        // Create nodes and edges recursively
        val nodeMap = mutableMapOf<String, ElkNode>()
        createNodesRecursively(root, graph, nodeMap, factory)
        createEdgesRecursively(root, graph, nodeMap, factory)

        return graph
    }

    private fun createNodesRecursively(
        node: MindMapNode,
        parent: ElkNode,
        nodeMap: MutableMap<String, ElkNode>,
        factory: ElkGraphFactory
    ) {
        val elkNode = factory.createElkNode()
        elkNode.identifier = node.id

        // Set node size based on text length (rough estimation)
        val textLength = node.text.length
        val width = (textLength * 8 + 60).coerceAtLeast(100).coerceAtMost(200)
        val height = 50 // Fixed height since we don't have image property

        elkNode.width = width.toDouble()
        elkNode.height = height.toDouble()

        parent.children.add(elkNode)
        nodeMap[node.id] = elkNode

        // Recursively create child nodes
        node.children.forEach { child ->
            createNodesRecursively(child, parent, nodeMap, factory)
        }
    }

    private fun createEdgesRecursively(
        node: MindMapNode,
        graph: ElkNode,
        nodeMap: Map<String, ElkNode>,
        factory: ElkGraphFactory
    ) {
        node.children.forEach { child ->
            val edge = factory.createElkEdge()
            edge.sources.add(nodeMap[node.id])
            edge.targets.add(nodeMap[child.id])
            graph.containedEdges.add(edge)

            // Recursively create edges for children
            createEdgesRecursively(child, graph, nodeMap, factory)
        }
    }

    private fun extractLayout(elkGraph: ElkNode): MindMapLayout {
        val nodes = mutableListOf<LayoutNode>()
        val edges = mutableListOf<LayoutEdge>()

        // Extract nodes
        extractNodesRecursively(elkGraph, nodes)

        // Extract edges
        elkGraph.containedEdges.forEach { elkEdge ->
            val sourceId = elkEdge.sources.firstOrNull()?.identifier ?: ""
            val targetId = elkEdge.targets.firstOrNull()?.identifier ?: ""

            val points = if (elkEdge.sections.isNotEmpty()) {
                val section = elkEdge.sections.first()
                val points = mutableListOf<Pair<Float, Float>>()
                points.add(Pair(section.startX.toFloat(), section.startY.toFloat()))
                section.bendPoints.forEach { point ->
                    points.add(Pair(point.x.toFloat(), point.y.toFloat()))
                }
                points.add(Pair(section.endX.toFloat(), section.endY.toFloat()))
                points
            } else {
                emptyList()
            }

            edges.add(LayoutEdge(sourceId, targetId, points))
        }

        // Calculate bounds
        val bounds = if (nodes.isNotEmpty()) {
            val minX = nodes.minOf { it.x }
            val minY = nodes.minOf { it.y }
            val maxX = nodes.maxOf { it.x + it.width }
            val maxY = nodes.maxOf { it.y + it.height }
            LayoutBounds(minX, minY, maxX - minX, maxY - minY)
        } else {
            LayoutBounds(0f, 0f, 0f, 0f)
        }

        return MindMapLayout(nodes, edges, bounds)
    }

    private fun extractNodesRecursively(elkNode: ElkNode, nodes: MutableList<LayoutNode>) {
        if (elkNode.identifier != null) {
            nodes.add(
                LayoutNode(
                    id = elkNode.identifier,
                    label = elkNode.identifier, // Will be replaced with actual text later
                    x = elkNode.x.toFloat(),
                    y = elkNode.y.toFloat(),
                    width = elkNode.width.toFloat(),
                    height = elkNode.height.toFloat()
                )
            )
        }

        elkNode.children.forEach { child ->
            extractNodesRecursively(child, nodes)
        }
    }
}
