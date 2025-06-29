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
class HomeViewModel @Inject constructor(
    private val repository: GifticonRepository
) : ViewModel() {
    
    private val _gifticons = MutableStateFlow<List<Gifticon>>(emptyList())
    val gifticons: StateFlow<List<Gifticon>> = _gifticons.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadGifticons()
    }
    
    private fun loadGifticons() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getActiveGifticons().collect { gifticonList ->
                    _gifticons.value = gifticonList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun refreshGifticons() {
        loadGifticons()
    }
    
    fun deleteGifticon(gifticon: Gifticon) {
        viewModelScope.launch {
            try {
                repository.deleteGifticon(gifticon)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun markGifticonAsUsed(gifticon: Gifticon) {
        viewModelScope.launch {
            try {
                repository.markAsUsed(gifticon.id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
} 