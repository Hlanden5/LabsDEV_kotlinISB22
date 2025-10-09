package com.calculator.lab2_4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calculator.lab2_4.ui.theme.Lab24Theme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Lab24Theme { SettingsScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var darkTheme by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("system") }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_settings)) }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dark theme")
                Switch(checked = darkTheme, onCheckedChange = { darkTheme = it })
            }
            Text("Language", style = MaterialTheme.typography.titleLarge)
            LangRow("system", "System default", language) { language = it }
            LangRow("en", "English", language) { language = it }
            LangRow("ru", "Русский", language) { language = it }
            Text("Переключатели пока декоративные (UI). Реальную смену применим позже.")
        }
    }
}

@Composable
private fun LangRow(value: String, label: String, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        RadioButton(selected = selected == value, onClick = { onSelect(value) })
    }
}
