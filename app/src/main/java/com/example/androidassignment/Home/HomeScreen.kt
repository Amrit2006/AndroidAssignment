package com.example.androidassignment.Home

import android.app.Application
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    notesViewModel: NotesViewModel = viewModel(
        factory = NotesViewModelFactory(LocalContext.current.applicationContext as Application)
    ) // Provide Application context
) {
    val notes by notesViewModel.notes.collectAsState()
    val showPopup by notesViewModel.showAddEditNotePopup.collectAsState()
    val currentTitle by notesViewModel.currentNoteTitle.collectAsState()
    val currentDescription by notesViewModel.currentNoteDescription.collectAsState()

    Scaffold( // Using Scaffold for a more standard layout with a FAB
        topBar = {
            TopAppBar(title = {
                Text(
                    "Notes",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { notesViewModel.onAddNoteClicked() }) {
                Icon(Icons.Filled.Add, "Add Note")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
        ) {
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notes yet. Tap '+' to add one!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    itemsIndexed(notes, key = { index, note ->
                        // TEMPORARY DEBUGGING:
                        Log.d(
                            "HomeScreenDebug",
                            "Index: $index, Note object: $note, Class: ${note::class.java.name}"
                        )
                        // You might try to generate a key based on content if possible, but index is simpler here
                        index // Using index as key
                    }) { index, note -> // note here is still of the same type as before
                        NotesLayout(
                            note = note,
                            onNoteClick = { notesViewModel.onEditNoteClicked(note) },
                            onDeleteClick = { notesViewModel.deleteNote(note) },
                            modifier = Modifier.animateItemPlacement()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }



            if (showPopup) {
                AddEditNoteDialog(
                    title = currentTitle,
                    description = currentDescription,
                    onTitleChange = { notesViewModel.onTitleChange(it) },
                    onDescriptionChange = { notesViewModel.onDescriptionChange(it) },
                    onSave = { notesViewModel.saveNote() },
                    onDismiss = { notesViewModel.onDismissPopup() }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class) // For Modifier.animateItemPlacement()
@Composable
fun NotesLayout(
    note: Note,
    onNoteClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card( // Using Card for better elevation and appearance
        modifier = modifier
            .fillMaxWidth()
            .clickable { onNoteClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    note.title.ifEmpty { "Untitled" }, // Handle empty title
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    note.description,
                    fontSize = 16.sp,
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete Note",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddEditNoteDialog(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) { // Using Dialog instead of Popup
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (title.isEmpty() && description.isEmpty() && !LocalInspectionMode.current) "Add Note" else "Edit Note", // Rough check if editing
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp), // Make description field taller
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onSave) {
                        Text("Save")
                    }
                }
            }
        }
    }
}


class NotesViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotesViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // You might need to provide a mock ViewModel or handle previews differently
    // For simplicity, this might not render perfectly without a running ViewModel instance.
    MaterialTheme {
        HomeScreen()
    }
}

//@Preview(showBackground = true)
//@Composable
//fun NotesLayoutPreview() {
//    MaterialTheme {
//        NotesLayout(
//            note = Note(title = "Sample Title", description = "This is a sample note description that could be a bit long."),
//            onNoteClick = {},
//            onDeleteClick = {}
//        )
//    }
//}

@Preview(showBackground = true)
@Composable
fun AddEditNoteDialogPreview() {
    MaterialTheme {
        AddEditNoteDialog(
            title = "Test Title",
            description = "Test Description",
            onTitleChange = {},
            onDescriptionChange = {},
            onSave = {},
            onDismiss = {}
        )
    }
}