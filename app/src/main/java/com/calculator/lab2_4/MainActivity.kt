package com.calculator.lab2_4

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.geometry.CornerRadius

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val dark by SettingsRepo.darkFlow(this@MainActivity).collectAsState(initial = false)
            Lab24Theme(darkTheme = dark) {
                CalculatorScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen() {
    val ctx = LocalContext.current
    var expression by rememberSaveableString("")

    val dot = runCatching { stringResource(R.string.btn_dot) }.getOrElse { "." }

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
                listOf("0", dot, "=", "+")
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
                                        "=" -> {
                                            val fixed = autoCloseParens(expression)
                                            val (ok, value, err) = Calculator.eval(fixed)
                                            expression = if (ok) value else "ERR: $err"
                                        }
                                        "(" -> expression += "("
                                        ")" -> expression += ")"
                                        dot -> if (canInsertDot(expression)) expression += "."
                                        else -> expression += label
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                val fns = listOf("sin","cos","tan","sqrt","abs","ln","^")

                fns.chunked(4).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { fn ->
                            OutlinedButton(
                                onClick = { expression += if (fn == "^") "^" else "$fn(" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(fn, maxLines = 1, softWrap = false)
                            }
                        }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
@Composable
private fun rememberSaveableString(init: String): MutableState<String> =
    rememberSaveable { mutableStateOf(init) }

private fun autoCloseParens(s: String): String {
    val opens = s.count { it == '(' }
    val closes = s.count { it == ')' }
    return if (opens > closes) s + ")".repeat(opens - closes) else s
}

private fun canInsertDot(expr: String): Boolean {
    var i = expr.length - 1
    while (i >= 0 && (expr[i].isDigit() || expr[i] == '.')) i--
    val lastToken = expr.substring(i + 1)
    return !lastToken.contains('.')
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
fun GearGlyph(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    teeth: Int = 8,
    toothHeightRatio: Float = 0.22f,
    toothWidthRatio: Float = 0.65f,
    innerHoleRatio: Float = 0.38f
) {
    val c = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)

        val rBase = minOf(w, h) * 0.42f
        val toothH = rBase * toothHeightRatio
        val rRim = rBase
        val rInner = (rRim * innerHoleRatio).coerceAtLeast(2f)


        val body = Path().apply {
            fillType = PathFillType.EvenOdd
            addOval(Rect(center = center, radius = rRim))
            addOval(Rect(center = center, radius = rInner))
        }
        drawPath(body, color = c)


        val circumference = (2 * Math.PI * rRim).toFloat()
        val pitch = circumference / teeth
        val toothW = (pitch * toothWidthRatio).coerceAtLeast(2f)

        repeat(teeth) { i ->
            val angleDeg = i * (360f / teeth)

            withTransform({
                rotate(degrees = angleDeg, pivot = center)
            }) {
                val left   = center.x - toothW / 2f
                val right  = center.x + toothW / 2f
                val top    = center.y - rRim - toothH
                val bottom = center.y - rRim

                drawRoundRect(
                    color = c,
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(toothW, toothH),
                    cornerRadius = CornerRadius(x = toothW * 0.15f, y = toothW * 0.15f)
                )
            }
        }

        val accent = c.copy(alpha = 0.75f)
        drawCircle(color = accent, radius = rRim, center = center, style = Stroke(width = rRim * 0.06f))
        drawCircle(color = accent, radius = rInner, center = center, style = Stroke(width = rRim * 0.04f))
    }
}


private fun <T> ArrayDeque<T>.push(v: T) = addLast(v)
private fun <T> ArrayDeque<T>.pop(): T = removeLast()
private fun <T> ArrayDeque<T>.peek(): T = last()



object Calculator {
    private val ops = setOf('+','-','*','/','^')
    private val prec = mapOf('+' to 1, '-' to 1, '*' to 2, '/' to 2, '^' to 3)
    private fun isOp(c: Char) = c in ops
    private fun pr(c: Char) = prec[c] ?: -1

    fun eval(s: String): Triple<Boolean, String, String?> = runCatching {
        val f = compile(s).getOrThrow()
        val v = f(Double.NaN)
        Triple(true, format(v), null)
    }.getOrElse { Triple(false, "", it.message) }

    fun compile(expr: String): Result<(Double)->Double> = runCatching {
        val rpn = toRPN(expr.replace(" ", ""))
        return@runCatching { x: Double -> evalRPN(rpn, x) }
    }

    private val fnNames = setOf("sin","cos","tan","sqrt","abs","ln")
    private fun toRPN(src: String): List<String> {
        val s = src.replace(" ", "")
        val out = mutableListOf<String>()
        val st = ArrayDeque<String>()
        var i = 0

        fun prec(op: String) = when (op) {
            "+","-" -> 1
            "*","/" -> 2
            "^"     -> 3
            else    -> -1
        }
        fun isOp(c: Char) = c in charArrayOf('+','-','*','/','^')

        fun readNumber(): String {
            val start = i
            var hasDot = false
            while (i < s.length && (s[i].isDigit() || (!hasDot && s[i]=='.'))) {
                if (s[i]=='.') hasDot = true
                i++
            }
            return s.substring(start, i)
        }
        fun readIdent(): String {
            val start = i
            while (i < s.length && s[i].isLetter()) i++
            return s.substring(start, i)
        }
        fun top() = st.lastOrNull()

        while (i < s.length) {
            val c = s[i]
            when {
                c.isDigit() || c=='.' -> out += readNumber()
                c.isLetter() -> {
                    val id = readIdent()
                    if (id == "x") out += "x"
                    else st.addLast("fn:$id")
                }
                isOp(c) -> {
                    val o1 = c.toString()
                    while (st.isNotEmpty()) {
                        val t = top()!!
                        if (t.startsWith("op:")) {
                            val o2 = t.removePrefix("op:")
                            val cond = prec(o2) > prec(o1) || (prec(o2) == prec(o1) && o1 != "^")
                            if (cond) { out += st.removeLast().removePrefix("op:"); continue }
                        } else if (t.startsWith("fn:")) {
                            out += st.removeLast().removePrefix("fn:"); continue
                        }
                        break
                    }
                    st.addLast("op:$o1"); i++
                }
                c == '(' -> { st.addLast("("); i++ }
                c == ')' -> {
                    while (top() != "(") {
                        require(st.isNotEmpty()) { "Unbalanced ')'" }
                        val t = st.removeLast()
                        out += when {
                            t.startsWith("op:") -> t.removePrefix("op:")
                            t.startsWith("fn:") -> t.removePrefix("fn:")
                            else -> error("Unbalanced ')'")
                        }
                    }
                    st.removeLast()
                    i++
                    if (top()?.startsWith("fn:") == true) {
                        out += st.removeLast().removePrefix("fn:")
                    }
                }
                else -> error("Bad char: $c")
            }
        }
        while (st.isNotEmpty()) {
            val t = st.removeLast()
            require(t != "(") { "Unbalanced '('" }
            out += when {
                t.startsWith("op:") -> t.removePrefix("op:")
                t.startsWith("fn:") -> t.removePrefix("fn:")
                else -> t
            }
        }
        return out
    }


    private fun evalRPN(rpn: List<String>, x: Double): Double {
        val st = ArrayDeque<Double>()
        for (t in rpn) {
            when {
                t.toDoubleOrNull()!=null -> st.push(t.toDouble())
                t.length==1 && t[0] in ops -> {
                    val b = st.pop(); val a = st.pop()
                    val v = when (t[0]) {
                        '+'-> a+b; '-'-> a-b; '*'-> a*b; '/'-> a/b; '^'-> a.pow(b)
                        else -> error("op")
                    }; st.push(v)
                }
                t == "x" -> st.push(x)
                else -> {
                    val a = st.pop()
                    val v = when (t.lowercase()) {
                        "sin"-> sin(a); "cos"-> cos(a); "tan"-> kotlin.math.tan(a)
                        "sqrt"-> sqrt(a); "abs"-> kotlin.math.abs(a); "ln"-> ln(a)
                        else -> error("Unknown id: $t")
                    }; st.push(v)
                }
            }
        }
        require(st.size==1) { "Bad expression" }
        return st.pop()
    }

    private fun format(v: Double) =
        if (v.isNaN()) "NaN" else "%,.6f".format(v).trimEnd('0').trimEnd(',')
}
