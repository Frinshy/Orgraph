package de.frinshhd.orgraph.mindmap

import de.frinshhd.orgraph.testing.TopicNode

/**
 * Utility functions to convert between different node formats
 */
object MindMapConverter {

    /**
     * Converts TopicNode (existing format) to MindMapNode (new ELK format)
     */
    fun topicNodeToMindMapNode(topicNode: TopicNode): MindMapNode {
        return MindMapNode(
            id = generateId(topicNode.name),
            text = topicNode.name,
            image = topicNode.image,
            children = topicNode.children.map { topicNodeToMindMapNode(it) }
        )
    }

    /**
     * Converts MindMapNode back to TopicNode if needed
     */
    fun mindMapNodeToTopicNode(mindMapNode: MindMapNode): TopicNode {
        val topicNode = TopicNode(
            name = mindMapNode.text,
            image = mindMapNode.image
        )
        topicNode.children.addAll(mindMapNode.children.map { mindMapNodeToTopicNode(it) })
        return topicNode
    }

    /**
     * Generates a unique ID for a node based on its text and a hash
     */
    private fun generateId(text: String): String {
        return "node_${text.hashCode().toString().removePrefix("-")}"
    }

    /**
     * Creates a sample mindmap for demonstration
     */
    fun createSampleMindMap(): MindMapNode {
        return MindMapNode(
            id = "root",
            text = "Project Planning",
            children = listOf(
                MindMapNode(
                    id = "research",
                    text = "Research",
                    children = listOf(
                        MindMapNode("market", "Market Analysis"),
                        MindMapNode("competitors", "Competitor Study"),
                        MindMapNode("users", "User Interviews")
                    )
                ),
                MindMapNode(
                    id = "design",
                    text = "Design",
                    children = listOf(
                        MindMapNode("wireframes", "Wireframes"),
                        MindMapNode("mockups", "Mockups"),
                        MindMapNode("prototype", "Prototype")
                    )
                ),
                MindMapNode(
                    id = "development",
                    text = "Development",
                    children = listOf(
                        MindMapNode("frontend", "Frontend"),
                        MindMapNode("backend", "Backend"),
                        MindMapNode("testing", "Testing")
                    )
                ),
                MindMapNode(
                    id = "launch",
                    text = "Launch",
                    children = listOf(
                        MindMapNode("marketing", "Marketing"),
                        MindMapNode("deployment", "Deployment"),
                        MindMapNode("monitoring", "Monitoring")
                    )
                )
            )
        )
    }
}
