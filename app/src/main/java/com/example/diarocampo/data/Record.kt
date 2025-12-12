package com.example.fieldlog.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "records")
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val photoPath: String? = null,
    val date: Long = System.currentTimeMillis(),
    val weather: String? = null
)