package com.gifticon.manager.worker

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.gifticon.manager.data.model.Gifticon
import com.gifticon.manager.data.model.GifticonCategory
import com.gifticon.manager.data.repository.GifticonRepository
import com.gifticon.manager.utils.BarcodeDetector
import com.gifticon.manager.utils.GifticonOcrExtractor
import com.gifticon.manager.utils.ImageStorageUtil
import com.gifticon.manager.utils.NotificationUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.util.*

@HiltWorker
class GalleryScanWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: GifticonRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val PROGRESS_KEY = "PROGRESS"
        const val TOTAL_KEY = "TOTAL"
        const val FOUND_KEY = "FOUND"
        const val STAGE_KEY = "STAGE"
        
        // 작업 단계
        const val STAGE_LOADING = "갤러리 이미지 로딩 중..."
        const val STAGE_BARCODE_SCAN = "바코드 검사 중..."
        const val STAGE_OCR_PROCESSING = "기프티콘 분석 중..."
        const val STAGE_SAVING = "기프티콘 저장 중..."
        const val STAGE_COMPLETED = "완료"
    }

    private val barcodeDetector = BarcodeDetector()
    private val ocrExtractor = GifticonOcrExtractor()

    override suspend fun doWork(): Result {
        return try {
            // 알림 채널 생성
            NotificationUtil.createNotificationChannel(applicationContext)
            
            // 진행 상황 알림 표시
            showProgressNotification(STAGE_LOADING, 0, 1000, 0)
            
            // 1. 갤러리에서 최근 1000장 이미지 수집
            val recentImages = getRecentImages(1000)
            
            if (recentImages.isEmpty()) {
                showCompletionNotification(0, "갤러리에서 이미지를 찾을 수 없습니다.")
                return Result.success()
            }
            
            // 2. 바코드 검사 (병렬 처리)
            showProgressNotification(STAGE_BARCODE_SCAN, 0, recentImages.size, 0)
            val barcodeImages = scanBarcodes(recentImages)
            
            if (barcodeImages.isEmpty()) {
                showCompletionNotification(0, "바코드가 있는 이미지를 찾을 수 없습니다.")
                return Result.success()
            }
            
            // 3. OCR 처리 및 기프티콘 판별 (병렬 처리)
            showProgressNotification(STAGE_OCR_PROCESSING, 0, barcodeImages.size, 0)
            val gifticonCandidates = processGifticons(barcodeImages)
            
            // 4. 기프티콘 저장
            showProgressNotification(STAGE_SAVING, 0, gifticonCandidates.size, 0)
            val savedCount = saveGifticons(gifticonCandidates)
            
            // 5. 완료 알림
            showCompletionNotification(savedCount, "갤러리 스캔이 완료되었습니다.")
            
            Result.success()
        } catch (e: Exception) {
            showCompletionNotification(0, "스캔 중 오류가 발생했습니다: ${e.message}")
            Result.failure()
        } finally {
            barcodeDetector.close()
            ocrExtractor.close()
        }
    }

    /**
     * 갤러리에서 최근 1000장 이미지 URI 수집
     */
    private fun getRecentImages(limit: Int): List<Uri> {
        val images = mutableListOf<Uri>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )
        
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        
        try {
            applicationContext.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "$sortOrder LIMIT $limit"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    images.add(contentUri)
                }
            }
        } catch (e: Exception) {
            // 권한 없거나 오류 발생 시 빈 리스트 반환
        }
        
        return images
    }

    /**
     * 바코드가 있는 이미지 필터링 (병렬 처리)
     */
    private suspend fun scanBarcodes(images: List<Uri>): List<Uri> = coroutineScope {
        val batchSize = 50 // 배치 크기
        val barcodeImages = mutableListOf<Uri>()
        
        images.chunked(batchSize).forEachIndexed { batchIndex, batch ->
            val jobs = batch.mapIndexed { index, uri ->
                async {
                    try {
                        val hasBarcode = barcodeDetector.hasBarcode(applicationContext, uri)
                        val currentProgress = batchIndex * batchSize + index + 1
                        
                        // 진행률 업데이트 (5장마다)
                        if (currentProgress % 5 == 0) {
                            setProgress(workDataOf(
                                PROGRESS_KEY to currentProgress,
                                TOTAL_KEY to images.size,
                                STAGE_KEY to STAGE_BARCODE_SCAN,
                                FOUND_KEY to barcodeImages.size
                            ))
                            
                            showProgressNotification(
                                STAGE_BARCODE_SCAN, 
                                currentProgress, 
                                images.size, 
                                barcodeImages.size
                            )
                        }
                        
                        if (hasBarcode) uri else null
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            
            val results = jobs.awaitAll()
            barcodeImages.addAll(results.filterNotNull())
            
            // 배치 간 잠시 대기 (과부하 방지)
            delay(100)
        }
        
        barcodeImages
    }

    /**
     * OCR 처리 및 기프티콘 판별 (병렬 처리)
     */
    private suspend fun processGifticons(images: List<Uri>): List<Gifticon> = coroutineScope {
        val batchSize = 10 // OCR은 더 작은 배치 크기
        val gifticons = mutableListOf<Gifticon>()
        
        images.chunked(batchSize).forEachIndexed { batchIndex, batch ->
            val jobs = batch.mapIndexed { index, uri ->
                async {
                    try {
                        val extractedInfo = ocrExtractor.extractGifticonInfo(applicationContext, uri)
                        val currentProgress = batchIndex * batchSize + index + 1
                        
                        // 진행률 업데이트
                        setProgress(workDataOf(
                            PROGRESS_KEY to currentProgress,
                            TOTAL_KEY to images.size,
                            STAGE_KEY to STAGE_OCR_PROCESSING,
                            FOUND_KEY to gifticons.size
                        ))
                        
                        showProgressNotification(
                            STAGE_OCR_PROCESSING, 
                            currentProgress, 
                            images.size, 
                            gifticons.size
                        )
                        
                        // 만료일이 있으면 기프티콘으로 판별
                        if (!extractedInfo.expiryDate.isNullOrEmpty()) {
                            // 이미지를 내부 저장소에 복사
                            val internalImagePath = ImageStorageUtil.copyImageToInternalStorage(
                                applicationContext, uri
                            )
                            
                            if (internalImagePath != null) {
                                Gifticon(
                                    brandName = "갤러리 기프티콘",
                                    productName = null,
                                    expiryDate = extractedInfo.expiryDate,
                                    amount = 0,
                                    balance = 0,
                                    barcodeNumber = null,
                                    category = GifticonCategory.ETC,
                                    imagePath = "file://$internalImagePath",
                                    notes = "갤러리 자동 스캔으로 등록"
                                )
                            } else null
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            
            val results = jobs.awaitAll()
            gifticons.addAll(results.filterNotNull())
            
            // 배치 간 대기 (OCR 부하 분산)
            delay(500)
        }
        
        gifticons
    }

    /**
     * 기프티콘 데이터베이스에 저장
     */
    private suspend fun saveGifticons(gifticons: List<Gifticon>): Int {
        var savedCount = 0
        
        gifticons.forEachIndexed { index, gifticon ->
            try {
                repository.insertGifticon(gifticon)
                savedCount++
                
                // 진행률 업데이트
                setProgress(workDataOf(
                    PROGRESS_KEY to index + 1,
                    TOTAL_KEY to gifticons.size,
                    STAGE_KEY to STAGE_SAVING,
                    FOUND_KEY to savedCount
                ))
                
                showProgressNotification(
                    STAGE_SAVING, 
                    index + 1, 
                    gifticons.size, 
                    savedCount
                )
            } catch (e: Exception) {
                // 저장 실패 시 이미지 파일 삭제
                gifticon.imagePath?.let { imagePath ->
                    if (imagePath.startsWith("file://")) {
                        val filePath = imagePath.removePrefix("file://")
                        ImageStorageUtil.deleteImage(filePath)
                    }
                }
            }
        }
        
        return savedCount
    }

    /**
     * 진행 상황 알림 표시
     */
    private suspend fun showProgressNotification(
        stage: String, 
        progress: Int, 
        total: Int, 
        found: Int
    ) {
        NotificationUtil.showGalleryScanProgressNotification(
            applicationContext,
            stage,
            progress,
            total,
            found
        )
    }

    /**
     * 완료 알림 표시
     */
    private suspend fun showCompletionNotification(foundCount: Int, message: String) {
        NotificationUtil.showGalleryScanCompletionNotification(
            applicationContext,
            foundCount,
            message
        )
    }
} 