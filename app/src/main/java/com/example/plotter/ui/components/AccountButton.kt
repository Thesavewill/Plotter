package com.example.plotter.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.plotter.ui.PlotterContract

@Composable
fun AccountButton(
    userEmail: String?,
    onIntent: (PlotterContract.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Кнопка аккаунта
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .border(1.dp, Color.Gray, CircleShape)
                .clickable { showMenu = true }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Account",
                modifier = Modifier.size(24.dp),
                tint = if (userEmail != null) Color.Blue else Color.Gray
            )

            if (userEmail != null) {
                Text(
                    text = userEmail.substringBefore("@"),
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.Black
                )
            }
        }

        // Выпадающее меню
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (userEmail != null) {
                DropdownMenuItem(
                    text = { Text("Сохранить график") },
                    onClick = {
                        onIntent(PlotterContract.Intent.SaveGraph)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Мои графики") },
                    onClick = {
                        onIntent(PlotterContract.Intent.ShowSavedGraphs)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Выйти") },
                    onClick = {
                        onIntent(PlotterContract.Intent.SignOut)
                        showMenu = false
                    }
                )
            } else {
                DropdownMenuItem(
                    text = { Text("Войти через Google") },
                    onClick = {
                        onIntent(PlotterContract.Intent.ToggleSignIn)
                        showMenu = false
                    }
                )
            }
        }
    }
}