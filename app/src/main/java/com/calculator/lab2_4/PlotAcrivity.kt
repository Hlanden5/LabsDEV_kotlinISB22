package com.calculator.lab2_4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.lab2_4.ui.theme.Lab24Theme
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import androidx.appcompat.app.AppCompatActivity
class PlotActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dark by SettingsRepo.darkFlow(this@PlotActivity).collectAsState(initial = false)
            Lab24Theme(darkTheme = dark) { PlotScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun PlotScreen() {
    val curveColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    var expr by remember { mutableStateOf("sin(x)") }
    var xMinText by remember { mutableStateOf("-10") }
    var xMaxText by remember { mutableStateOf("10") }

    val compileResult = remember(expr) { Calculator.compile(expr) }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.merge(
        TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f))
    )
    val density = LocalDensity.current
    val tickLen = with(density) { 4.dp.toPx() }
    val pad = with(density) { 2.dp.toPx() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_plot)) }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = expr,
                onValueChange = { expr = it },
                label = { Text("f(x) =") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = xMinText,
                    onValueChange = { xMinText = it },
                    label = { Text("Xmin") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = xMaxText,
                    onValueChange = { xMaxText = it },
                    label = { Text("Xmax") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            if (compileResult.isFailure) {
                Text(
                    "Ошибка разбора: ${compileResult.exceptionOrNull()?.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Canvas(Modifier.fillMaxWidth().height(280.dp)) {
                val w = size.width
                val h = size.height

                val xMin = xMinText.toDoubleOrNull() ?: -10.0
                val xMax = xMaxText.toDoubleOrNull() ?: 10.0
                val xSpan = (xMax - xMin).takeIf { it != 0.0 } ?: 1.0

                compileResult.getOrNull()?.let { f ->
                    val n = w.toInt().coerceIn(200, 1600)
                    val xs = (0..n).map { i -> xMin + xSpan * i / n }
                    val ys = xs.map { x -> runCatching { f(x) }.getOrElse { Double.NaN } }
                    val valid = ys.filter { it.isFinite() }
                    if (valid.isEmpty()) return@Canvas


                    var yMin = valid.minOrNull() ?: -1.0
                    var yMax = valid.maxOrNull() ?:  1.0
                    var ySpan = yMax - yMin
                    if (ySpan < 1e-9) {
                        val c = yMin
                        yMin = c - 1.0
                        yMax = c + 1.0
                        ySpan = 2.0
                    }

                    val s = minOf(w / xSpan.toFloat(), h / ySpan.toFloat())
                    val xC = ((xMin + xMax) / 2.0).toFloat()
                    val yC = ((yMin + yMax) / 2.0).toFloat()
                    fun mapX(x: Double) = ((x.toFloat() - xC) * s + w / 2f)
                    fun mapY(y: Double) = (h / 2f - (y.toFloat() - yC) * s)

                    var xAxisY = Float.NaN
                    var yAxisX = Float.NaN
                    if (yMin < 0.0 && yMax > 0.0) {
                        xAxisY = mapY(0.0)
                        drawLine(axisColor, Offset(0f, xAxisY), Offset(w, xAxisY), strokeWidth = 2f)
                    }
                    if (xMin < 0.0 && xMax > 0.0) {
                        yAxisX = mapX(0.0)
                        drawLine(axisColor, Offset(yAxisX, 0f), Offset(yAxisX, h), strokeWidth = 2f)
                    }

                    fun niceStep(range: Double, maxTicks: Int): Double {
                        val raw = range / maxTicks
                        val p = floor(log10(raw)).toInt()
                        val base = 10.0.pow(p)
                        return when {
                            raw <= 1 * base -> 1 * base
                            raw <= 2 * base -> 2 * base
                            raw <= 5 * base -> 5 * base
                            else -> 10 * base
                        }
                    }
                    fun fmt(v: Double): String {
                        if (abs(v) < 1e-10) return "0"
                        val s = "%,.6f".format(v).replace(',', '.')
                        return s.trimEnd('0').trimEnd('.')
                    }

                    val xStep = niceStep(xSpan, 6)
                    var tx = ceil(xMin / xStep) * xStep
                    val xLabelY = if (!xAxisY.isNaN()) xAxisY + tickLen + pad else h - tickLen - pad
                    while (tx <= xMax + 1e-9) {
                        val px = mapX(tx)
                        val tickTop = if (!xAxisY.isNaN()) xAxisY - tickLen else h - tickLen * 2
                        val tickBot = if (!xAxisY.isNaN()) xAxisY + tickLen else h
                        drawLine(axisColor.copy(alpha = 0.7f), Offset(px, tickTop), Offset(px, tickBot), strokeWidth = 1f)

                        val text = fmt(tx)
                        val res = textMeasurer.measure(text = text, style = labelStyle)
                        val tl = Offset(px - res.size.width / 2f, xLabelY)
                        drawText(textMeasurer, text = text, topLeft = tl, style = labelStyle)
                        tx += xStep
                    }

                    val yStep = niceStep(ySpan, 6)
                    var ty = ceil(yMin / yStep) * yStep
                    val yLabelX = if (!yAxisX.isNaN()) yAxisX + tickLen + pad else tickLen + pad
                    while (ty <= yMax + 1e-9) {
                        val py = mapY(ty)
                        val tickLeft = if (!yAxisX.isNaN()) yAxisX - tickLen else 0f
                        val tickRight = if (!yAxisX.isNaN()) yAxisX + tickLen else tickLen * 2
                        drawLine(axisColor.copy(alpha = 0.7f), Offset(tickLeft, py), Offset(tickRight, py), strokeWidth = 1f)

                        val text = fmt(ty)
                        val res = textMeasurer.measure(text = text, style = labelStyle)
                        val tl = Offset(yLabelX, py - res.size.height / 2f)
                        drawText(textMeasurer, text = text, topLeft = tl, style = labelStyle)
                        ty += yStep
                    }

                    val path = Path()
                    xs.indices.forEach { i ->
                        val y = ys[i]
                        if (!y.isFinite()) return@forEach
                        val px = mapX(xs[i])
                        val py = mapY(y)
                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }
                    drawPath(path, color = curveColor, style = Stroke(width = 4f))
                }
            }
        }
    }
}
