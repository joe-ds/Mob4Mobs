package com.example.mob4mobs

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MyDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        // Create table with two columns: id (int) and name (text)
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL
            );
        """.trimIndent()
        db.execSQL(createTable)

        val insertDefault = """
        INSERT INTO $TABLE_NAME ($COLUMN_NAME) VALUES 
        ('test'),
        ('password'),
        ('ohyoufoundme'),
        ('stretchgoal');
    """.trimIndent()
        db.execSQL(insertDefault)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "secret_notes.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "notes"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "note"
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: MyDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Utils().setupChallengeFiles(this)

        dbHelper = MyDatabaseHelper(this)

        setContent {
            MaterialTheme {
                SearchScreen(dbHelper)
            }
        }
    }
}

@Composable
fun SearchScreen(
    dbHelper: MyDatabaseHelper
) {
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Mob4Mobs Secure Notes",
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("What's your note called?") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val note = getMatchingNote(dbHelper, query)
                if (note != null) {
                    val intent = Intent(context, NotesActivity::class.java).apply {
                        putExtra("note_name", note)
                    }
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "No such note found!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Search")
        }
    }
}

fun getMatchingNote(dbHelper: MyDatabaseHelper, query: String): String? {
    val db = dbHelper.readableDatabase
    val rawQuery = "SELECT * FROM ${MyDatabaseHelper.TABLE_NAME} WHERE ${MyDatabaseHelper.COLUMN_NAME} = '$query'"
    val cursor = db.rawQuery(rawQuery, null)
    val result = if (cursor.moveToFirst()) {
        cursor.getString(cursor.getColumnIndexOrThrow(MyDatabaseHelper.COLUMN_NAME))
    } else null
    cursor.close()
    return result
}