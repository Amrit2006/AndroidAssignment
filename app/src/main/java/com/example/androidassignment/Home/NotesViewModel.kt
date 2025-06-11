package com.example.androidassignment.Home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidassignment.Data.AppDatabase
import com.example.androidassignment.Data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.text.trim
import com.example.androidassignment.Home.Note

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoteRepository

    // For HomeScreen to observe the list of notes
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // For managing the state of the Add/Edit Note Popup/Screen
    private val _showAddEditNotePopup = MutableStateFlow(false)
    val showAddEditNotePopup: StateFlow<Boolean> = _showAddEditNotePopup.asStateFlow()

    private val _currentNoteTitle = MutableStateFlow("")
    val currentNoteTitle: StateFlow<String> = _currentNoteTitle.asStateFlow()

    private val _currentNoteDescription = MutableStateFlow("")
    val currentNoteDescription: StateFlow<String> = _currentNoteDescription.asStateFlow()

    // To know if we are editing an existing note
    private var _editingNoteId: Int? = null


    init {
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        viewModelScope.launch {
            repository.allNotes.collectLatest { notesList ->
                _notes.value = notesList
            }
        }
    }

    fun onAddNoteClicked() {
        _editingNoteId = null // Clear any previous editing state
        _currentNoteTitle.value = ""
        _currentNoteDescription.value = ""
        _showAddEditNotePopup.value = true
    }

    fun onEditNoteClicked(note: Note) {
        _editingNoteId = note.id
        _currentNoteTitle.value = note.title
        _currentNoteDescription.value = note.description
        _showAddEditNotePopup.value = true
    }

    fun onDismissPopup() {
        _showAddEditNotePopup.value = false
    }

    fun onTitleChange(newTitle: String) {
        _currentNoteTitle.value = newTitle
    }

    fun onDescriptionChange(newDescription: String) {
        _currentNoteDescription.value = newDescription
    }

    fun saveNote() {
        val title = _currentNoteTitle.value.trim()
        val description = _currentNoteDescription.value.trim()

        if (title.isEmpty() && description.isEmpty()) {
            // Optionally show a message that note can't be empty
            onDismissPopup() // Or just dismiss
            return
        }

        viewModelScope.launch {
            if (_editingNoteId != null) {
                // Update existing note
                val updatedNote = Note(id = _editingNoteId!!, title = title, description = description)
                repository.update(updatedNote)
            } else {
                // Add new note
                val newNote = Note(title = title, description = description)
                repository.insert(newNote)
            }
            onDismissPopup()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }
}