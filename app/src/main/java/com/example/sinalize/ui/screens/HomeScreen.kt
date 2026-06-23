package com.example.sinalize.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.sinalize.viewmodel.LibrasViewModel

@Composable
fun HomeScreen(
    viewModel: LibrasViewModel,
    onCategoryClick: () -> Unit,
    onProgressClick: () -> Unit
) {
    val progress = viewModel.userProgress.value
    val percent = (progress.completedLessons / 10f).coerceAtMost(1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Sinalize", style = MaterialTheme.typography.headlineLarge)
            Text("Aprenda Libras com aulas, prática por câmera e feedback em tempo real.")

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Seu aprendizado", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("XP: ${progress.totalXp}")
                        Text("Aulas: ${progress.completedLessons}")
                        Text("${(percent * 100).toInt()}%")
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { percent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onCategoryClick, modifier = Modifier.fillMaxWidth()) {
                Text("Começar aulas")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = onProgressClick, modifier = Modifier.fillMaxWidth()) {
                Text("Ver progresso")
            }
        }
    }
}
