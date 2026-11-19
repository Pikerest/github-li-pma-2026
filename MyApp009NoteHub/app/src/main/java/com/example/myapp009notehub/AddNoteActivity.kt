package com.example.myapp009notehub

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapp009notehub.data.Note
import com.example.myapp009notehub.data.NoteHubDatabaseInstance
import com.example.myapp009notehub.databinding.ActivityAddNoteBinding
import kotlinx.coroutines.launch

class AddNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private val noteDao by lazy { NoteHubDatabaseInstance.getDatabase(this).noteDao() }
    private var existingNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0)
        binding.textFormTitle.setText(
            if (noteId == 0) R.string.add_note else R.string.edit_note
        )

        lifecycleScope.launch {
            binding.editTextCategory.setAdapter(createSuggestionsAdapter(noteDao.getCategories()))
            binding.editTextTag.setAdapter(createSuggestionsAdapter(noteDao.getTags()))

            if (noteId != 0) {
                existingNote = noteDao.getNoteById(noteId)
                existingNote?.let(::showNote)
            }
        }

        binding.btnSaveNote.setOnClickListener { saveNote() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun showNote(note: Note) {
        binding.editTextTitle.setText(note.title)
        binding.editTextContent.setText(note.content)
        binding.editTextCategory.setText(note.category, false)
        binding.editTextTag.setText(note.tag, false)
    }

    private fun createSuggestionsAdapter(items: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)
    }

    private fun saveNote() {
        val title = binding.editTextTitle.text?.toString()?.trim().orEmpty()
        val content = binding.editTextContent.text?.toString()?.trim().orEmpty()
        val category = binding.editTextCategory.text?.toString()?.trim().orEmpty()
        val tag = binding.editTextTag.text?.toString()?.trim().orEmpty()

        binding.inputTitle.error = if (title.isBlank()) getString(R.string.required_field) else null
        binding.inputContent.error = if (content.isBlank()) getString(R.string.required_field) else null
        if (title.isBlank() || content.isBlank()) return

        val note = existingNote?.copy(
            title = title,
            content = content,
            category = category,
            tag = tag
        ) ?: Note(
            title = title,
            content = content,
            category = category,
            tag = tag
        )

        binding.btnSaveNote.isEnabled = false
        lifecycleScope.launch {
            if (existingNote == null) noteDao.insert(note) else noteDao.update(note)
            finish()
        }
    }

    companion object {
        const val EXTRA_NOTE_ID = "note_id"
    }
}
