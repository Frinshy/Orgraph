package de.frinshy.orgraph.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun OrgraphCard(
    modifier: Modifier = Modifier,
    elevation: CardElevation = CardDefaults.cardElevation(
        defaultElevation = 3.dp, // More subtle elevation
        pressedElevation = 6.dp,
        focusedElevation = 6.dp,
        hoveredElevation = 4.dp
    ),
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else modifier

    Card(
        modifier = cardModifier,
        elevation = elevation,
        colors = colors,
        border = border,
        shape = MaterialTheme.shapes.large, // Use expressive shape
        content = content
    )
}

@Composable
fun OrgraphButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 24.dp, // More generous padding
        vertical = 12.dp
    ),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        shape = MaterialTheme.shapes.large, // Use expressive shape
        content = content
    )
}

@Composable
fun OrgraphFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary, // More vibrant primary
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    icon: ImageVector = Icons.Default.Add
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f, // More pronounced press animation
        animationSpec = tween(150) // Slightly longer animation
    )

    FloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
            isPressed = false
        },
        modifier = modifier.scale(scale),
        containerColor = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large // Use expressive shape
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp) // Slightly larger icon
        )
    }
}

@Composable
fun OrgraphChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    colors: ChipColors = if (selected) {
        SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    } else {
        SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            labelColor = MaterialTheme.colorScheme.onSurface
        )
    }
) {
    SuggestionChip(
        onClick = { onClick?.invoke() },
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        },
        modifier = modifier,
        enabled = onClick != null,
        colors = colors,
        shape = MaterialTheme.shapes.large // More expressive corners
    )
}

@Composable
fun SubjectIndicator(
    color: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 14.dp // Slightly larger for better visibility
) {
    val animatedSize by animateDpAsState(
        targetValue = size,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Box(
        modifier = modifier
            .size(animatedSize)
            .shadow(1.dp, CircleShape) // Subtle elevation before clipping
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.9f),
                        color.copy(alpha = 0.7f)
                    )
                )
            )
    )
}

@Composable
fun OrgraphIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.primary
    )
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .scale(animatedScale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        enabled = enabled,
        colors = colors
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp) // Slightly larger icons
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrgraphTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    showAppIcon: Boolean = true,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer, // More expressive container
        titleContentColor = MaterialTheme.colorScheme.primary // Vibrant title color
    )
) {
    val iconRotation by animateFloatAsState(
        targetValue = if (showAppIcon) 0f else 360f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                if (showAppIcon) {
                    Icon(
                        painter = painterResource("images/icon.png"),
                        contentDescription = "App Icon",
                        modifier = Modifier
                            .size(28.dp) // Slightly larger for more presence
                            .padding(end = 12.dp) // More spacing
                            .graphicsLayer { rotationZ = iconRotation },
                        tint = MaterialTheme.colorScheme.primary // Color the icon
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold // More expressive weight
                )
            }
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = colors
    )
}

@Composable
fun OrgraphDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.large
            ),
        content = content
    )
}

@Composable
fun OrgraphDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled
    )
}