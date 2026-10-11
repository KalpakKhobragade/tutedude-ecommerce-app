package com.example.ecommerceapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ecommerceapp.ui.auth.LoginScreen
import com.example.ecommerceapp.ui.auth.RegisterScreen
import com.example.ecommerceapp.ui.details.DetailsScreen
import com.example.ecommerceapp.ui.home.HomeScreen
import com.example.ecommerceapp.ui.upload.UploadScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val UPLOAD = "upload"
    const val DETAILS = "details/{productId}"

    fun details(productId: String): String = "details/$productId"
}

@Composable
fun AppNavGraph(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onProductClick = { productId -> navController.navigate(Routes.details(productId)) },
                onUploadClick = { navController.navigate(Routes.UPLOAD) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.DETAILS,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) {
            DetailsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.UPLOAD) {
            UploadScreen(onBack = { navController.popBackStack() })
        }
    }
}
