package com.example.fieldlog.data

import kotlinx.coroutines.flow.Flow

class RecordRepository(private val recordDao: RecordDao) {
    
    val allRecords: Flow<List<Record>> = recordDao.getAllRecords()
    
    suspend fun insert(record: Record): Long {
        return recordDao.insert(record)
    }
    
    suspend fun update(record: Record) {
        recordDao.update(record)
    }
    
    suspend fun delete(record: Record) {
        recordDao.delete(record)
    }
    
    suspend fun getRecordById(id: Int): Record? {
        return recordDao.getRecordById(id)
    }
}