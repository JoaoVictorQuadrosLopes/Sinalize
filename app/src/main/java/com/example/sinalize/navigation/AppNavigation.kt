package com.example.sinalize.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sinalize.ui.screens.CategoryScreen
import com.example.sinalize.ui.screens.HomeScreen
import com.example.sinalize.ui.screens.LessonScreen
import com.example.sinalize.ui.screens.PracticeCameraScreen
import com.example.sinalize.ui.screens.ProgressScreen
import com.example.sinalize.ui.screens.QuizScreen
import com.example.sinalize.viewmodel.LibrasViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: LibrasViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onCategoryClick = { navController.navigate("categories") },
                onProgressClick = { navController.navigate("progress") }
            )
        }

        composable("categories") {
            CategoryScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onCategoryClick = { categoryId ->
                    viewModel.loadLessonsByCategory(categoryId)
                    navController.navigate("lessons/$categoryId")
                }
            )
        }

        composable(
            route = "lessons/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.IntType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
            viewModel.loadLessonsByCategory(categoryId)

            LessonScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onLessonClick = { lessonId ->
                    viewModel.loadLesson(lessonId)
                    navController.navigate("practice/$lessonId")
                }
            )
        }

        composable(
            route = "practice/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.IntType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: 0
            viewModel.loadLesson(lessonId)

            PracticeCameraScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onFinishClick = { navController.navigate("quiz/$lessonId") }
            )
        }

        composable(
            route = "quiz/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.IntType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: 0
            viewModel.loadLesson(lessonId)

            QuizScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onFinishClick = {
                    viewModel.completeLesson()
                    navController.navigate("progress")
                }
            )
        }

        composable("progress") {
            ProgressScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onHomeClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
