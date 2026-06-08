package com.example.plotter.ui.panels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp, max = 300.dp), // Гибкая высота вместо жёсткой 115.dp
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Заголовок панели
            Text(
                text = "Функции",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(functions, key = { it.id }) { item ->
                    FunctionRow(
                        item = item,
                        isSelected = item.id == selectedId,
                        onIntent = onIntent,
                        onColorClick = { onIntent(PlotterContract.Intent.OpenColorPicker(item.id)) }
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // Кнопки действий
            // Добавьте кнопку в раздел кнопок действий:
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onIntent(PlotterContract.Intent.AddFunction()) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Добавить")
                }

                // Новая кнопка для рукописного ввода
                IconButton(
                    onClick = { onIntent(PlotterContract.Intent.OpenHandwritingMode) },
                    modifier = Modifier
                        .size(48.dp)
                        .weight(0.3f),
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Рукописный ввод",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Button(
                    onClick = onImageCaptureRequested,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("С фото")
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val selectedColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, selectedColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Кнопка выбора цвета
            IconButton(
                onClick = onColorClick,
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(item.color)
                        .border(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), CircleShape)
                )
            }

            // Поле ввода функции
            BasicTextField(
                value = item.expression,
                onValueChange = { onIntent(PlotterContract.Intent.UpdateExpression(item.id, it)) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                ),
                modifier = Modifier
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            onIntent(PlotterContract.Intent.SelectFunction(item.id))
                            keyboardController?.hide()
                        }
                    },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (item.expression.text.isEmpty()) {
                            Text(
                                text = "f(x) = ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Кнопка удаления
            IconButton(
                onClick = { onIntent(PlotterContract.Intent.RemoveFunction(item.id)) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}