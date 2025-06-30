package com.gifticon.manager.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gifticon.manager.data.model.Gifticon
import com.gifticon.manager.data.model.GifticonCategory
import com.gifticon.manager.data.repository.GifticonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditGifticonViewModel @Inject constructor(
    private val repository: GifticonRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _shouldNavigateBack = MutableStateFlow(false)
    val shouldNavigateBack: StateFlow<Boolean> = _shouldNavigateBack.asStateFlow()
    
    // 편집 가능한 필드들
    private val _expiryDate = MutableStateFlow("")
    val expiryDate: StateFlow<String> = _expiryDate.asStateFlow()
    
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()
    
    init {
        // AddGifticonViewModel에서 추출된 정보를 가져와서 초기화
        // 실제 추출된 정보가 있을 때만 초기화
    }
    
    private fun loadExtractedInfo() {
        // 실제 추출된 정보가 있을 때만 초기화
        // 기본값은 빈 상태로 유지
    }
    
    // 추출된 정보로 초기화
    fun initializeWithExtractedInfo(
        extractedInfo: com.gifticon.manager.utils.ExtractedGifticonInfo,
        imageUri: Uri
    ) {
        _expiryDate.value = extractedInfo.expiryDate ?: ""
        _imageUri.value = imageUri
    }
    
    // 필드 업데이트 함수들
    fun updateExpiryDate(date: String) {
        _expiryDate.value = date
    }
    
    fun saveGifticon() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val gifticon = Gifticon(
                    brandName = "기프티콘", // 브랜드명은 사용자가 직접 입력하지 않음
                    productName = null, // 상품명도 사용자가 직접 입력하지 않음
                    expiryDate = expiryDate.value,
                    amount = 0, // 금액은 0으로 설정
                    balance = 0, // 잔액도 0으로 설정
                    barcodeNumber = null, // 바코드 번호 제거
                    category = GifticonCategory.ETC, // 기본 카테고리
                    imagePath = imageUri.value?.toString()
                )
                
                repository.insertGifticon(gifticon)
                _shouldNavigateBack.value = true
                
            } catch (e: Exception) {
                _error.value = "기프티콘 저장에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetNavigation() {
        _shouldNavigateBack.value = false
    }
    
    fun clearError() {
        _error.value = null
    }
} 