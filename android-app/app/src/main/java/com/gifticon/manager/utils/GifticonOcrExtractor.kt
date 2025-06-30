package com.gifticon.manager.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.text.SimpleDateFormat
import java.util.*

data class ExtractedGifticonInfo(
    val expiryDate: String? = null
)

class GifticonOcrExtractor {
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    suspend fun extractGifticonInfo(context: Context, uri: Uri): ExtractedGifticonInfo {
        return try {
            println("DEBUG: OCR 시작 - URI: $uri")
            
            val bitmap = getBitmapFromUri(context, uri)
            val image = InputImage.fromBitmap(bitmap, 0)
            
            suspendCancellableCoroutine { continuation ->
                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        println("DEBUG: OCR 텍스트 추출 성공 - 텍스트: ${visionText.text}")
                        val extractedInfo = processText(visionText.text)
                        println("DEBUG: OCR 처리된 정보: $extractedInfo")
                        continuation.resume(extractedInfo)
                    }
                    .addOnFailureListener { exception ->
                        println("DEBUG: OCR 텍스트 추출 실패 - 에러: ${exception.message}")
                        exception.printStackTrace()
                        continuation.resume(ExtractedGifticonInfo())
                    }
            }
            
        } catch (e: Exception) {
            println("DEBUG: OCR 전체 처리 실패 - 에러: ${e.message}")
            e.printStackTrace()
            ExtractedGifticonInfo()
        }
    }
    
    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }
    
    private fun processText(text: String): ExtractedGifticonInfo {
        println("DEBUG: 텍스트 처리 시작 - 원본 텍스트: $text")
        
        // 모든 숫자 추출
        val allNumbers = extractAllNumbers(text)
        println("DEBUG: 추출된 모든 숫자: $allNumbers")
        
        // 만료일 찾기
        val expiryDate = findExpiryDate(text, allNumbers)
        println("DEBUG: 찾은 만료일: $expiryDate")
        
        return ExtractedGifticonInfo(
            expiryDate = expiryDate
        )
    }
    
    private fun extractAllNumbers(text: String): List<String> {
        // 숫자 패턴 매칭 (연속된 숫자들)
        val numberPattern = Regex("\\d+")
        return numberPattern.findAll(text).map { it.value }.toList()
    }
    
    private fun findExpiryDate(text: String, numbers: List<String>): String? {
        println("DEBUG: 만료일 찾기 시작 - 텍스트: $text, 숫자들: $numbers")
        
        // 날짜 패턴들
        val datePatterns = listOf(
            Regex("(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})"), // YYYY-MM-DD, YYYY/MM/DD
            Regex("(\\d{1,2})[-/](\\d{1,2})[-/](\\d{4})"), // MM-DD-YYYY, MM/DD/YYYY
            Regex("(\\d{4})[.](\\d{1,2})[.](\\d{1,2})"), // YYYY.MM.DD
            Regex("(\\d{1,2})[.](\\d{1,2})[.](\\d{4})"), // MM.DD.YYYY
            Regex("(\\d{2})[.](\\d{2})[.](\\d{2})"), // YY.MM.DD
            Regex("(\\d{2})[-/](\\d{2})[-/](\\d{2})"), // YY-MM-DD, YY/MM/DD
            Regex("(\\d{4})(\\d{2})(\\d{2})"), // YYYYMMDD
            Regex("(\\d{2})(\\d{2})(\\d{4})")  // MMDDYYYY
        )
        
        // 만료 관련 키워드
        val expiryKeywords = listOf("만료", "유효기간", "사용기한", "expiry", "valid", "until", "기한", "기간", "~까지")
        
        // 1. 만료 키워드가 있는 라인에서 날짜 찾기
        val lines = text.split("\n")
        for (line in lines) {
            val hasExpiryKeyword = expiryKeywords.any { line.contains(it, ignoreCase = true) }
            if (hasExpiryKeyword) {
                for (pattern in datePatterns) {
                    val match = pattern.find(line)
                    if (match != null) {
                        val dateStr = match.value
                        val parsedDate = parseDate(dateStr)
                        if (parsedDate != null) {
                            println("DEBUG: 만료 키워드로 찾은 날짜: $dateStr -> ${formatDate(parsedDate)}")
                            return formatDate(parsedDate)
                        }
                    }
                }
            }
        }
        
        // 2. 숫자들 중에서 날짜로 해석 가능한 것 찾기 (연속된 3개 숫자) - 우선순위 높음
        for (i in 0 until numbers.size - 2) {
            val year = numbers[i].toIntOrNull()
            val month = numbers[i + 1].toIntOrNull()
            val day = numbers[i + 2].toIntOrNull()
            
            println("DEBUG: 연속 숫자 확인 - 인덱스 $i: year=$year, month=$month, day=$day")
            
            if (year != null && month != null && day != null) {
                // 연도가 2024-2030 범위이고, 월이 1-12, 일이 1-31인 경우
                if (year in 2024..2030 && month in 1..12 && day in 1..31) {
                    val dateStr = "$year-$month-$day"
                    val parsedDate = parseDate(dateStr)
                    if (parsedDate != null && isFutureDate(parsedDate)) {
                        println("DEBUG: 연속 숫자로 찾은 날짜: $year-$month-$day -> ${formatDate(parsedDate)}")
                        return formatDate(parsedDate)
                    }
                }
                // YY.MM.DD 형식 (25.06.30 -> 2025-06-30)
                else if (year in 20..30 && month in 1..12 && day in 1..31) {
                    println("DEBUG: YY.MM.DD 조건 만족 - year=$year (20-30), month=$month (1-12), day=$day (1-31)")
                    val fullYear = 2000 + year
                    val dateStr = "$fullYear-$month-$day"
                    val parsedDate = parseDate(dateStr)
                    if (parsedDate != null && isFutureDate(parsedDate)) {
                        println("DEBUG: YY.MM.DD 형식으로 찾은 날짜: $year.$month.$day -> ${formatDate(parsedDate)}")
                        return formatDate(parsedDate)
                    } else {
                        println("DEBUG: YY.MM.DD 파싱 실패 또는 과거 날짜 - dateStr=$dateStr, parsedDate=$parsedDate")
                    }
                } else {
                    println("DEBUG: 연속 숫자가 날짜 조건을 만족하지 않음 - year=$year (20-30 또는 2024-2030), month=$month (1-12), day=$day (1-31)")
                    println("DEBUG: 조건 검사 - year in 20..30: ${year in 20..30}, month in 1..12: ${month in 1..12}, day in 1..31: ${day in 1..31}")
                }
            }
        }
        
        // 3. 전체 텍스트에서 미래 날짜 찾기 (비현실적인 날짜 제외)
        for (pattern in datePatterns) {
            val matches = pattern.findAll(text)
            for (match in matches) {
                val dateStr = match.value
                val parsedDate = parseDate(dateStr)
                if (parsedDate != null && isFutureDate(parsedDate) && isRealisticDate(parsedDate)) {
                    println("DEBUG: 미래 날짜로 찾은 날짜: $dateStr -> ${formatDate(parsedDate)}")
                    return formatDate(parsedDate)
                }
            }
        }
        
        // 4. 8자리 숫자 중에서 날짜 찾기 (비현실적인 날짜 제외)
        for (number in numbers) {
            if (number.length == 8) { // YYYYMMDD 형식
                val year = number.substring(0, 4).toIntOrNull()
                val month = number.substring(4, 6).toIntOrNull()
                val day = number.substring(6, 8).toIntOrNull()
                
                if (year != null && month != null && day != null && 
                    year in 2024..2030 && month in 1..12 && day in 1..31) {
                    val dateStr = "$year-$month-$day"
                    val parsedDate = parseDate(dateStr)
                    if (parsedDate != null && isFutureDate(parsedDate)) {
                        println("DEBUG: 8자리 숫자로 찾은 날짜: $number -> ${formatDate(parsedDate)}")
                        return formatDate(parsedDate)
                    }
                }
            }
        }
        
        println("DEBUG: 만료일을 찾지 못했습니다")
        return null
    }
    
    private fun parseDate(dateStr: String): Date? {
        val patterns = listOf(
            "yyyy-MM-dd", "yyyy/MM/dd", "MM-dd-yyyy", "MM/dd/yyyy",
            "yyyy.MM.dd", "MM.dd.yyyy", "yy.MM.dd", "yy-MM-dd", "yy/MM/dd",
            "yyyyMMdd", "MMddyyyy"
        )
        
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                return sdf.parse(dateStr)
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }
    
    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(date)
    }
    
    private fun isFutureDate(date: Date): Boolean {
        val today = Calendar.getInstance()
        val targetDate = Calendar.getInstance()
        targetDate.time = date
        
        // 오늘 날짜의 시간을 00:00:00으로 설정하여 날짜만 비교
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        targetDate.set(Calendar.HOUR_OF_DAY, 0)
        targetDate.set(Calendar.MINUTE, 0)
        targetDate.set(Calendar.SECOND, 0)
        targetDate.set(Calendar.MILLISECOND, 0)
        
        // 만료일은 당일까지 유효하므로 after() 대신 !before() 사용
        val isValid = !targetDate.before(today)
        println("DEBUG: 미래 날짜 검사 - targetDate: ${formatDate(date)}, today: ${formatDate(today.time)}, isValid: $isValid")
        
        return isValid
    }
    
    private fun isRealisticDate(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        // 현실적인 범위: 2024-2030년, 월 1-12, 일 1-31
        return year in 2024..2030 && month in 1..12 && day in 1..31
    }
    
    fun close() {
        textRecognizer.close()
    }
} 