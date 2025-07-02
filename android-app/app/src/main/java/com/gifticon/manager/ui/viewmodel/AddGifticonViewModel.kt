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
import com.gifticon.manager.utils.ImageStorageUtil
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
    
    // 전역 변수로 추출된 정보와 이미지 URI 저장 (EditGifticonViewModel에서 접근하기 위해)
    companion object {
        private var globalExtractedInfo: com.gifticon.manager.utils.ExtractedGifticonInfo? = null
        private var globalImageUri: Uri? = null
        
        fun getGlobalExtractedInfo(): com.gifticon.manager.utils.ExtractedGifticonInfo? = globalExtractedInfo
        fun getGlobalImageUri(): Uri? = globalImageUri
        fun clearGlobalData() {
            globalExtractedInfo = null
            globalImageUri = null
        }
    }
    
    fun processSelectedImage(context: Context, uri: Uri, onComplete: (com.gifticon.manager.utils.ExtractedGifticonInfo?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _imageUri.value = uri
            
            try {
                // 바코드 확인
                val hasBarcode = barcodeDetector.hasBarcode(context, uri)
                if (!hasBarcode) {
                    _error.value = "이 이미지에는 바코드나 QR코드가 없습니다. 기프티콘 사진을 선택해주세요."
                    _isLoading.value = false
                    onComplete(null)
                    return@launch
                }
                
                // 이미지를 내부 저장소에 복사
                val internalImagePath = ImageStorageUtil.copyImageToInternalStorage(context, uri)
                if (internalImagePath == null) {
                    _error.value = "이미지 저장에 실패했습니다."
                    _isLoading.value = false
                    onComplete(null)
                    return@launch
                }
                
                // OCR 실행
                println("DEBUG: OCR 추출 시작 - URI: $uri")
                val extractedInfo = ocrExtractor.extractGifticonInfo(context, uri)
                println("DEBUG: OCR 추출 완료 - 결과: $extractedInfo")
                
                _extractedInfo.value = extractedInfo
                
                // 전역 변수에 저장 (내부 저장소 경로 사용)
                globalExtractedInfo = extractedInfo
                globalImageUri = Uri.parse("file://$internalImagePath")
                
                _isLoading.value = false
                onComplete(extractedInfo)
                
            } catch (e: Exception) {
                println("DEBUG: OCR 추출 실패 - 에러: ${e.message}")
                e.printStackTrace()
                _error.value = "기프티콘 정보 추출에 실패했습니다: ${e.message}"
                _isLoading.value = false
                onComplete(null)
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        barcodeDetector.close()
        ocrExtractor.close()
    }
} 