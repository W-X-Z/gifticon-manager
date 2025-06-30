package com.gifticon.manager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gifticon.manager.ui.screen.AddGifticonScreen
import com.gifticon.manager.ui.screen.EditGifticonScreen
import com.gifticon.manager.ui.screen.HomeScreen

@Composable
fun GifticonNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("add_gifticon") {
            AddGifticonScreen(navController = navController)
        }
        composable("edit_gifticon") {
            EditGifticonScreen(navController = navController)
        }
    }
} 