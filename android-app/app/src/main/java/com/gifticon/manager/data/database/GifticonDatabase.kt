package com.gifticon.manager.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
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
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // 데이터베이스 생성 시 로그 출력
                        android.util.Log.d("GifticonDatabase", "데이터베이스가 새로 생성되었습니다.")
                    }
                    
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // 데이터베이스 열릴 때 로그 출력
                        android.util.Log.d("GifticonDatabase", "데이터베이스가 열렸습니다.")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 