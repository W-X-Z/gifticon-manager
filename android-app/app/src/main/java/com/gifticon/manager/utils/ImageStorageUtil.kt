package com.gifticon.manager.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ImageStorageUtil {
    
    private const val IMAGE_DIRECTORY = "gifticon_images"
    
    /**
     * URI에서 이미지를 앱의 내부 저장소로 복사하고 파일 경로를 반환
     */
    fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                val fileName = generateFileName()
                val file = createImageFile(context, fileName)
                
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
                outputStream.close()
                
                file.absolutePath
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 파일 경로에서 Bitmap을 로드
     */
    fun loadBitmapFromPath(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(filePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 이미지 파일이 존재하는지 확인
     */
    fun isImageExists(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            file.exists()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 이미지 파일 삭제
     */
    fun deleteImage(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 앱의 모든 이미지 파일 삭제
     */
    fun clearAllImages(context: Context) {
        try {
            val imageDir = File(context.filesDir, IMAGE_DIRECTORY)
            if (imageDir.exists()) {
                imageDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun generateFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val random = Random().nextInt(1000)
        return "gifticon_${timestamp}_${random}.jpg"
    }
    
    private fun createImageFile(context: Context, fileName: String): File {
        val imageDir = File(context.filesDir, IMAGE_DIRECTORY)
        if (!imageDir.exists()) {
            imageDir.mkdirs()
        }
        return File(imageDir, fileName)
    }
} 