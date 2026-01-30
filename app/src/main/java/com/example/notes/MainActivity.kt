package com.example.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notes.data.NoteRepository
import com.example.notes.data.database.NotesDatabase
import com.example.notes.theme.NotesTheme
import com.example.notes.ui.screens.NoteEditorScreen
import com.example.notes.ui.screens.NotesListScreen
import com.example.notes.viewmodel.NotesViewModel
import com.example.notes.viewmodel.NotesViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: NotesViewModel by viewModels {
        val database = NotesDatabase.getInstance(this)
        val repository = NoteRepository(database.noteDao())
        NotesViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesTheme(darkTheme = isSystemInDarkTheme()) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    NotesNavHost(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun NotesNavHost(viewModel: NotesViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            val notes by viewModel.notesState.collectAsState()
            val query by viewModel.searchQueryState.collectAsState()
            val sortAscending by viewModel.sortAscendingState.collectAsState()

            NotesListScreen(
                notes = notes,
                query = query,
                sortAscending = sortAscending,
                onQueryChange = viewModel::updateSearchQuery,
                onToggleSort = viewModel::toggleSortOrder,
                onNoteClick = { noteId -> navController.navigate("editor/$noteId") },
                onAddNote = { navController.navigate("editor/0") }
            )
        }
        composable(
            route = "editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            val editorState by viewModel.editorState.collectAsState()

            // Загружаем текущую заметку при открытии экрана.
            LaunchedEffect(noteId) {
                viewModel.loadNote(noteId)
            }

            NoteEditorScreen(
                state = editorState,
                onBack = { navController.popBackStack() },
                onDelete = {
                    viewModel.deleteCurrentNote()
                    navController.popBackStack()
                },
                onDraftChange = viewModel::updateDraft,
                onAutoSave = viewModel::saveDraft
            )
        }
    }
}
