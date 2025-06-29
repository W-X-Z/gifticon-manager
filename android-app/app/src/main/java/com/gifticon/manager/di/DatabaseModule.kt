package com.gifticon.manager.di

import android.content.Context
import androidx.room.Room
import com.gifticon.manager.data.database.GifticonDao
import com.gifticon.manager.data.database.GifticonDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideGifticonDatabase(@ApplicationContext context: Context): GifticonDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            GifticonDatabase::class.java,
            "gifticon_database"
        ).build()
    }
    
    @Provides
    fun provideGifticonDao(database: GifticonDatabase): GifticonDao {
        return database.gifticonDao()
    }
} 