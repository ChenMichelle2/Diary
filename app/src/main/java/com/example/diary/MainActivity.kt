package com.example.diary

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

// Create DataStore
val Context.dataStore by preferencesDataStore(name = "user_preferences")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiaryApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Date format
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // States
    var selectedDate by remember { mutableStateOf(Date()) }
    var diaryText by remember { mutableStateOf("") }
    var fontSize by remember { mutableStateOf(16) } // default font size

    LaunchedEffect(Unit) {
        try {
            val prefs = context.dataStore.data.first()
            fontSize = prefs[intPreferencesKey("font_size")] ?: 16
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        LaunchedEffect(Unit) {
            val dialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                    selectedDate = cal.time
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
            dialog.setOnDismissListener { showDatePicker = false }
            dialog.show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Personal Diary") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Pick Date")
                }
                Text(
                    text = "Date: ${dateFormat.format(selectedDate)}",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text field for diary entry
            OutlinedTextField(
                value = diaryText,
                onValueChange = { diaryText = it },
                label = { Text("Write your diary entry") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = fontSize.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Font size control
            Text("Font Size: $fontSize", modifier = Modifier.padding(bottom = 8.dp))
            Slider(
                value = fontSize.toFloat(),
                onValueChange = { fontSize = it.toInt() },
                valueRange = 12f..30f,
                steps = 10
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    scope.launch {
                        // Create the filename using the date
                        val fileName = "diary_${dateFormat.format(selectedDate)}.txt"
                        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { stream ->
                            OutputStreamWriter(stream).use { writer ->
                                writer.write(diaryText)
                            }
                        }
                        // Save the font size preference
                        context.dataStore.edit { settings ->
                            settings[intPreferencesKey("font_size")] = fontSize
                        }
                        Toast.makeText(context, "Diary entry saved!", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save Diary")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Entry")
            }
        }
    }
}
