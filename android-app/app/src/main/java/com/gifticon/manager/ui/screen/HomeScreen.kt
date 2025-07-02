package com.gifticon.manager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.NavController
import com.gifticon.manager.ui.viewmodel.HomeViewModel
import com.gifticon.manager.utils.AdMobUtil
import coil.compose.AsyncImage
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBanner() {
    val context = LocalContext.current
    
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdMobUtil.getBannerAdUnitId()
                adListener = AdMobUtil.createAdListener(
                    onAdLoaded = {
                        // 광고 로드 성공 시 로그
                    },
                    onAdFailedToLoad = { loadAdError ->
                        // 광고 로드 실패 시 로그
                    }
                )
            }
        },
        update = { adView ->
            adView.loadAd(AdMobUtil.createAdRequest())
        }
    )
}

private fun isExpired(expiryDate: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        val expiry = sdf.parse(expiryDate)
        val today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        expiry?.before(today) ?: false
    } catch (e: Exception) {
        false
    }
}

private fun calculateDaysUntilExpiry(expiryDate: String): Int {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        val expiry = sdf.parse(expiryDate)
        val today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        if (expiry != null) {
            val diffInMillis = expiry.time - today.time
            val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
            diffInDays.toInt()
        } else {
            Int.MAX_VALUE
        }
    } catch (e: Exception) {
        Int.MAX_VALUE
    }
}

private fun getDdayText(expiryDate: String): String {
    val daysUntilExpiry = calculateDaysUntilExpiry(expiryDate)
    return when {
        isExpired(expiryDate) -> "만료됨"
        daysUntilExpiry == 0 -> "D-day"
        daysUntilExpiry < 0 -> "D+${-daysUntilExpiry}"
        else -> "D-${daysUntilExpiry}"
    }
}

private fun getDdayColor(expiryDate: String): Color {
    val daysUntilExpiry = calculateDaysUntilExpiry(expiryDate)
    return when {
        isExpired(expiryDate) -> Color.Red
        daysUntilExpiry <= 7 -> Color.Red
        daysUntilExpiry <= 30 -> Color(0xFFFF6B35) // 주황색
        else -> Color(0xFF2196F3) // 파란색
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val gifticons by viewModel.gifticons.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val apiStatus by viewModel.apiStatus.collectAsState()

    // 화면이 포커스될 때마다 새로고침
    LaunchedEffect(Unit) {
        viewModel.refreshGifticons()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                // 개선된 Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        // 메인 타이틀
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "기프티콘 매니저",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "나의 기프티콘을 한눈에",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // 알림 아이콘 (헤더 우측)
                            IconButton(
                                onClick = {
                                    navController.navigate("notification_settings")
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "알림 설정",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("add_gifticon")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "기프티콘 추가")
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "기프티콘을 불러오는 중...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (gifticons.isEmpty()) {
                    // 빈 상태 - 더 아름답게 디자인
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = RoundedCornerShape(60.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "아직 등록된 기프티콘이 없습니다",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "+ 버튼을 눌러 첫 번째 기프티콘을 추가해보세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                } else {
                    // 기프티콘 목록 - 더 모던한 디자인
                    val sortedGifticons = gifticons.sortedBy { gifticon ->
                        val daysUntilExpiry = calculateDaysUntilExpiry(gifticon.expiryDate)
                        // 만료된 기프티콘은 맨 뒤로, 그 외에는 남은 기간 순으로 정렬
                        if (isExpired(gifticon.expiryDate)) Int.MAX_VALUE else daysUntilExpiry
                    }
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 20.dp,
                            bottom = 100.dp // FAB와 겹치지 않도록 하단 여백 증가
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 광고 배너 (첫 번째 아이템으로 추가)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "광고",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    AdBanner()
                                }
                            }
                        }
                        
                        items(sortedGifticons) { gifticon ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    // 이미지가 있으면 표시
                                    gifticon.imagePath?.let { imagePath ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clickable {
                                                    navController.navigate("image_viewer/${gifticon.id}")
                                                },
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            AsyncImage(
                                                model = imagePath,
                                                contentDescription = "기프티콘 이미지",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = getDdayText(gifticon.expiryDate),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = getDdayColor(gifticon.expiryDate)
                                            )
                                        }
                                        
                                        // 금액 배지
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = when {
                                                isExpired(gifticon.expiryDate) -> Color.Red.copy(alpha = 0.1f)
                                                gifticon.balance > 0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                                            }
                                        ) {
                                            Text(
                                                text = when {
                                                    isExpired(gifticon.expiryDate) -> "만료됨"
                                                    gifticon.balance > 0 -> "${gifticon.balance}원"
                                                    else -> "상품권"
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    isExpired(gifticon.expiryDate) -> Color.Red
                                                    gifticon.balance > 0 -> MaterialTheme.colorScheme.primary
                                                    else -> MaterialTheme.colorScheme.secondary
                                                },
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                    
                                    // 상품명 (독립적인 한 줄)
                                    gifticon.productName?.let { productName ->
                                        if (productName.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = productName,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 2,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "만기일: ${gifticon.expiryDate}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        
                                        // 편집 버튼
                                        IconButton(
                                            onClick = {
                                                navController.navigate("edit_gifticon/${gifticon.id}")
                                            },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "편집",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 