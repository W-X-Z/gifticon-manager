package com.gifticon.manager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import java.text.SimpleDateFormat
import java.util.*
import java.util.TimeZone
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gifticon.manager.ui.viewmodel.EditGifticonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGifticonScreen(
    navController: NavController,
    gifticonId: Long = 0,
    viewModel: EditGifticonViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val shouldNavigateBack by viewModel.shouldNavigateBack.collectAsState()
    val expiryDate by viewModel.expiryDate.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val productName by viewModel.productName.collectAsState()
    val imageUri by viewModel.imageUri.collectAsState()
    val gifticon by viewModel.gifticon.collectAsState()
    
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { 
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
    }

    // 기프티콘 ID가 있으면 해당 기프티콘 로드
    LaunchedEffect(gifticonId) {
        if (gifticonId > 0) {
            viewModel.loadGifticon(gifticonId)
        }
    }
    
    // 저장 성공 시 홈으로 바로 이동
    LaunchedEffect(shouldNavigateBack) {
        if (shouldNavigateBack) {
            viewModel.resetNavigation()
            navController.popBackStack("home", inclusive = false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "기프티콘 편집",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                        }
                    },
                    actions = {
                        // 삭제 버튼 (기존 기프티콘 편집 시에만 활성화)
                        IconButton(
                            onClick = { viewModel.deleteGifticon() },
                            enabled = !isLoading && gifticonId > 0
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제")
                        }
                        IconButton(
                            onClick = { viewModel.saveGifticon() },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "저장")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 기프티콘 이미지 표시
                val imageToShow = if (gifticonId > 0) {
                    gifticon?.imagePath
                } else {
                    imageUri?.toString()
                }
                
                imageToShow?.let { imagePath ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable {
                                // 이미지 뷰어로 이동 (새 기프티콘의 경우 임시 ID 사용)
                                if (gifticonId > 0) {
                                    navController.navigate("image_viewer/${gifticonId}")
                                } else {
                                    // 새 기프티콘의 경우 이미지 경로를 직접 전달하는 방식 사용
                                    val encodedPath = java.net.URLEncoder.encode(imagePath, "UTF-8")
                                    navController.navigate("image_viewer_direct/${encodedPath}")
                                }
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AsyncImage(
                            model = imagePath,
                            contentDescription = "기프티콘 이미지 (클릭하여 확대보기)",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                // 만료일 입력
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { },
                    label = { Text("만료일") },
                    placeholder = { Text("날짜를 선택하세요") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "날짜 선택")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                // 금액 입력
                OutlinedTextField(
                    value = amount,
                    onValueChange = { viewModel.updateAmount(it) },
                    label = { Text("금액") },
                    placeholder = { Text("예: 5000 (상품권인 경우 비워두세요)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                // 상품명 입력
                OutlinedTextField(
                    value = productName,
                    onValueChange = { viewModel.updateProductName(it) },
                    label = { Text("상품명") },
                    placeholder = { Text("예: 아메리카노, 영화관람권, 치킨세트 등") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                // 저장 버튼
                Button(
                    onClick = { viewModel.saveGifticon() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "저장하기",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                // 에러 메시지
                error?.let { errorMessage ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
        
        // DatePicker 다이얼로그
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = try {
                    if (expiryDate.isNotEmpty()) {
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
                        val parsedDate = dateFormatter.parse(expiryDate)
                        if (parsedDate != null) {
                            calendar.time = parsedDate
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            calendar.timeInMillis
                        } else null
                    } else null
                } catch (e: Exception) {
                    null
                } ?: System.currentTimeMillis()
            )
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // 시간대 문제 해결을 위해 Calendar 사용
                            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
                            calendar.timeInMillis = millis
                            calendar.set(Calendar.HOUR_OF_DAY, 0)
                            calendar.set(Calendar.MINUTE, 0)
                            calendar.set(Calendar.SECOND, 0)
                            calendar.set(Calendar.MILLISECOND, 0)
                            
                            val selectedDate = dateFormatter.format(calendar.time)
                            viewModel.updateExpiryDate(selectedDate)
                        }
                        showDatePicker = false
                    }) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("취소")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )
            }
        }
    }
} 