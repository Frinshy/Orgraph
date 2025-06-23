package de.frinshhd.orgraph.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.konyaco.fluent.FluentTheme
import com.konyaco.fluent.component.ComboBox
import com.konyaco.fluent.component.Icon
import com.konyaco.fluent.component.Switcher
import com.konyaco.fluent.component.Text
import com.konyaco.fluent.icons.Icons
import com.konyaco.fluent.icons.regular.DarkTheme
import com.konyaco.fluent.icons.regular.LocalLanguage
import com.konyaco.fluent.icons.regular.Window
import com.konyaco.fluent.surface.Card

@Composable
fun SettingsScreen() {
    var selectedLanguage by remember { mutableStateOf("English") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // General Section
        NavCategory(
            "General", listOf(
            NavItem("Language", Icons.Default.LocalLanguage) {
                ComboBox(
                    items = listOf("English", "German"),
                    selected = if (selectedLanguage == "English") 0 else 1,
                    onSelectionChange = { _, item -> selectedLanguage = item }
                )
            },
            NavItem("Theme", Icons.Default.DarkTheme) {
                var selectedTheme by remember { mutableStateOf("Default") }
                    ComboBox(
                        items = listOf("Default", "Dark", "Light"),
                        selected = when (selectedTheme) {
                            "Light" -> 2
                            "Dark" -> 1
                            else -> 0
                        },
                        onSelectionChange = { _, item ->
                            selectedTheme = item
                        },
                    )

            }
        ))


        // Appearance Section
        NavCategory("Appearance", listOf(
            NavItem("Theme", Icons.Default.Window) {
                Switcher(
                    checked = true,
                    onCheckStateChange = { },
                    text = "Dark Theme",
                    textBefore = true
                )
            }
        ))
    }

}

@Composable
fun NavCategory(title: String, items: List<NavItem>) {
    val sectionTitleStyle = TextStyle(fontSize = FluentTheme.typography.subtitle.fontSize, fontWeight = FluentTheme.typography.subtitle.fontWeight)

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(text = title, style = sectionTitleStyle, modifier = Modifier.padding(bottom = 10.dp))
        items.forEach { item ->
            NavItemCard(
                item
            )
        }
    }
}

data class NavItem(
    val text: String,
    val icon: ImageVector,
    val content: @Composable (() -> Unit) = {}
)

@Composable
fun NavItemCard(navItem: NavItem) {
    val cardTitleStyle = TextStyle(fontSize = FluentTheme.typography.body.fontSize, fontWeight = FluentTheme.typography.body.fontWeight)

    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 4.dp),
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(30.dp).padding(end = 10.dp),
                imageVector = navItem.icon,
                contentDescription = "${navItem.text} Icon",
            )
            Text(text = navItem.text, style = cardTitleStyle)
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.padding(end = 8.dp)) {
                navItem.content()
            }
        }
    }

}