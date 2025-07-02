package com.gifticon.manager.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gifticon.manager.data.model.Gifticon
import com.gifticon.manager.data.repository.GifticonRepository
import com.gifticon.manager.utils.ImageStorageUtil
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
    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()
    private val _expiryDate = MutableStateFlow("")
    val expiryDate: StateFlow<String> = _expiryDate.asStateFlow()
    private val _productName = MutableStateFlow("")
    val productName: StateFlow<String> = _productName.asStateFlow()
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri.asStateFlow()
    private val _gifticon = MutableStateFlow<Gifticon?>(null)
    val gifticon: StateFlow<Gifticon?> = _gifticon.asStateFlow()
    private var currentGifticonId: Long = 0
    
    init {
        loadExtractedInfo()
    }
    private fun loadExtractedInfo() {
        val extractedInfo = AddGifticonViewModel.getGlobalExtractedInfo()
        val imageUri = AddGifticonViewModel.getGlobalImageUri()
        if (extractedInfo != null && imageUri != null) {
            _expiryDate.value = extractedInfo.expiryDate ?: ""
            _imageUri.value = imageUri
        }
    }
    
    fun loadGifticon(gifticonId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val gifticon = repository.getGifticonById(gifticonId)
                if (gifticon != null) {
                    _gifticon.value = gifticon
                    _expiryDate.value = gifticon.expiryDate
                    _amount.value = gifticon.amount.toString()
                    _productName.value = gifticon.productName ?: ""
                    _imageUri.value = gifticon.imagePath?.let { Uri.parse(it) }
                    currentGifticonId = gifticonId
                }
            } catch (e: Exception) {
                _error.value = "기프티콘을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun updateAmount(amount: String) {
        _amount.value = amount
    }
    fun updateExpiryDate(date: String) {
        _expiryDate.value = date
    }
    
    fun updateProductName(name: String) {
        _productName.value = name
    }
    fun saveGifticon() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val amountValue = amount.value.toIntOrNull() ?: 0
                // 상품명이 있지만 금액이 0인 경우 상품권으로 처리
                
                if (currentGifticonId > 0) {
                    // 기존 기프티콘 업데이트
                    val updatedGifticon = gifticon.value?.copy(
                        expiryDate = expiryDate.value,
                        amount = amountValue,
                        balance = amountValue,
                        productName = if (productName.value.isNotEmpty()) productName.value else null
                    )
                    if (updatedGifticon != null) {
                        repository.updateGifticon(updatedGifticon)
                    }
                } else {
                    // 새 기프티콘 생성
                    val newGifticon = Gifticon(
                        brandName = "기프티콘",
                        productName = if (productName.value.isNotEmpty()) productName.value else null,
                        expiryDate = expiryDate.value,
                        amount = amountValue,
                        balance = amountValue,
                        barcodeNumber = null,
                        category = com.gifticon.manager.data.model.GifticonCategory.ETC,
                        imagePath = imageUri.value?.toString()
                    )
                    repository.insertGifticon(newGifticon)
                    AddGifticonViewModel.clearGlobalData()
                }
                _shouldNavigateBack.value = true
            } catch (e: Exception) {
                _error.value = "기프티콘 저장에 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteGifticon() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (currentGifticonId > 0) {
                    // 기프티콘 삭제 전에 이미지 파일도 삭제
                    val gifticon = repository.getGifticonById(currentGifticonId)
                    gifticon?.imagePath?.let { imagePath ->
                        if (imagePath.startsWith("file://")) {
                            val filePath = imagePath.removePrefix("file://")
                            ImageStorageUtil.deleteImage(filePath)
                        }
                    }
                    repository.deleteGifticon(currentGifticonId)
                    _shouldNavigateBack.value = true
                }
            } catch (e: Exception) {
                _error.value = "기프티콘 삭제에 실패했습니다: ${e.message}"
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