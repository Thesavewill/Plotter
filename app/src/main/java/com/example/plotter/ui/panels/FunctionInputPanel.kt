package com.example.plotter.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.plotter.domain.model.PlotFunction
import com.example.plotter.ui.PlotterContract

@Composable
fun FunctionInputPanel(
    functions: List<PlotFunction>,
    selectedId: String?,
    colorPickerState: PlotterContract.ColorPickerState,
    onIntent: (PlotterContract.Intent) -> Unit,
    onImageCaptureRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .background(Color.White)
            .border(2.dp, Color.Black)
            .padding(8.dp)
    ) {
        LazyColumn {
            items(functions, key = { it.id }) { item ->
                FunctionRow(
                    item = item,
                    isSelected = item.id == selectedId,
                    onIntent = onIntent,
                    onColorClick = { onIntent(PlotterContract.Intent.OpenColorPicker(item.id)) }
                )
            }
            item {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    Button(
                        onClick = { onIntent(PlotterContract.Intent.AddFunction()) },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить график", style = MaterialTheme.typography.labelLarge)
                    }
                    Button(
                        onClick = onImageCaptureRequested,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Распознать с фото",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("С фото", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun FunctionRow(
    item: PlotFunction,
    isSelected: Boolean,
    onIntent: (PlotterContract.Intent) -> Unit,
    onColorClick: () -> Unit
) {
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Button(
                onClick = { onIntent(PlotterContract.Intent.RemoveFunction(item.id)) },
                modifier = Modifier.padding(end = 5.dp).size(20.dp),
                shape = RoundedCornerShape(3.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
            }
            Button(
                onClick = onColorClick,
                modifier = Modifier.padding(top = 5.dp, end = 8.dp).size(20.dp),
                shape = RoundedCornerShape(3.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = item.color)
            ) {}
        }
        BasicTextField(
            value = item.expression,
            onValueChange = { onIntent(PlotterContract.Intent.UpdateExpression(item.id, it)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                .border(1.dp, if (isSelected) Color.Blue else Color.Gray, RoundedCornerShape(4.dp))
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        onIntent(PlotterContract.Intent.SelectFunction(item.id))
                    }
                },
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                    if (item.expression.text.isEmpty()) {
                        Text("Поле", color = Color.Gray)
                    }
                    innerTextField()
                }
            }
        )
    }
}