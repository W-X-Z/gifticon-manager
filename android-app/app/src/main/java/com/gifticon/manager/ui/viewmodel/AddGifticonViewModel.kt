package com.gifticon.manager.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gifticon.manager.data.model.Gifticon
import com.gifticon.manager.data.model.GifticonCategory
import com.gifticon.manager.data.repository.GifticonRepository
import com.gifticon.manager.utils.BarcodeDetector
import com.gifticon.manager.utils.GifticonOcrExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddGifticonViewModel @Inject constructor(
    private val repository: GifticonRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()
    
    private val _analyzedGifticon = MutableStateFlow<Gifticon?>(null)
    val analyzedGifticon: StateFlow<Gifticon?> = _analyzedGifticon.asStateFlow()
    
    private val _isCheckingBarcode = MutableStateFlow(false)
    val isCheckingBarcode: StateFlow<Boolean> = _isCheckingBarcode.asStateFlow()
    
    private val _shouldNavigateBack = MutableStateFlow(false)
    val shouldNavigateBack: StateFlow<Boolean> = _shouldNavigateBack.asStateFlow()
    
    private val _extractedInfo = MutableStateFlow<com.gifticon.manager.utils.ExtractedGifticonInfo?>(null)
    val extractedInfo: StateFlow<com.gifticon.manager.utils.ExtractedGifticonInfo?> = _extractedInfo.asStateFlow()
    
    private val barcodeDetector = BarcodeDetector()
    private val ocrExtractor = GifticonOcrExtractor()
    
    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
        _error.value = null
    }
    
    fun checkImageForBarcode(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isCheckingBarcode.value = true
            _error.value = null
            
            try {
                val hasBarcode = barcodeDetector.hasBarcode(context, uri)
                if (hasBarcode) {
                    _imageUri.value = uri
                    extractGifticonInfo(context, uri)
                } else {
                    _error.value = "이 이미지에는 바코드나 QR코드가 없습니다. 기프티콘 사진을 선택해주세요."
                }
            } catch (e: Exception) {
                _error.value = "이미지 분석 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isCheckingBarcode.value = false
            }
        }
    }
    
    private fun extractGifticonInfo(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                println("DEBUG: OCR 추출 시작 - URI: $uri")
                val extractedInfo = ocrExtractor.extractGifticonInfo(context, uri)
                println("DEBUG: OCR 추출 완료 - 결과: $extractedInfo")
                
                _extractedInfo.value = extractedInfo
                
                // 추출된 정보로 Gifticon 객체 생성 (임시)
                val gifticon = createGifticonFromExtractedInfo(extractedInfo)
                _analyzedGifticon.value = gifticon
                
                _isLoading.value = false
                
            } catch (e: Exception) {
                println("DEBUG: OCR 추출 실패 - 에러: ${e.message}")
                e.printStackTrace()
                _error.value = "기프티콘 정보 추출에 실패했습니다: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    private fun createGifticonFromExtractedInfo(extractedInfo: com.gifticon.manager.utils.ExtractedGifticonInfo): Gifticon {
        return Gifticon(
            brandName = "기프티콘",
            productName = null,
            expiryDate = extractedInfo.expiryDate ?: "2024-12-31",
            amount = 0,
            balance = 0,
            barcodeNumber = null,
            category = GifticonCategory.ETC,
            purchaseDate = null
        )
    }
    
    private fun determineCategory(brandName: String?): GifticonCategory {
        return GifticonCategory.ETC
    }
    
    fun analyzeImage() {
        val uri = _imageUri.value ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // 임시로 더미 데이터 사용
                val dummyGifticon = Gifticon(
                    brandName = "스타벅스",
                    productName = "아메리카노",
                    expiryDate = "2024-12-31",
                    amount = 5000,
                    balance = 5000,
                    category = GifticonCategory.CAFE
                )
                
                _analyzedGifticon.value = dummyGifticon
                _isLoading.value = false
                
            } catch (e: Exception) {
                _error.value = "이미지 분석에 실패했습니다: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun saveGifticon() {
        val gifticon = _analyzedGifticon.value ?: return
        
        viewModelScope.launch {
            try {
                repository.insertGifticon(gifticon)
                // 성공 시 초기화
                _analyzedGifticon.value = null
                _extractedInfo.value = null
                _imageUri.value = null
                _error.value = null
                // 홈 화면으로 돌아가기
                _shouldNavigateBack.value = true
            } catch (e: Exception) {
                _error.value = "기프티콘 저장에 실패했습니다: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun resetNavigation() {
        _shouldNavigateBack.value = false
    }
    
    // 추출된 정보와 이미지 URI를 반환하는 함수
    fun getExtractedInfoAndImageUri(): Pair<com.gifticon.manager.utils.ExtractedGifticonInfo?, Uri?> {
        return Pair(_extractedInfo.value, _imageUri.value)
    }
    
    override fun onCleared() {
        super.onCleared()
        barcodeDetector.close()
        ocrExtractor.close()
    }
} 