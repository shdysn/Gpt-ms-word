package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Editor : Screen("editor/{documentId}") {
        fun createRoute(documentId: String): String = "editor/$documentId"
    }
}
