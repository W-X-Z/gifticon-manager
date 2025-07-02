package com.gifticon.manager.data.repository

import com.gifticon.manager.data.database.GifticonDao
import com.gifticon.manager.data.model.Gifticon
import com.gifticon.manager.data.model.GifticonCategory
import com.gifticon.manager.utils.ImageStorageUtil
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GifticonRepository @Inject constructor(
    private val gifticonDao: GifticonDao
) {
    
    // Local Database Operations
    fun getAllGifticons(): Flow<List<Gifticon>> = gifticonDao.getAllGifticons()
    
    fun getActiveGifticons(): Flow<List<Gifticon>> = gifticonDao.getActiveGifticons()
    
    fun getExpiringSoonGifticons(): Flow<List<Gifticon>> = gifticonDao.getExpiringSoonGifticons()
    
    fun getGifticonsByCategory(category: GifticonCategory): Flow<List<Gifticon>> = 
        gifticonDao.getGifticonsByCategory(category)
    
    fun searchGifticons(query: String): Flow<List<Gifticon>> = 
        gifticonDao.searchGifticons(query)
    
    suspend fun getGifticonById(id: Long): Gifticon? = gifticonDao.getGifticonById(id)
    
    suspend fun insertGifticon(gifticon: Gifticon): Long = gifticonDao.insertGifticon(gifticon)
    
    suspend fun updateGifticon(gifticon: Gifticon) = gifticonDao.updateGifticon(gifticon)
    
    suspend fun deleteGifticon(gifticon: Gifticon) = gifticonDao.deleteGifticon(gifticon)
    
    suspend fun deleteGifticon(id: Long) {
        // 기프티콘 삭제 전에 이미지 파일도 삭제
        val gifticon = gifticonDao.getGifticonById(id)
        gifticon?.imagePath?.let { imagePath ->
            if (imagePath.startsWith("file://")) {
                val filePath = imagePath.removePrefix("file://")
                ImageStorageUtil.deleteImage(filePath)
            }
        }
        gifticonDao.deleteGifticonById(id)
    }
    
    suspend fun updateBalance(id: Long, newBalance: Int) = 
        gifticonDao.updateBalance(id, newBalance)
    
    suspend fun markAsUsed(id: Long) = gifticonDao.markAsUsed(id)
    

} 