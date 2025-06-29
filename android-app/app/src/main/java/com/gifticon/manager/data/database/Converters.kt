package com.gifticon.manager.data.database

import androidx.room.TypeConverter
import com.gifticon.manager.data.model.GifticonCategory

class Converters {
    
    @TypeConverter
    fun fromGifticonCategory(category: GifticonCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toGifticonCategory(categoryString: String): GifticonCategory {
        return try {
            GifticonCategory.valueOf(categoryString)
        } catch (e: IllegalArgumentException) {
            GifticonCategory.ETC
        }
    }
} 