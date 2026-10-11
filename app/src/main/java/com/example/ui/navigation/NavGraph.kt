package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.PreferencesManager
import com.example.data.repository.DocumentRepository
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.editor.EditorViewModelFactory
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.home.HomeViewModelFactory

@Composable
fun WordNavGraph(
    repository: DocumentRepository,
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier,
    initialDocumentId: String? = null,
    navController: NavHostController = rememberNavController()
) {
    androidx.compose.runtime.LaunchedEffect(initialDocumentId) {
        if (!initialDocumentId.isNullOrBlank()) {
            navController.navigate(Screen.Editor.createRoute(initialDocumentId))
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(repository, preferencesManager)
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToEditor = { documentId ->
                    navController.navigate(Screen.Editor.createRoute(documentId))
                }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument("documentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId") ?: ""
            val editorViewModel: EditorViewModel = viewModel(
                key = "editor_$documentId",
                factory = EditorViewModelFactory(documentId, repository, preferencesManager)
            )
            EditorScreen(
                viewModel = editorViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
