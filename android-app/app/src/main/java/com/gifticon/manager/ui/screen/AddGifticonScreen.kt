package com.gifticon.manager.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gifticon.manager.ui.viewmodel.AddGifticonViewModel

@Composable
fun AddGifticonScreen(
    navController: NavController,
    viewModel: AddGifticonViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.processSelectedImage(context, uri) { _ ->
                navController.navigate("edit_gifticon") {
                    popUpTo("add_gifticon") { inclusive = true }
                }
            }
        } else {
            // 사용자가 이미지를 선택하지 않고 뒤로 갔을 때 홈으로 이동
            navController.popBackStack("home", inclusive = false)
        }
    }
    // 진입 즉시 갤러리 실행
    LaunchedEffect(Unit) {
        galleryLauncher.launch("image/*")
    }
    // UI 없음 (빈 컴포저블)
} 