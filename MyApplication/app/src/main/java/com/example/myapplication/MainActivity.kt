package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.exp
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorApp()
        }
    }
}

@Composable
fun CalculatorApp() {
    var displayText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.Black),
            contentAlignment = Alignment.CenterEnd
        ) {
            BasicText(
                text = displayText,
                style = TextStyle(
                    fontSize = 34.sp,
                    color = Color.White
                ),
                modifier = Modifier.padding(20.dp)
            )
        }

        val buttons = listOf(
            listOf("C", "√", "^", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("(", "0", ")", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { symbol ->
                    Button(
                        onClick = {
                            displayText = handleButtonClick(symbol, displayText)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (symbol) {
                                "/", "*", "-", "+", "=" -> Color(0xFFBC510A)
                                "C", "√", "^", "(", ")" -> Color(0xFF888280)
                                else -> Color(0xFF211E1E)
                            }
                        )
                    ) {
                        Text(text = symbol, fontSize = 34.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun handleButtonClick(symbol: String, currentDisplay: String): String {
    return when (symbol) {
        "=" -> {
            try {
                val result = Calculator.evaluate(currentDisplay)
                result.toString()
            } catch (e: Exception) {
                "Error"
            }
        }
        "C" -> ""
        else -> currentDisplay + symbol
    }
}

object Calculator {

    private fun precedence(op: Char): Int {
        return when (op) {
            '+', '-' -> 1
            '*', '/' -> 2
            '^', 'r' -> 3
            'e' -> 4
            else -> -1
        }
    }

    private fun applyOp(a: Double, b: Double, op: Char): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> a / b
            '^' -> a.pow(b)
            'r' -> b.pow(1 / a)
            'e' -> exp(b)
            else -> throw UnsupportedOperationException("Operador no soportado")
        }
    }

    fun infixToPostfix(expression: String): String {
        val result = StringBuilder()
        val stack = ArrayDeque<Char>() // Utilizar ArrayDeque en lugar de Stack
        var i = 0
        while (i < expression.length) {
            val c = expression[i]
            if (c.isDigit() || c == '.') {
                while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                    result.append(expression[i++])
                }
                result.append(' ')
                i--
            } else if (c == '(') {
                stack.addLast(c)
            } else if (c == ')') {
                while (stack.isNotEmpty() && stack.last() != '(') {
                    result.append(stack.removeLast()).append(' ')
                }
                stack.removeLast()
            } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == 'e' || c == 'r') {
                while (stack.isNotEmpty() && precedence(stack.last()) >= precedence(c)) {
                    result.append(stack.removeLast()).append(' ')
                }
                stack.addLast(c)
            }
            i++
        }
        while (stack.isNotEmpty()) {
            result.append(stack.removeLast()).append(' ')
        }
        return result.toString()
    }

    fun evaluatePostfix(expression: String): Double {
        val stack = ArrayDeque<Double>() // Utilizar ArrayDeque en lugar de Stack
        val tokens = expression.split(" ").filter { it.isNotEmpty() }
        for (token in tokens) {
            if (token[0].isDigit() || token[0] == '.') {
                stack.addLast(token.toDouble())
            } else {
                val b = stack.removeLast()
                val a = if (stack.isNotEmpty() && token[0] != 'e') stack.removeLast() else 0.0
                stack.addLast(applyOp(a, b, token[0]))
            }
        }
        return stack.removeLast()
    }

    fun evaluate(expression: String): Double {
        return evaluatePostfix(infixToPostfix(expression))
    }
}
