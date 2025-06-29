package com.gifticon.manager.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.gifticon.manager.data.model.Gifticon

@Database(
    entities = [Gifticon::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GifticonDatabase : RoomDatabase() {
    
    abstract fun gifticonDao(): GifticonDao
    
    companion object {
        @Volatile
        private var INSTANCE: GifticonDatabase? = null
        
        fun getDatabase(context: Context): GifticonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GifticonDatabase::class.java,
                    "gifticon_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 