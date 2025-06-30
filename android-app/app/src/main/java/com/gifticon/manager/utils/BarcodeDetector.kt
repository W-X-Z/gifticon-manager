package com.gifticon.manager.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BarcodeDetector {
    
    private val scanner = BarcodeScanning.getClient()
    
    suspend fun hasBarcode(context: Context, uri: Uri): Boolean {
        return try {
            val bitmap = getBitmapFromUri(context, uri)
            val image = InputImage.fromBitmap(bitmap, 0)
            
            suspendCancellableCoroutine { continuation ->
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        val hasBarcode = barcodes.isNotEmpty()
                        continuation.resume(hasBarcode)
                    }
                    .addOnFailureListener {
                        continuation.resume(false)
                    }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }
    
    fun close() {
        scanner.close()
    }
} 