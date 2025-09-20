package com.IS_B22_Dukhov.lab1_variant_10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ClampScreen()
            }
        }
    }
}

@Composable
fun ClampScreen() {
    val generatedList = remember { ListProcessor.generateList(size = 12) }

    var zInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        Text("Сгенерированный список:", style = MaterialTheme.typography.titleMedium)
        Text(generatedList.toString(), modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = zInput,
            onValueChange = { zInput = it },
            label = { Text("Введите Z (целое)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            val z = zInput.toIntOrNull()
            resultText = if (z == null) {
                "Ошибка: введите корректное целое число Z."
            } else {
                val res = ListProcessor.clampAbove(generatedList, z)
                buildString {
                    appendLine("Z = $z")
                    appendLine("Исходный:  ${res.original}")
                    appendLine("Результат:  ${res.replaced}")
                    appendLine("Заменено элементов: ${res.replacements}")
                }
            }
        }) {
            Text("Выполнить")
        }

        Spacer(Modifier.height(12.dp))

        // Поле только для чтения под результат
        OutlinedTextField(
            value = resultText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Результат") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
        )
    }
}
