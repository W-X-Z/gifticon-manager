package com.gifticon.manager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gifticon.manager.ui.screen.AddGifticonScreen
import com.gifticon.manager.ui.screen.EditGifticonScreen
import com.gifticon.manager.ui.screen.HomeScreen
import com.gifticon.manager.ui.screen.ImageViewerScreen
import com.gifticon.manager.ui.screen.NotificationSettingsScreen

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
        composable("edit_gifticon/{gifticonId}") { backStackEntry ->
            val gifticonId = backStackEntry.arguments?.getString("gifticonId")?.toLongOrNull() ?: 0L
            EditGifticonScreen(navController = navController, gifticonId = gifticonId)
        }
        composable("image_viewer/{gifticonId}") { backStackEntry ->
            val gifticonId = backStackEntry.arguments?.getString("gifticonId")?.toLongOrNull() ?: 0L
            ImageViewerScreen(navController = navController, gifticonId = gifticonId)
        }
        composable("image_viewer_direct/{imagePath}") { backStackEntry ->
            val imagePath = backStackEntry.arguments?.getString("imagePath")?.let { 
                java.net.URLDecoder.decode(it, "UTF-8") 
            } ?: ""
            ImageViewerScreen(navController = navController, imagePath = imagePath)
        }
        composable("notification_settings") {
            NotificationSettingsScreen(navController = navController)
        }
    }
} 