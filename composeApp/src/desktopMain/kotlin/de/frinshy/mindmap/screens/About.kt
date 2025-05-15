package de.frinshy.mindmap.screens

            import androidx.compose.foundation.layout.*
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.Alignment
            import androidx.compose.ui.Modifier
            import androidx.compose.ui.unit.dp
            import androidx.compose.ui.unit.sp
            import com.konyaco.fluent.component.Icon
            import com.konyaco.fluent.component.Text
            import com.konyaco.fluent.icons.Icons
            import com.konyaco.fluent.icons.regular.Info

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
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Icon",
                            modifier = Modifier
                                .size(48.dp)
                                .padding(end = 16.dp)
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
                                text = "Developed by FrinshHD",
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }