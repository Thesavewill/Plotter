package com.example.plotter.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.plotter.domain.model.CanvasTransform
import com.example.plotter.domain.model.PlotFunction
import org.junit.Rule
import org.junit.Test

class PlotterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val defaultState = PlotterContract.State(
        canvas = CanvasTransform(isInitialized = true, canvasWidth = 1000f, canvasHeight = 800f),
        functions = listOf(PlotFunction())
    )

    @Test
    fun displaysFunctionInputPanel() {
        composeTestRule.setContent {
            PlotterScreen(
                state = defaultState,
                onIntent = {},
                onImageCaptureRequested = {},
                onSignInRequested = {}
            )
        }
        composeTestRule.onNodeWithText("Функции").assertIsDisplayed()
    }

    @Test
    fun displaysAddFunctionButton() {
        composeTestRule.setContent {
            PlotterScreen(defaultState, {}, {}, {})
        }
        composeTestRule.onNodeWithText("Добавить").assertIsDisplayed()
    }

    @Test
    fun displaysPhotoButton() {
        composeTestRule.setContent {
            PlotterScreen(defaultState, {}, {}, {})
        }
        composeTestRule.onNodeWithText("С фото").assertIsDisplayed()
    }

    @Test
    fun displaysHandwritingButton() {
        composeTestRule.setContent {
            PlotterScreen(defaultState, {}, {}, {})
        }
        composeTestRule.onNodeWithContentDescription("Рукописный ввод").assertIsDisplayed()
    }

    @Test
    fun displaysAccountButton() {
        composeTestRule.setContent {
            PlotterScreen(defaultState, {}, {}, {})
        }
        composeTestRule.onNodeWithContentDescription("Account").assertIsDisplayed()
    }

    @Test
    fun callsOnIntent_whenAddFunctionClicked() {
        var capturedIntent: PlotterContract.Intent? = null
        composeTestRule.setContent {
            PlotterScreen(
                state = defaultState,
                onIntent = { capturedIntent = it },
                onImageCaptureRequested = {},
                onSignInRequested = {}
            )
        }
        composeTestRule.onNodeWithText("Добавить").performClick()
        assert(capturedIntent is PlotterContract.Intent.AddFunction)
    }
}