package de.frinshhd.orgraph.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konyaco.fluent.component.Text

@Composable
fun AboutScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource("images/icon.svg"),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(90.dp)
                    .padding(end = 25.dp)
            )
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "MindMap Application",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version: 1.0.0",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Developed by Frinshy",
                    fontSize = 16.sp
                )
            }
        }
    }
}