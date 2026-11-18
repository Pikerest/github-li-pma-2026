package com.example.myapp009notehub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp009notehub.data.Note
import com.example.myapp009notehub.data.NoteHubDatabaseInstance
import com.example.myapp009notehub.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var noteAdapter: NoteAdapter
    private val noteDao by lazy { NoteHubDatabaseInstance.getDatabase(this).noteDao() }
    private var notesJob: Job? = null
    private var allNotes: List<Note> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteAdapter = NoteAdapter(
            onEdit = { note -> openNoteForm(note.id) },
            onDelete = { note -> confirmDelete(note) }
        )

        binding.recyclerNotes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = noteAdapter
        }

        val filterListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        binding.spinnerCategory.onItemSelectedListener = filterListener
        binding.spinnerTag.onItemSelectedListener = filterListener

        binding.fabAddNote.setOnClickListener { openNoteForm() }
    }

    override fun onStart() {
        super.onStart()
        notesJob = lifecycleScope.launch {
            noteDao.getAllNotes().collectLatest { notes ->
                allNotes = notes
                updateFilterOptions()
                applyFilters()
            }
        }
    }

    override fun onStop() {
        notesJob?.cancel()
        notesJob = null
        super.onStop()
    }

    private fun openNoteForm(noteId: Int = 0) {
        startActivity(
            Intent(this, AddNoteActivity::class.java)
                .putExtra(AddNoteActivity.EXTRA_NOTE_ID, noteId)
        )
    }

    private fun confirmDelete(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_note_title)
            .setMessage(getString(R.string.delete_note_message, note.title))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch { noteDao.delete(note) }
            }
            .show()
    }

    private fun updateFilterOptions() {
        val selectedCategory = binding.spinnerCategory.selectedItem?.toString()
        val selectedTag = binding.spinnerTag.selectedItem?.toString()

        val categories = listOf(getString(R.string.all_categories)) +
            allNotes.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
        val tags = listOf(getString(R.string.all_tags)) +
            allNotes.map { it.tag }.filter { it.isNotBlank() }.distinct().sorted()

        binding.spinnerCategory.adapter = createSpinnerAdapter(categories)
        binding.spinnerTag.adapter = createSpinnerAdapter(tags)
        binding.spinnerCategory.setSelection(categories.indexOf(selectedCategory).coerceAtLeast(0))
        binding.spinnerTag.setSelection(tags.indexOf(selectedTag).coerceAtLeast(0))
    }

    private fun createSpinnerAdapter(items: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(this, android.R.layout.simple_spinner_item, items).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun applyFilters() {
        val allCategories = getString(R.string.all_categories)
        val allTags = getString(R.string.all_tags)
        val selectedCategory = binding.spinnerCategory.selectedItem?.toString() ?: allCategories
        val selectedTag = binding.spinnerTag.selectedItem?.toString() ?: allTags

        val filteredNotes = allNotes.filter { note ->
            (selectedCategory == allCategories || note.category == selectedCategory) &&
                (selectedTag == allTags || note.tag == selectedTag)
        }

        noteAdapter.submitList(filteredNotes)
        binding.textEmpty.visibility = if (filteredNotes.isEmpty()) View.VISIBLE else View.GONE
    }
}
