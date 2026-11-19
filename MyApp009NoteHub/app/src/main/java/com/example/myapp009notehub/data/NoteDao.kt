package com.example.myapp009notehub.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: Note)

    @Query("SELECT * FROM note_table ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note_table WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): Note?

    @Query("SELECT DISTINCT category FROM note_table WHERE TRIM(category) <> '' ORDER BY category COLLATE NOCASE")
    suspend fun getCategories(): List<String>

    @Query("SELECT DISTINCT tag FROM note_table WHERE TRIM(tag) <> '' ORDER BY tag COLLATE NOCASE")
    suspend fun getTags(): List<String>

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)
}
