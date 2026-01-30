package com.example.notes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.notes.viewmodel.NoteEditorState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.snapshotFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val saveFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun NoteEditorScreen(
    state: NoteEditorState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onDraftChange: (String, String) -> Unit,
    onAutoSave: () -> Unit
) {
    var title by rememberSaveable(state.id) { mutableStateOf(state.title) }
    var content by rememberSaveable(state.id) { mutableStateOf(state.content) }
    var readyToSave by remember(state.id) { mutableStateOf(false) }

    LaunchedEffect(state.id) {
        title = state.title
        content = state.content
        readyToSave = true
    }

    LaunchedEffect(Unit) {
        snapshotFlow { title to content }
            .filter { readyToSave }
            .debounce(800)
            .collectLatest { (newTitle, newContent) ->
                onDraftChange(newTitle, newContent)
                onAutoSave()
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = if (state.id == 0L) "Новая заметка" else "Редактирование") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (state.id != 0L) {
                        IconButton(onClick = onDelete) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    onDraftChange(title, content)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Заголовок") },
                textStyle = MaterialTheme.typography.titleLarge,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true
            )

            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    onDraftChange(title, content)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text(text = "Текст заметки") },
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions.Default,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (state.lastSavedAt == 0L) {
                    "Автосохранение включено"
                } else {
                    "Последнее сохранение: ${formatTimestamp(state.lastSavedAt)}"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val dateTime = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    return saveFormatter.format(dateTime)
}
