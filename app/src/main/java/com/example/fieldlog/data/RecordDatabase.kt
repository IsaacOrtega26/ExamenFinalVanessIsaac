package com.example.fieldlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Record::class], version = 1, exportSchema = false)
abstract class RecordDatabase : RoomDatabase() {
    
    abstract fun recordDao(): RecordDao
    
    companion object {
        @Volatile
        private var INSTANCE: RecordDatabase? = null
        
        fun getDatabase(context: Context): RecordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecordDatabase::class.java,
                    "fieldlog_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

