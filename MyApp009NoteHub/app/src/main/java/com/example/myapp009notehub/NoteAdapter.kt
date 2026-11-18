package com.example.myapp009notehub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp009notehub.data.Note
import com.example.myapp009notehub.databinding.ItemNoteBinding

class NoteAdapter(
    private val onEdit: (Note) -> Unit,
    private val onDelete: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) = with(binding) {
            textTitle.text = note.title
            textContent.text = note.content
            textCategory.text = root.context.getString(R.string.category_value, note.category)
            textTag.text = root.context.getString(R.string.tag_value, note.tag)
            textCategory.visibility = if (note.category.isBlank()) View.GONE else View.VISIBLE
            textTag.visibility = if (note.tag.isBlank()) View.GONE else View.VISIBLE
            root.setOnClickListener { onEdit(note) }
            btnEdit.setOnClickListener { onEdit(note) }
            btnDelete.setOnClickListener { onDelete(note) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean =
            oldItem == newItem
    }
}
