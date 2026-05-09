package com.example.plotter.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plotter.ui.PlotterContract

@Composable
fun KeyboardPanel(
    onIntent: (PlotterContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .border(2.dp, Color.Black)
    ) {
        val buttonHeight = maxHeight / 4f

        val keys = listOf(
            "7", "8", "9", "sin()", "*",
            "4", "5", "6", "cos()", "/",
            "1", "2", "3", "tg()", "x",
            "-", "0", "+", "ctg()", "⌫"
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.fillMaxSize()
        ) {
            items(keys) { key ->
                Button(
                    onClick = {
                        if (key == "⌫") onIntent(PlotterContract.Intent.DeleteSymbol)
                        else onIntent(PlotterContract.Intent.InsertSymbol(key))
                    },
                    modifier = Modifier
                        .padding(2.dp)
                        .height(buttonHeight)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (key) {
                            "x" -> Color.Blue
                            "⌫" -> Color(0xFFE57373)
                            else -> Color.White
                        },
                        contentColor = if (key == "x" || key == "⌫") Color.White else Color.Black
                    )
                ) {
                    Text(
                        text = key,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}