package de.frinshy.orgraph

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import de.frinshy.orgraph.presentation.viewmodel.OrgraphViewModel
import de.frinshy.orgraph.ui.screens.MindMapScreen
import de.frinshy.orgraph.ui.screens.TeacherListScreen
import de.frinshy.orgraph.ui.theme.OrgraphTheme

@Composable
@Preview
fun App() {
    val viewModel: OrgraphViewModel = viewModel()
    val school by viewModel.school.collectAsState()
    val selectedView by viewModel.selectedView.collectAsState()

    OrgraphTheme {
        when (selectedView) {
            OrgraphViewModel.ViewMode.LIST -> {
                TeacherListScreen(
                    school = school,
                    viewModel = viewModel
                )
            }
            OrgraphViewModel.ViewMode.MINDMAP -> {
                MindMapScreen(
                    school = school,
                    viewModel = viewModel
                )
            }
        }
    }
}