package com.example.sinalize.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sinalize.viewmodel.LibrasViewModel

@Composable
fun ProgressScreen(
    viewModel: LibrasViewModel,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val progress = viewModel.userProgress.value
    val progressPercent = (progress.completedLessons / 10f).coerceAtMost(1f)

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        OutlinedButton(onClick = onBackClick) { Text("Voltar") }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Meu progresso", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("XP total: ${progress.totalXp}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Aulas concluídas: ${progress.completedLessons}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Categorias concluídas: ${progress.completedCategories}")
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(progress = { progressPercent }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text("Progresso geral: ${(progressPercent * 100).toInt()}%")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onHomeClick, modifier = Modifier.fillMaxWidth()) { Text("Voltar para início") }
    }
}
