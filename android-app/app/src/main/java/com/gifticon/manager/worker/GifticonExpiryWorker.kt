package com.gifticon.manager.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gifticon.manager.data.repository.GifticonRepository
import com.gifticon.manager.utils.NotificationUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class GifticonExpiryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: GifticonRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 알림 채널 생성
            NotificationUtil.createNotificationChannel(applicationContext)
            
            // 알림 설정 로드
            val sharedPrefs = applicationContext.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
            val isEnabled = sharedPrefs.getBoolean("is_enabled", false)
            
            if (!isEnabled) {
                return Result.success()
            }
            
            val notificationDaysString = sharedPrefs.getString("notification_days", "")
            if (notificationDaysString.isNullOrEmpty()) {
                return Result.success()
            }
            
            val notificationDays = notificationDaysString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            
            // 모든 기프티콘 가져오기
            val gifticons = repository.getAllGifticons().first()
            
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("Asia/Seoul")
            }
            
            val today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            
            var notificationCount = 0
            
            // 각 기프티콘에 대해 만료일 체크
            for (gifticon in gifticons) {
                try {
                    val expiryDate = sdf.parse(gifticon.expiryDate)
                    if (expiryDate != null) {
                        val diffInMillis = expiryDate.time - today.time
                        val daysUntilExpiry = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
                        
                        // 설정된 알림 일수와 일치하는지 확인
                        if (notificationDays.contains(daysUntilExpiry) && daysUntilExpiry >= 0) {
                            val gifticonName = gifticon.productName ?: gifticon.brandName
                            val notificationId = (gifticon.id + daysUntilExpiry * 1000).toInt()
                            
                            NotificationUtil.showExpiryNotification(
                                applicationContext,
                                gifticonName,
                                daysUntilExpiry,
                                notificationId
                            )
                            
                            notificationCount++
                        }
                    }
                } catch (e: Exception) {
                    // 날짜 파싱 실패 시 무시
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
} 