package com.calculator.lab2_4

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.calculator.lab2_4.ui.theme.Lab24Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Lab24Theme { CalculatorScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen() {
    val ctx = LocalContext.current
    var expression by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_calculator)) },
                actions = {
                    IconButton(onClick = { ctx.startActivity(Intent(ctx, PlotActivity::class.java)) }) {
                        val cd = stringResource(R.string.action_plots)
                        ChartGlyph(
                            modifier = Modifier
                                .size(24.dp)
                                .semantics { contentDescription = cd }
                        )
                    }
                    IconButton(onClick = { ctx.startActivity(Intent(ctx, SettingsActivity::class.java)) }) {
                        val cd = stringResource(R.string.action_settings)
                        GearGlyph(
                            modifier = Modifier
                                .size(24.dp)
                                .semantics { contentDescription = cd }
                        )
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (expression.isBlank()) stringResource(R.string.hint_expression) else expression,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            )
            val rows = listOf(
                listOf("C", "DEL", "(", ")"),
                listOf("7", "8", "9", "/"),
                listOf("4", "5", "6", "*"),
                listOf("1", "2", "3", "-"),
                listOf("0", stringResource(R.string.btn_dot), "=", "+")
            )

            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { label ->
                            ElevatedButton(
                                onClick = {
                                    when (label) {
                                        "C" -> expression = ""
                                        "DEL" -> if (expression.isNotEmpty()) expression = expression.dropLast(1)
                                        "=" -> {}
                                        else -> expression += label
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChartGlyph(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val c = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.08f)
        drawLine(c, Offset(w*0.1f, h*0.85f), Offset(w*0.9f, h*0.85f), strokeWidth = stroke.width)
        drawLine(c, Offset(w*0.1f, h*0.85f), Offset(w*0.1f, h*0.15f), strokeWidth = stroke.width)
        val p = Path().apply {
            moveTo(w*0.12f, h*0.8f)
            lineTo(w*0.35f, h*0.55f)
            lineTo(w*0.55f, h*0.68f)
            lineTo(w*0.78f, h*0.3f)
        }
        drawPath(p, c, style = stroke)
    }
}

@Composable
fun GearGlyph(modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    val c = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val r = minOf(w, h) * 0.4f
        val center = Offset(w/2f, h/2f)
        val stroke = Stroke(width = r * 0.18f)
        drawCircle(color = c, radius = r, center = center, style = stroke)
        drawCircle(color = c, radius = r*0.3f, center = center, style = Stroke(width = r*0.12f))
        repeat(6) { i ->
            val angle = Math.toRadians((i * 60.0)).toFloat()
            val dx = kotlin.math.cos(angle)
            val dy = kotlin.math.sin(angle)
            val start = Offset(center.x + dx * (r * 0.85f), center.y + dy * (r * 0.85f))
            val end   = Offset(center.x + dx * (r * 1.15f), center.y + dy * (r * 1.15f))
            drawLine(c, start, end, strokeWidth = stroke.width * 0.6f)
        }
    }
}
