package com.example.fieldlog.data

import androidx.lifecycle.LiveData
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
    fun getAllRecords(): LiveData<List<Record>>
    
    @Query("SELECT * FROM records WHERE id = :recordId")
    fun getRecordById(recordId: Int): LiveData<Record?>
    
    @Query("DELETE FROM records WHERE id = :recordId")
    suspend fun deleteById(recordId: Int)
}

