package com.gifticon.manager.utils

import android.content.Context
import androidx.work.*
import com.gifticon.manager.worker.GalleryScanWorker
import java.util.concurrent.TimeUnit

object GalleryScanManager {
    
    private const val GALLERY_SCAN_WORK_NAME = "gallery_scan_work"
    
    /**
     * 갤러리 스캔 작업을 시작합니다.
     */
    fun startGalleryScan(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val galleryScanRequest = OneTimeWorkRequestBuilder<GalleryScanWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10000L,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                GALLERY_SCAN_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                galleryScanRequest
            )
    }
    
    /**
     * 갤러리 스캔 작업을 취소합니다.
     */
    fun cancelGalleryScan(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(GALLERY_SCAN_WORK_NAME)
    }
    
    /**
     * 갤러리 스캔 작업 상태를 가져옵니다.
     */
    fun getGalleryScanStatus(context: Context) = 
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(GALLERY_SCAN_WORK_NAME)
} 