package com.example.notes.data

import com.example.notes.data.dao.NoteDao
import com.example.notes.data.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    fun observeNotes(query: String): Flow<List<NoteEntity>> {
        return if (query.isBlank()) {
            noteDao.observeNotesByDateDesc()
        } else {
            noteDao.observeNotesByQuery(query.trim())
        }
    }

    fun observeNote(id: Long): Flow<NoteEntity?> = noteDao.observeNoteById(id)

    suspend fun upsert(note: NoteEntity): Long = noteDao.upsert(note)

    suspend fun update(note: NoteEntity) = noteDao.update(note)

    suspend fun delete(note: NoteEntity) = noteDao.delete(note)
}
