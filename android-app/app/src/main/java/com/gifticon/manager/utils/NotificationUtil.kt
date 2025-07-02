package com.gifticon.manager.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gifticon.manager.MainActivity
import com.gifticon.manager.R

object NotificationUtil {
    
    private const val EXPIRY_CHANNEL_ID = "gifticon_expiry"
    private const val GALLERY_SCAN_CHANNEL_ID = "gallery_scan"
    private const val EXPIRY_NOTIFICATION_ID = 1001
    private const val GALLERY_SCAN_PROGRESS_ID = 2001
    private const val GALLERY_SCAN_COMPLETION_ID = 2002
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            
            // 만료 알림 채널
            val expiryChannel = NotificationChannel(
                EXPIRY_CHANNEL_ID,
                "기프티콘 만료 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "기프티콘 만료일을 미리 알려드립니다"
                enableLights(true)
                enableVibration(true)
            }
            
            // 갤러리 스캔 채널
            val galleryScanChannel = NotificationChannel(
                GALLERY_SCAN_CHANNEL_ID,
                "갤러리 스캔",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "갤러리 스캔 진행 상황을 알려드립니다"
            }
            
            notificationManager.createNotificationChannel(expiryChannel)
            notificationManager.createNotificationChannel(galleryScanChannel)
        }
    }
    
    fun showExpiryNotification(
        context: Context,
        gifticonName: String,
        daysLeft: Int,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = when {
            daysLeft == 0 -> "기프티콘이 오늘 만료됩니다!"
            daysLeft == 1 -> "기프티콘이 내일 만료됩니다!"
            else -> "기프티콘이 ${daysLeft}일 후 만료됩니다!"
        }
        
        val message = gifticonName + "을 사용해주세요."
        
        val notification = NotificationCompat.Builder(context, EXPIRY_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
    }
    
    /**
     * 갤러리 스캔 진행 상황 알림
     */
    fun showGalleryScanProgressNotification(
        context: Context,
        stage: String,
        progress: Int,
        total: Int,
        found: Int
    ) {
        val notification = NotificationCompat.Builder(context, GALLERY_SCAN_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📱 갤러리 스캔 중...")
            .setContentText("$stage ($progress/$total) - 발견: ${found}개")
            .setProgress(total, progress, false)
            .setOngoing(true)
            .setSilent(true)
            .build()
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(GALLERY_SCAN_PROGRESS_ID, notification)
    }
    
    /**
     * 갤러리 스캔 완료 알림
     */
    fun showGalleryScanCompletionNotification(
        context: Context,
        foundCount: Int,
        message: String
    ) {
        // 진행 상황 알림 제거
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(GALLERY_SCAN_PROGRESS_ID)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = if (foundCount > 0) {
            "🎉 갤러리 스캔 완료!"
        } else {
            "📱 갤러리 스캔 완료"
        }
        
        val contentText = if (foundCount > 0) {
            "${foundCount}개의 기프티콘을 발견하여 등록했습니다."
        } else {
            message
        }
        
        val notification = NotificationCompat.Builder(context, GALLERY_SCAN_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(GALLERY_SCAN_COMPLETION_ID, notification)
    }
    
    fun cancelAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()
    }
} 