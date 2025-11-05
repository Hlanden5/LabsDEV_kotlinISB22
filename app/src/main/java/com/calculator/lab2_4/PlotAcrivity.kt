package com.calculator.lab2_4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calculator.lab2_4.ui.theme.Lab24Theme
import kotlin.math.*

class PlotActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // читаем тёмную тему из SettingsRepo
            val dark by SettingsRepo.darkFlow(this@PlotActivity).collectAsState(initial = false)
            Lab24Theme(darkTheme = dark) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    PlotScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun PlotScreen() {
    var expr by remember { mutableStateOf("sin(x)") }
    val compileResult = remember(expr) { Calculator.compile(expr) }
    val TARGET_GRID_PX = 64f
    var scale by remember { mutableStateOf(TARGET_GRID_PX) }
    var cx by remember { mutableStateOf(0f) }
    var cy by remember { mutableStateOf(0f) }
    var lastTouch by remember { mutableStateOf<Offset?>(null) }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.merge(
        TextStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.85f))
    )

    val gridMinor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    val gridMajor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    val curveColor = MaterialTheme.colorScheme.primary
    val crosshairColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
    val tooltipBg = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    val tooltipFg = MaterialTheme.colorScheme.onSurface

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.title_plot)) }) }) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = expr,
                    onValueChange = { expr = it },
                    label = { Text(stringResource(R.string.label_function)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { cx = 0f; cy = 0f; scale = TARGET_GRID_PX }) {
                    Text(stringResource(R.string.reset))
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("${stringResource(R.string.scale_label)}: %.2f".format(scale)) }
                )
                AssistChip(
                    onClick = {},
                    label = { Text("${stringResource(R.string.center_label)}: (%.2f; %.2f)".format(cx, cy)) }
                )
            }

            if (compileResult.isFailure) {
                Text(
                    "${stringResource(R.string.error_parse)}: ${compileResult.exceptionOrNull()?.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { centroid, pan, zoom, _ ->
                                val pre = screenToWorld(
                                    centroid,
                                    size.width.toFloat(),
                                    size.height.toFloat(),
                                    cx,
                                    cy,
                                    scale
                                )
                                scale = (scale * zoom).coerceIn(8f, 2000f)
                                val post = screenToWorld(
                                    centroid,
                                    size.width.toFloat(),
                                    size.height.toFloat(),
                                    cx,
                                    cy,
                                    scale
                                )
                                cx += pre.x - post.x
                                cy += pre.y - post.y
                                cx -= pan.x / scale
                                cy += pan.y / scale
                                lastTouch = centroid
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    fun worldToScreenX(x: Float) = (x - cx) * scale + w / 2f
                    fun worldToScreenY(y: Float) = (cy - y) * scale + h / 2f
                    fun screenToWorldX(px: Float) = cx + (px - w / 2f) / scale
                    fun screenToWorldY(py: Float) = cy - (py - h / 2f) / scale

                    val minX = screenToWorldX(0f)
                    val maxX = screenToWorldX(w)
                    val minY = screenToWorldY(h)
                    val maxY = screenToWorldY(0f)

                    val step = niceStepFromApprox((TARGET_GRID_PX / scale).toDouble()).toFloat()
                    val majorEvery = 5

                    val startX = floor(minX / step).toInt()
                    val endX = ceil(maxX / step).toInt()
                    for (i in startX..endX) {
                        val x = i * step
                        val px = worldToScreenX(x)
                        val isMajor = i % majorEvery == 0
                        drawLine(
                            color = if (isMajor) gridMajor else gridMinor,
                            start = Offset(px, 0f),
                            end = Offset(px, h),
                            strokeWidth = if (isMajor) 1.4f else 1f
                        )
                        if (isMajor) {
                            val label = fmt(x)
                            val res = textMeasurer.measure(label, style = labelStyle)
                            val tx = (px - res.size.width / 2f).coerceIn(0f, w - res.size.width)
                            val ty = (h - res.size.height - 4f).coerceIn(0f, h - res.size.height)
                            drawText(textMeasurer, label, topLeft = Offset(tx, ty), style = labelStyle)
                        }
                    }

                    val startY = floor(minY / step).toInt()
                    val endY = ceil(maxY / step).toInt()
                    for (i in startY..endY) {
                        val y = i * step
                        val py = worldToScreenY(y)
                        val isMajor = i % majorEvery == 0
                        drawLine(
                            color = if (isMajor) gridMajor else gridMinor,
                            start = Offset(0f, py),
                            end = Offset(w, py),
                            strokeWidth = if (isMajor) 1.4f else 1f
                        )
                        if (isMajor) {
                            val label = fmt(y)
                            val res = textMeasurer.measure(label, style = labelStyle)
                            val tx = 4f.coerceIn(0f, w - res.size.width)
                            val ty = (py - res.size.height / 2f).coerceIn(0f, h - res.size.height)
                            drawText(textMeasurer, label, topLeft = Offset(tx, ty), style = labelStyle)
                        }
                    }

                    if (minX <= 0 && maxX >= 0) {
                        val px = worldToScreenX(0f)
                        drawLine(axisColor, Offset(px, 0f), Offset(px, h), strokeWidth = 2.4f)
                    }
                    if (minY <= 0 && maxY >= 0) {
                        val py = worldToScreenY(0f)
                        drawLine(axisColor, Offset(0f, py), Offset(w, py), strokeWidth = 2.4f)
                    }

                    compileResult.getOrNull()?.let { f ->
                        val samples = (w / 1.5f).roundToInt().coerceIn(200, 4000)
                        val dx = (maxX - minX) / samples
                        val path = Path()
                        var started = false
                        var x = minX
                        for (k in 0..samples) {
                            val y = runCatching { f(x.toDouble()).toFloat() }.getOrElse { Float.NaN }
                            if (y.isFinite()) {
                                val px = worldToScreenX(x)
                                val py = worldToScreenY(y)
                                if (!started) {
                                    path.moveTo(px, py)
                                    started = true
                                } else path.lineTo(px, py)
                            } else started = false
                            x += dx
                        }
                        drawPath(path, color = curveColor, style = Stroke(width = 3.5f))
                    }

                    lastTouch?.let { p ->
                        drawLine(crosshairColor, Offset(p.x, 0f), Offset(p.x, h), 1.2f)
                        drawLine(crosshairColor, Offset(0f, p.y), Offset(w, p.y), 1.2f)
                        val wx = screenToWorldX(p.x)
                        val wy = screenToWorldY(p.y)
                        val tooltip = "x=${fmt(wx)}\ny=${fmt(wy)}"
                        val res = textMeasurer.measure(tooltip, style = labelStyle)
                        val pad = 6f
                        val boxW = res.size.width + pad * 2f
                        val boxH = res.size.height + pad * 2f
                        val bx = (p.x + 10f).coerceIn(0f, w - boxW - 4f)
                        val by = (p.y + 10f).coerceIn(0f, h - boxH - 4f)
                        drawRoundRect(
                            color = tooltipBg,
                            topLeft = Offset(bx, by),
                            size = androidx.compose.ui.geometry.Size(boxW, boxH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )
                        drawText(
                            textMeasurer, tooltip,
                            topLeft = Offset(bx + pad, by + pad),
                            style = labelStyle.copy(color = tooltipFg)
                        )
                    }
                }
            }
        }
    }
}

private fun screenToWorld(p: Offset, w: Float, h: Float, cx: Float, cy: Float, scale: Float): Offset {
    val x = cx + (p.x - w / 2f) / scale
    val y = cy - (p.y - h / 2f) / scale
    return Offset(x, y)
}

private fun niceStepFromApprox(approx: Double): Double {
    val p = floor(log10(approx))
    val base = 10.0.pow(p)
    val c = listOf(1.0, 2.0, 2.5, 5.0, 10.0)
    var best = 1.0
    var diff = Double.MAX_VALUE
    for (v in c) {
        val valStep = v * base
        val d = abs(approx - valStep)
        if (d < diff) { diff = d; best = valStep }
    }
    return best
}

private fun fmt(v: Float): String {
    if (abs(v) < 1e-6f) return "0"
    val s = "%,.5f".format(v).replace(',', '.')
    return s.trimEnd('0').trimEnd('.')
}
