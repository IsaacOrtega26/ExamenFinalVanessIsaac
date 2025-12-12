package com.example.fieldlog.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fieldlog.data.Record
import com.example.fieldlog.data.RecordRepository
import kotlinx.coroutines.launch

class RecordViewModel(private val repository: RecordRepository) : ViewModel() {
    
    val records: LiveData<List<Record>> = repository.getAllRecords()
    
    fun insertRecord(record: Record) {
        viewModelScope.launch {
            repository.insertRecord(record)
        }
    }
    
    fun deleteRecord(record: Record) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }
    
    fun updateRecord(record: Record) {
        viewModelScope.launch {
            repository.updateRecord(record)
        }
    }
    
    fun getRecord(id: Int): LiveData<Record?> {
        return repository.getRecord(id)
    }
}

