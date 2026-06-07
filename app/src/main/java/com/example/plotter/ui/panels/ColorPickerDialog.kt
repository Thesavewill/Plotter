package com.example.plotter.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.plotter.ui.theme.AppColors

@Composable
fun ColorPickerDialog(
    initialColor: Color = AppColors.GraphDefaultBlue,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(initialColor) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().heightIn(min = 450.dp, max = 500.dp).shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text(text = "Выберите цвет", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
                Text(text = "Нажмите на цвет для выбора", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 20.dp))

                Card(modifier = Modifier.fillMaxWidth().height(80.dp).padding(bottom = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = selectedColor), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Выбранный цвет", color = if (selectedColor.isLight()) Color.Black else Color.White, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.titleMedium)
                    }
                }

                LazyVerticalGrid(columns = GridCells.Fixed(6), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(AppColors.ColorPickerPalette) { color ->
                        ColorCircle(color = color, isSelected = color == selectedColor, onClick = { selectedColor = color })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) { Text("Отмена") }
                    Button(onClick = { onColorSelected(selectedColor); onDismiss() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = selectedColor, contentColor = if (selectedColor.isLight()) Color.Black else Color.White)) { Text("Применить") }
                }
            }
        }
    }
}

@Composable
private fun ColorCircle(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    Box(modifier = Modifier.aspectRatio(1f).clip(CircleShape).background(color).then(if (isSelected) Modifier.border(3.dp, borderColor, CircleShape) else Modifier).clickable(onClick = onClick))
}

private fun Color.isLight(): Boolean = (red * 0.299 + green * 0.587 + blue * 0.114) > 0.5