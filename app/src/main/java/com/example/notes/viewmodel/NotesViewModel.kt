package com.example.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes.data.NoteRepository
import com.example.notes.data.entity.NoteEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val sortAscending = MutableStateFlow(false)
    private var noteJob: Job? = null

    val searchQueryState: StateFlow<String> = searchQuery.asStateFlow()
    val sortAscendingState: StateFlow<Boolean> = sortAscending.asStateFlow()

    val notesState: StateFlow<List<NoteEntity>> = combine(
        searchQuery,
        sortAscending
    ) { query, ascending ->
        query to ascending
    }.flatMapLatest { (query, ascending) ->
        repository.observeNotes(query).map { notes ->
            if (ascending) {
                notes.sortedBy { it.updatedAt }
            } else {
                notes
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editorState = MutableStateFlow(NoteEditorState())
    val editorState: StateFlow<NoteEditorState> = _editorState.asStateFlow()

    fun updateSearchQuery(value: String) {
        searchQuery.value = value
    }

    fun toggleSortOrder() {
        sortAscending.value = !sortAscending.value
    }

    fun loadNote(noteId: Long?) {
        noteJob?.cancel()
        if (noteId == null || noteId == 0L) {
            _editorState.value = NoteEditorState()
            return
        }
        noteJob = viewModelScope.launch {
            repository.observeNote(noteId).collect { note ->
                note?.let {
                    _editorState.value = NoteEditorState(
                        id = it.id,
                        title = it.title,
                        content = it.content,
                        lastSavedAt = it.updatedAt
                    )
                }
            }
        }
    }

    fun updateDraft(title: String, content: String) {
        _editorState.value = _editorState.value.copy(
            title = title,
            content = content
        )
    }

    fun saveDraft() {
        val draft = _editorState.value
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val note = NoteEntity(
                id = draft.id,
                title = draft.title.trim().ifBlank { "Без названия" },
                content = draft.content.trim(),
                updatedAt = now
            )
            val id = repository.upsert(note)
            _editorState.value = draft.copy(id = if (draft.id == 0L) id else draft.id, lastSavedAt = now)
        }
    }

    fun deleteCurrentNote() {
        val draft = _editorState.value
        if (draft.id == 0L) return
        viewModelScope.launch {
            repository.delete(
                NoteEntity(
                    id = draft.id,
                    title = draft.title,
                    content = draft.content,
                    updatedAt = draft.lastSavedAt
                )
            )
        }
    }
}

class NotesViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class NoteEditorState(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val lastSavedAt: Long = 0
)
