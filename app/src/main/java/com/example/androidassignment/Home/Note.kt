package com.example.androidassignment.Home

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes_table") // If this is your Room entity
data class Note(
    @PrimaryKey(autoGenerate = true) // If this is your Room entity
    val id: Int = 0,             // <<< THIS IS THE CRITICAL LINE
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)