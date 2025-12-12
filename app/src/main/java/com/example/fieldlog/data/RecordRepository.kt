package com.example.fieldlog.data

import androidx.lifecycle.LiveData

class RecordRepository(private val recordDao: RecordDao) {
    
    fun getAllRecords(): LiveData<List<Record>> = recordDao.getAllRecords()
    
    suspend fun insertRecord(record: Record): Long {
        return recordDao.insert(record)
    }
    
    suspend fun updateRecord(record: Record) {
        recordDao.update(record)
    }
    
    suspend fun deleteRecord(record: Record) {
        recordDao.delete(record)
    }
    
    fun getRecord(id: Int): LiveData<Record?> {
        return recordDao.getRecordById(id)
    }
}

