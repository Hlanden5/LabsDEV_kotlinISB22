package com.calculator.lab2_4

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.calculator.lab2_4.ui.theme.Lab24Theme
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.collectAsState

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dark by SettingsRepo.darkFlow(this@SettingsActivity).collectAsState(initial = false)
            Lab24Theme(darkTheme = dark) { SettingsScreen() }
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

val Context.dataStore by preferencesDataStore("prefs")

object SettingsRepo {
    private val DARK = booleanPreferencesKey("dark")
    private val LANG = stringPreferencesKey("lang")

    fun darkFlow(ctx: Context) = ctx.dataStore.data.map { it[DARK] ?: false }
    fun langFlow(ctx: Context) = ctx.dataStore.data.map { it[LANG] ?: "system" }

    suspend fun setDark(ctx: Context, v: Boolean) = ctx.dataStore.edit { it[DARK] = v }
    suspend fun setLang(ctx: Context, v: String)  = ctx.dataStore.edit { it[LANG] = v }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val dark by SettingsRepo.darkFlow(ctx).collectAsState(initial = false)
    val lang by SettingsRepo.langFlow(ctx).collectAsState(initial = "system")

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
            Text(stringResource(R.string.section_appearance), style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.dark_theme))
                Switch(
                    checked = dark,
                    onCheckedChange = { v -> scope.launch { SettingsRepo.setDark(ctx, v) } }
                )
            }

            Text(stringResource(R.string.section_language), style = MaterialTheme.typography.titleLarge)

            LangRow("system", stringResource(R.string.lang_system), lang) {
                scope.launch {
                    SettingsRepo.setLang(ctx, "system")
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                    ctx.findActivity()?.recreate()
                }
            }
            LangRow("en", stringResource(R.string.lang_en), lang) {
                scope.launch {
                    SettingsRepo.setLang(ctx, "en")
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                    ctx.findActivity()?.recreate()
                }
            }
            LangRow("ru", stringResource(R.string.lang_ru), lang) {
                scope.launch {
                    SettingsRepo.setLang(ctx, "ru")
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ru"))
                    ctx.findActivity()?.recreate()
                }
            }

            Text(stringResource(R.string.note_apply))
        }
    }
}

@Composable
private fun LangRow(value: String, label: String, selected: String, onSelect: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        RadioButton(selected = selected == value, onClick = onSelect)
    }
}
