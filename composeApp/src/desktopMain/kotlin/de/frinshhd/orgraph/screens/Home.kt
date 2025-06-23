package de.frinshhd.orgraph.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konyaco.fluent.component.Button
import com.konyaco.fluent.component.Text

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to MindMap",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Organize your thoughts visually",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { /* Add navigation or action here */ },
            ) {
                Text(text = "Get Started")
            }
        }
    }
}