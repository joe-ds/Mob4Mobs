package com.example.mob4mobs

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.IOException

class NotesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Utils().setupChallengeFiles(this)

        val rawNoteName = intent?.data?.getQueryParameter("name")
            ?: intent.getStringExtra("note_name")
            ?: "UnknownNote"

        setContent {
            MaterialTheme {
                NoteViewerScreen(rawNoteName)
            }
        }
    }
}

@Composable
fun NoteViewerScreen(noteName: String) {
    val context = LocalContext.current

    var password by remember { mutableStateOf("") }
    var fileContent by remember { mutableStateOf<String?>(null) }
    var decryptedContent by remember { mutableStateOf("") }

    LaunchedEffect(noteName) {
        val content = loadNoteFromRealFilesystem(context, "notes/$noteName")
        if (content != null) {
            fileContent = content
        } else {
            Toast.makeText(context, "Note not found", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(fileContent, password) {
        decryptedContent = xorWithKey(fileContent ?: "", password)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Viewing note: $noteName", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter password to decrypt (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (fileContent != null) {
            Text(
                text = decryptedContent,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text("No note loaded.", color = MaterialTheme.colorScheme.error)
        }
    }
}

fun loadNoteFromRealFilesystem(context: Context, userInput: String): String? {
    val targetFile = File(context.filesDir, userInput)

    return try {
        targetFile.readText()
    } catch (e: IOException) {
        null
    }
}

fun xorWithKey(data: String, key: String): String {
    if (key.isEmpty()) return data
    return data.mapIndexed { i, c ->
        c.code.xor(key[i % key.length].code).toChar()
    }.joinToString("")
}