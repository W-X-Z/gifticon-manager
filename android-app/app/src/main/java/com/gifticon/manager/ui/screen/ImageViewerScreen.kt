package com.gifticon.manager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import com.gifticon.manager.ui.viewmodel.ImageViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerScreen(
    navController: NavController,
    gifticonId: Long = 0L,
    imagePath: String = "",
    viewModel: ImageViewerViewModel = hiltViewModel()
) {
    val gifticon by viewModel.gifticon.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    LaunchedEffect(gifticonId) {
        if (gifticonId > 0) {
            viewModel.loadGifticon(gifticonId)
        }
    }
    
    Log.d("ImageViewerScreen", "기프티콘 ID: $gifticonId, 이미지 경로: $imagePath")
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "기프티콘 이미지",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack, 
                                contentDescription = "뒤로가기",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { 
                                scale = if (scale > 1f) 1f else 2f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        ) {
                            Icon(
                                if (scale > 1f) Icons.Default.ZoomOut else Icons.Default.ZoomIn,
                                contentDescription = if (scale > 1f) "축소" else "확대",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f..3f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    val imageToShow = if (gifticonId > 0) {
                        gifticon?.imagePath
                    } else {
                        imagePath
                    }
                    
                    if (imageToShow?.isNotEmpty() == true) {
                        AsyncImage(
                            model = imageToShow,
                            contentDescription = "기프티콘 이미지",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                ),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "이미지를 불러올 수 없습니다",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
} 