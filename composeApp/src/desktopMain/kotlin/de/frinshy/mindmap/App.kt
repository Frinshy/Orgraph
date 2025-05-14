package de.frinshy.mindmap

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.FluentTheme
import com.konyaco.fluent.background.Mica
import com.konyaco.fluent.component.*
import com.konyaco.fluent.darkColors
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.Home
import com.konyaco.fluent.icons.regular.Window
import com.konyaco.fluent.lightColors
import de.frinshy.mindmap.components.ComponentItem
import de.frinshy.mindmap.components.ComponentNavigator

@Composable
@Preview
fun App() {
    FluentTheme(
        colors = if (isSystemInDarkTheme()) darkColors() else lightColors(),
    ) {

        val components = listOf(
            ComponentItem("Home", "home", "Home", icon = Icons.Default.Home, content = { }),
            ComponentItem("Window", "window", "Window", icon = Icons.Default.Window, content = { }),
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
                    modifier = Modifier.fillMaxHeight(),
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

                Column(Modifier.padding(24.dp).fillMaxSize()) {

                    val textFieldValue = remember { mutableStateOf("Hello Fluent Design") }

                    Button(onClick = { textFieldValue.value = "Bye Fluent Design" }) {
                        Text(textFieldValue.value)
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
    val expandedItems = remember {
        mutableStateOf(false)
    }
    LaunchedEffect(selectedItem) {
        if (navItem != selectedItem) {
            val navItemAsGroup = "${navItem.group}/${navItem.name}/"
            if ((selectedItem.group + "/").startsWith(navItemAsGroup))
                expandedItems.value = true
        }
    }
    SideNavItem(
        selectedItem == navItem,
        onClick = {
            onSelectedItemChanged(navItem)
            expandedItems.value = !expandedItems.value
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