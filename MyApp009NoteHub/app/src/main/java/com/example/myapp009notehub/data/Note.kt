package com.example.myapp009notehub.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "note_table")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String = "",
    val tag: String = ""
)
