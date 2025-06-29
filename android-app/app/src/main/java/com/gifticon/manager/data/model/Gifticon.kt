package com.gifticon.manager.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "gifticons")
@Parcelize
data class Gifticon(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brandName: String,
    val productName: String? = null,
    val expiryDate: String, // YYYY-MM-DD 형식
    val amount: Int = 0,
    val balance: Int = 0,
    val barcodeNumber: String? = null,
    val category: GifticonCategory = GifticonCategory.ETC,
    val purchaseDate: String? = null,
    val notes: String? = null,
    val imagePath: String? = null,
    val isUsed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable

enum class GifticonCategory {
    CAFE, MOVIE, CONVENIENCE_STORE, CHICKEN, FAST_FOOD, BEAUTY, SHOPPING, ETC
} 