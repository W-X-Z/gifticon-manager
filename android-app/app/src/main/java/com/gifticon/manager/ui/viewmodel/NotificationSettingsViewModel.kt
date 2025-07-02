package com.gifticon.manager.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.gifticon.manager.worker.GifticonExpiryWorker
import com.gifticon.manager.utils.NotificationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _isNotificationEnabled = MutableStateFlow(false)
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()
    
    private val _notificationDays = MutableStateFlow<Set<Int>>(emptySet())
    val notificationDays: StateFlow<Set<Int>> = _notificationDays.asStateFlow()
    
    private val workManager = WorkManager.getInstance(context)
    
    init {
        loadNotificationSettings()
    }
    
    private fun loadNotificationSettings() {
        val sharedPrefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
        _isNotificationEnabled.value = sharedPrefs.getBoolean("is_enabled", false)
        
        val daysString = sharedPrefs.getString("notification_days", "")
        if (daysString?.isNotEmpty() == true) {
            val days = daysString.split(",").mapNotNull { it.toIntOrNull() }.toSet()
            _notificationDays.value = days
        }
    }
    
    fun toggleNotification() {
        _isNotificationEnabled.value = !_isNotificationEnabled.value
    }
    
    fun addNotificationDay(days: Int) {
        val currentDays = _notificationDays.value.toMutableSet()
        currentDays.add(days)
        _notificationDays.value = currentDays
    }
    
    fun removeNotificationDay(days: Int) {
        val currentDays = _notificationDays.value.toMutableSet()
        currentDays.remove(days)
        _notificationDays.value = currentDays
    }
    
    fun saveNotificationSettings() {
        viewModelScope.launch {
            val sharedPrefs = context.getSharedPreferences("notification_settings", Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            
            editor.putBoolean("is_enabled", _isNotificationEnabled.value)
            editor.putString("notification_days", _notificationDays.value.joinToString(","))
            
            editor.apply()
            
            scheduleNotifications()
        }
    }
    
    private fun scheduleNotifications() {
        // 기존 작업 취소
        workManager.cancelAllWork()
        
        if (_isNotificationEnabled.value && _notificationDays.value.isNotEmpty()) {
            // 매일 실행되는 주기적 작업 스케줄링
            val workRequest = PeriodicWorkRequestBuilder<GifticonExpiryWorker>(
                1, TimeUnit.DAYS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            ).build()
            
            workManager.enqueueUniquePeriodicWork(
                "gifticon_expiry_check",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        } else {
            // 알림이 비활성화되면 모든 알림 취소
            NotificationUtil.cancelAllNotifications(context)
        }
    }
} 