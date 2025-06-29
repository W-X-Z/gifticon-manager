package com.gifticon.manager.data.database

import androidx.room.*
import com.gifticon.manager.data.model.Gifticon
import com.gifticon.manager.data.model.GifticonCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface GifticonDao {
    
    @Query("SELECT * FROM gifticons ORDER BY expiryDate ASC")
    fun getAllGifticons(): Flow<List<Gifticon>>
    
    @Query("SELECT * FROM gifticons WHERE id = :id")
    suspend fun getGifticonById(id: Long): Gifticon?
    
    @Query("SELECT * FROM gifticons WHERE category = :category ORDER BY expiryDate ASC")
    fun getGifticonsByCategory(category: GifticonCategory): Flow<List<Gifticon>>
    
    @Query("SELECT * FROM gifticons WHERE isUsed = 0 AND date(expiryDate) >= date('now') ORDER BY expiryDate ASC")
    fun getActiveGifticons(): Flow<List<Gifticon>>
    
    @Query("SELECT * FROM gifticons WHERE isUsed = 0 AND date(expiryDate) >= date('now') AND date(expiryDate) <= date('now', '+7 days') ORDER BY expiryDate ASC")
    fun getExpiringSoonGifticons(): Flow<List<Gifticon>>
    
    @Query("SELECT * FROM gifticons WHERE date(expiryDate) < date('now') OR isUsed = 1 ORDER BY expiryDate DESC")
    fun getExpiredOrUsedGifticons(): Flow<List<Gifticon>>
    
    @Query("SELECT * FROM gifticons WHERE brandName LIKE '%' || :query || '%' OR productName LIKE '%' || :query || '%' ORDER BY expiryDate ASC")
    fun searchGifticons(query: String): Flow<List<Gifticon>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGifticon(gifticon: Gifticon): Long
    
    @Update
    suspend fun updateGifticon(gifticon: Gifticon)
    
    @Delete
    suspend fun deleteGifticon(gifticon: Gifticon)
    
    @Query("DELETE FROM gifticons WHERE id = :id")
    suspend fun deleteGifticonById(id: Long)
    
    @Query("UPDATE gifticons SET balance = :newBalance, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBalance(id: Long, newBalance: Int, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE gifticons SET isUsed = 1, balance = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsUsed(id: Long, updatedAt: Long = System.currentTimeMillis())
} 