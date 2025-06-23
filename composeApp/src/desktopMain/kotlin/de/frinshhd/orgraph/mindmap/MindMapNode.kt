package de.frinshhd.orgraph.mindmap

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset

data class MindMapNode(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    var position: Offset = Offset.Zero,
    val children: MutableList<MindMapNode> = mutableStateListOf()
)

