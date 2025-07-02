package com.gifticon.manager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gifticon.manager.data.model.Gifticon
import com.gifticon.manager.data.repository.GifticonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageViewerViewModel @Inject constructor(
    private val repository: GifticonRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _gifticon = MutableStateFlow<Gifticon?>(null)
    val gifticon: StateFlow<Gifticon?> = _gifticon.asStateFlow()
    
    fun loadGifticon(gifticonId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val gifticon = repository.getGifticonById(gifticonId)
                _gifticon.value = gifticon
            } catch (e: Exception) {
                // 에러 처리
                android.util.Log.e("ImageViewerViewModel", "기프티콘 로드 실패: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
} 