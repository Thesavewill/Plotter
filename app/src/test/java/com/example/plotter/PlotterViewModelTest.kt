package com.example.plotter.viewmodel

import android.content.Context
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.plotter.data.auth.AuthManager
import com.example.plotter.ui.PlotterContract.Intent
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import org.mockito.junit.MockitoJUnitRunner

/**
 * Юнит-тесты для PlotterViewModel с моками для Firebase.
 */
@RunWith(MockitoJUnitRunner::class)
class PlotterViewModelTest {

    private lateinit var viewModel: PlotterViewModel
    private lateinit var context: Context
    private lateinit var mockAuth: FirebaseAuth

    @Before
    fun setup() {
        // Мокаем FirebaseAuth перед инициализацией AuthManager
        mockAuth = mock(FirebaseAuth::class.java)
        mockStatic(FirebaseAuth::class.java).use { mockedStatic ->
            mockedStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)
            whenever(mockAuth.currentUser).thenReturn(null) // Неавторизован по умолчанию

            context = mock(Context::class.java)
            viewModel = PlotterViewModel(context)
        }
    }

    @Test
    fun `initial state has one default function`() {
        val state = viewModel.state.value
        assertEquals(1, state.functions.size)
        assertNotNull(state.functions[0].id)
    }

    @Test
    fun `AddFunction adds new function`() {
        viewModel.handleIntent(Intent.AddFunction())
        assertEquals(2, viewModel.state.value.functions.size)
    }

    @Test
    fun `RemoveFunction removes function by id`() {
        val initialId = viewModel.state.value.functions[0].id
        viewModel.handleIntent(Intent.AddFunction())
        viewModel.handleIntent(Intent.RemoveFunction(initialId))
        assertFalse(viewModel.state.value.functions.any { it.id == initialId })
    }

    @Test
    fun `RemoveFunction keeps at least one function`() {
        val id = viewModel.state.value.functions[0].id
        viewModel.handleIntent(Intent.RemoveFunction(id))
        assertEquals(1, viewModel.state.value.functions.size)
    }

    @Test
    fun `UpdateExpression updates text`() {
        val id = viewModel.state.value.functions[0].id
        val newValue = TextFieldValue("sin(x)", TextRange(6))
        viewModel.handleIntent(Intent.UpdateExpression(id, newValue))
        assertEquals("sin(x)", viewModel.state.value.functions[0].expression.text)
    }

    @Test
    fun `SelectFunction updates selectedId`() {
        viewModel.handleIntent(Intent.AddFunction())
        val newId = viewModel.state.value.functions.last().id
        viewModel.handleIntent(Intent.SelectFunction(newId))
        assertEquals(newId, viewModel.state.value.selectedFunctionId)
    }

    @Test
    fun `Pan updates canvas offset`() {
        val initialX = viewModel.state.value.canvas.offsetX
        viewModel.handleIntent(Intent.Pan(10f, 20f))
        assertEquals(initialX + 10f, viewModel.state.value.canvas.offsetX, 0.001f)
    }

    @Test
    fun `InsertSymbol inserts at cursor`() {
        val id = viewModel.state.value.functions[0].id
        viewModel.handleIntent(Intent.UpdateExpression(id, TextFieldValue("x", TextRange(1))))
        viewModel.handleIntent(Intent.SelectFunction(id))
        viewModel.handleIntent(Intent.InsertSymbol("^2"))
        assertEquals("x^2", viewModel.state.value.functions[0].expression.text)
    }

    @Test
    fun `OpenHandwritingMode sets flag to true`() {
        viewModel.handleIntent(Intent.OpenHandwritingMode)
        assertTrue(viewModel.state.value.isHandwritingMode)
    }

    @Test
    fun `CloseHandwritingMode sets flag to false`() {
        viewModel.handleIntent(Intent.OpenHandwritingMode)
        viewModel.handleIntent(Intent.CloseHandwritingMode)
        assertFalse(viewModel.state.value.isHandwritingMode)
    }

    @Test
    fun `RequestRenameGraph opens dialog`() {
        viewModel.handleIntent(Intent.RequestRenameGraph("test-id", "Old name"))
        assertTrue(viewModel.state.value.showRenameDialog)
        assertEquals("test-id", viewModel.state.value.renameGraphId)
        assertEquals("Old name", viewModel.state.value.renameGraphName)
    }

    @Test
    fun `CloseRenameDialog resets state`() {
        viewModel.handleIntent(Intent.RequestRenameGraph("id", "name"))
        viewModel.handleIntent(Intent.CloseRenameDialog)
        assertFalse(viewModel.state.value.showRenameDialog)
        assertNull(viewModel.state.value.renameGraphId)
    }
}