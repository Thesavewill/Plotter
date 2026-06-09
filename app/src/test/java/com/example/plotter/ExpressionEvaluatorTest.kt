package com.example.plotter.domain.evaluator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.tan

class ExpressionEvaluatorTest {

    @Before
    fun setup() {
        ExpressionEvaluator.clearCache()
    }

    @Test
    fun `evaluate simple addition`() {
        assertEquals(5.0, ExpressionEvaluator.evaluate("2 + 3", 0.0)!!, 0.0001)
    }

    @Test
    fun `evaluate with variable x`() {
        assertEquals(15.0, ExpressionEvaluator.evaluate("x + 5", 10.0)!!, 0.0001)
    }

    @Test
    fun `evaluate sin function`() {
        assertEquals(1.0, ExpressionEvaluator.evaluate("sin(x)", PI / 2)!!, 0.0001)
    }

    @Test
    fun `evaluate russian tg function`() {
        val x = PI / 4
        assertEquals(tan(x), ExpressionEvaluator.evaluate("tg(x)", x)!!, 0.0001)
    }

    @Test
    fun `evaluate empty expression returns null`() {
        assertNull(ExpressionEvaluator.evaluate("", 0.0))
    }

    @Test
    fun `evaluate invalid expression returns null`() {
        assertNull(ExpressionEvaluator.evaluate("2 + + +", 0.0))
    }
}