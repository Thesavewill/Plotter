package com.example.plotter.domain.evaluator

import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import kotlin.math.tan

object CustomFunctions {
    val tg = object : Function("tg", 1) {
        override fun apply(vararg args: Double): Double = tan(args[0])
    }
    val ctg = object : Function("ctg", 1) {
        override fun apply(vararg args: Double): Double = 1.0 / tan(args[0])
    }
    val all = listOf(tg, ctg)
}

object ExpressionEvaluator {
    private val cache = mutableMapOf<String, net.objecthunter.exp4j.Expression>()

    fun evaluate(expression: String, x: Double): Double? {
        if (expression.isBlank()) return null
        return try {
            val compiled = cache.getOrPut(expression) {
                ExpressionBuilder(expression)
                    .variable("x")
                    .functions(CustomFunctions.all)
                    .build()
            }
            compiled.setVariable("x", x).evaluate()
        } catch (_: Exception) {
            null
        }
    }

    fun clearCache() {
        cache.clear()
    }
}