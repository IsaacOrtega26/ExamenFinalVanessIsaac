package com.example.fieldlog.data

import androidx.room.*

@Dao
interface RecordDao {
    
    @Insert
    suspend fun insert(record: Record): Long
    
    @Update
    suspend fun update(record: Record)
    
    @Delete
    suspend fun delete(record: Record)
    
    @Query("SELECT * FROM records ORDER BY date DESC")
    suspend fun getAllRecords(): List<Record>
    
    @Query("SELECT * FROM records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Int): Record?
    
    @Query("DELETE FROM records WHERE id = :recordId")
    suspend fun deleteById(recordId: Int)
}