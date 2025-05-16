package de.frinshy.mindmap

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalDrawer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.Background
import com.konyaco.fluent.FluentTheme
import com.konyaco.fluent.animation.FluentDuration
import com.konyaco.fluent.animation.FluentEasing
import com.konyaco.fluent.background.Layer
import com.konyaco.fluent.background.Mica
import com.konyaco.fluent.component.*
import com.konyaco.fluent.darkColors
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.Home
import com.konyaco.fluent.icons.regular.Info
import com.konyaco.fluent.icons.regular.Map
import com.konyaco.fluent.icons.regular.Settings
import com.konyaco.fluent.icons.regular.Window
import com.konyaco.fluent.lightColors
import com.konyaco.fluent.surface.Card
import de.frinshy.mindmap.components.ComponentItem
import de.frinshy.mindmap.components.ComponentNavigator
import de.frinshy.mindmap.mindmap.MindMapNode
import de.frinshy.mindmap.mindmap.MindMapUI
import de.frinshy.mindmap.screens.AboutScreen
import de.frinshy.mindmap.screens.HomeScreen
import de.frinshy.mindmap.screens.SettingsScreen
import org.jetbrains.skiko.currentSystemTheme
import java.awt.Component

@Composable
@Preview
fun App() {
    FluentTheme(
        colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
    ) {
        val components = listOf(
            ComponentItem("Home", "home", "Home", icon = Icons.Default.Home) { HomeScreen() },
            ComponentItem("MindMap", "mindmap", "MindMap", icon = Icons.Default.Map) { MindMapUI() },
            ComponentItem("About", "about", "About", icon = Icons.Default.Info) { AboutScreen() },
        )

        Mica(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxSize()) {
                var expanded by remember { mutableStateOf(true) }

                val (selectedItem, setSelectedItem) = remember {
                    mutableStateOf(components.first())
                }

                var selectedItemWithContent by remember {
                    mutableStateOf(selectedItem)
                }
                LaunchedEffect(selectedItem) {
                    if (selectedItem.content != null) {
                        selectedItemWithContent = selectedItem
                    }
                }

                val navigator = remember(setSelectedItem) {
                    ComponentNavigator(setSelectedItem)
                }

                var textFieldValue by remember {
                    mutableStateOf(TextFieldValue())
                }

                val filteredComponents = remember(textFieldValue.text) {
                    components.filter { it.name.contains(textFieldValue.text, ignoreCase = true) }
                }

                SideNav(
                    modifier = Modifier.fillMaxHeight().background(FluentTheme.colors.background.mica.base),
                    expanded = expanded,
                    onExpandStateChange = { expanded = it },
                    title = { Text("Controls") },
                    autoSuggestionBox = {
                        TextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            placeholder = { Text("Search") },
                            modifier = Modifier.fillMaxWidth().focusHandle(),
                            singleLine = true,
                        )
                    },
                    footer = {
                        NavigationItem(selectedItem, setSelectedItem, settingsNavItem)
                    }
                ) {
                    if (filteredComponents.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No results found", style = FluentTheme.typography.body)
                        }
                    } else {
                        filteredComponents.forEach { navItem ->
                            NavigationItem(selectedItem, setSelectedItem, navItem)
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) {
                    AnimatedContent(selectedItemWithContent, Modifier.fillMaxSize(), transitionSpec = {
                        (fadeIn(
                            tween (
                                FluentDuration.ShortDuration,
                                easing = FluentEasing.FadeInFadeOutEasing,
                                delayMillis = FluentDuration.QuickDuration
                            )
                        ) + slideInVertically (
                            tween(
                                FluentDuration.MediumDuration,
                                easing = FluentEasing.FastInvokeEasing,
                                delayMillis = FluentDuration.QuickDuration
                            )
                        ) { it / 5 }) togetherWith fadeOut(
                            tween(
                                FluentDuration.QuickDuration,
                                easing = FluentEasing.FadeInFadeOutEasing,
                                delayMillis = FluentDuration.QuickDuration
                            )
                        )
                    }) {
                        Box(
                            modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 12.dp).fillMaxSize(),
                        ) {
                            it.content?.invoke(it, navigator)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationItem(
    selectedItem: ComponentItem,
    onSelectedItemChanged: (ComponentItem) -> Unit,
    navItem: ComponentItem
) {
    val expandedItems = remember { mutableStateOf(false) }

    LaunchedEffect(selectedItem) {
        if (navItem != selectedItem) {
            val navItemAsGroup = "${navItem.group}/${navItem.name}/"
            if ((selectedItem.group + "/").startsWith(navItemAsGroup)) {
                expandedItems.value = true
            }
        }
    }

    SideNavItem(
        selected = selectedItem == navItem,
        onClick = {
            if (selectedItem != navItem) {
                onSelectedItemChanged(navItem) // Update the selected item
            }
            if (navItem.items?.isNotEmpty() == true) {
                expandedItems.value = !expandedItems.value // Toggle expansion for items
            }
        },
        icon = navItem.icon?.let { { Icon(it, navItem.name) } },
        content = { Text(navItem.name) },
        expandItems = expandedItems.value,
        items = navItem.items?.let {
            if (it.isNotEmpty()) {
                {
                    it.forEach { nestedItem ->
                        NavigationItem(
                            selectedItem = selectedItem,
                            onSelectedItemChanged = onSelectedItemChanged,
                            navItem = nestedItem
                        )
                    }
                }
            } else {
                null
            }
        }
    )
}

private val settingsNavItem = ComponentItem(
    name = "Settings",
    group = "settings",
    description = "Settings",
    icon = Icons.Default.Settings,
    content = { SettingsScreen() }
)