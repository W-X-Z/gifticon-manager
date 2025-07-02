package com.gifticon.manager.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BarcodeDetector {
    
    companion object {
        private const val TAG = "BarcodeDetector"
    }
    
    private val scanner = BarcodeScanning.getClient()
    
    suspend fun hasBarcode(context: Context, uri: Uri): Boolean {
        return try {
            Log.d(TAG, "Starting barcode detection for URI: $uri")
            
            val bitmap = getBitmapFromUri(context, uri)
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI: $uri")
                return false
            }
            
            Log.d(TAG, "Bitmap decoded successfully: ${bitmap.width}x${bitmap.height}")
            
            val image = InputImage.fromBitmap(bitmap, 0)
            
            suspendCancellableCoroutine { continuation ->
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        Log.d(TAG, "Barcode detection completed. Found ${barcodes.size} barcodes")
                        barcodes.forEach { barcode ->
                            Log.d(TAG, "Barcode type: ${barcode.format}, value: ${barcode.rawValue}")
                        }
                        val hasBarcode = barcodes.isNotEmpty()
                        continuation.resume(hasBarcode)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Barcode detection failed: ${exception.message}", exception)
                        continuation.resume(false)
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in hasBarcode: ${e.message}", e)
            false
        }
    }
    
    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            Log.d(TAG, "Attempting to decode bitmap from URI: $uri")
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // 먼저 이미지 크기만 확인
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                
                // 적절한 샘플링 크기 계산
                val maxSize = 1024
                var inSampleSize = 1
                if (options.outHeight > maxSize || options.outWidth > maxSize) {
                    val halfHeight = options.outHeight / 2
                    val halfWidth = options.outWidth / 2
                    while (halfHeight / inSampleSize >= maxSize && halfWidth / inSampleSize >= maxSize) {
                        inSampleSize *= 2
                    }
                }
                
                Log.d(TAG, "Image size: ${options.outWidth}x${options.outHeight}, sample size: $inSampleSize")
                
                // 스트림을 다시 열어서 실제 비트맵 디코딩
                context.contentResolver.openInputStream(uri)?.use { inputStream2 ->
                    val decodeOptions = BitmapFactory.Options().apply {
                        this.inSampleSize = inSampleSize
                    }
                    
                    val bitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
                    if (bitmap == null) {
                        Log.e(TAG, "Failed to decode bitmap from URI: $uri")
                    } else {
                        Log.d(TAG, "Successfully decoded bitmap: ${bitmap.width}x${bitmap.height}")
                    }
                    bitmap
                }
            } ?: run {
                Log.e(TAG, "Failed to open input stream for URI: $uri")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode bitmap: ${e.message}", e)
            null
        }
    }
    
    fun close() {
        scanner.close()
    }
} 