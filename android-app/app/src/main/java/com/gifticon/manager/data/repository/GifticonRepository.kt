package com.gifticon.manager.data.repository

import com.gifticon.manager.data.api.AnalyzeImageRequest
import com.gifticon.manager.data.api.GifticonAnalysisService
import com.gifticon.manager.data.database.GifticonDao
import com.gifticon.manager.data.model.Gifticon
import com.gifticon.manager.data.model.GifticonCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GifticonRepository @Inject constructor(
    private val gifticonDao: GifticonDao,
    private val analysisService: GifticonAnalysisService
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
    
    suspend fun updateBalance(id: Long, newBalance: Int) = 
        gifticonDao.updateBalance(id, newBalance)
    
    suspend fun markAsUsed(id: Long) = gifticonDao.markAsUsed(id)
    
    // API Operations
    suspend fun analyzeGifticonImage(imageBase64: String, mimeType: String?): Result<Gifticon> {
        return try {
            val request = AnalyzeImageRequest(imageBase64, mimeType)
            val response = analysisService.analyzeImage(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    val gifticon = Gifticon(
                        brandName = data.brandName,
                        productName = data.productName,
                        expiryDate = data.expiryDate,
                        amount = data.amount,
                        balance = data.balance,
                        barcodeNumber = data.barcodeNumber,
                        category = GifticonCategory.valueOf(data.category.uppercase()),
                        purchaseDate = data.purchaseDate
                    )
                    Result.success(gifticon)
                } else {
                    Result.failure(Exception("응답 데이터가 비어있습니다."))
                }
            } else {
                val errorMessage = response.body()?.error ?: "이미지 분석에 실패했습니다."
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkApiHealth(): Boolean {
        return try {
            val response = analysisService.checkHealth()
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            false
        }
    }
} 