package com.calculator.lab2_4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calculator.lab2_4.ui.theme.Lab24Theme
import kotlin.math.sin

class PlotActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Lab24Theme { PlotScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotScreen() {
    val curveColor = MaterialTheme.colorScheme.primary

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
            Text("Заглушка графика (синус)")

            Canvas(Modifier.fillMaxWidth().height(220.dp)) {
                val w = size.width
                val h = size.height
                val path = Path()

                for (x in 0..200) {
                    val t = x / 200f
                    val px = t * w
                    val py = h / 2f + kotlin.math.sin(t * 4.0 * kotlin.math.PI).toFloat() * h / 3f
                    if (x == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }

                drawPath(path, color = curveColor, style = Stroke(width = 4f))
            }
        }
    }
}
