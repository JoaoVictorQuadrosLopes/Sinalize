package com.example.sinalize.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
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
fun CategoryScreen(
    viewModel: LibrasViewModel,
    onBackClick: () -> Unit,
    onCategoryClick: (Int) -> Unit
) {
    val categories = viewModel.categories.value

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        OutlinedButton(onClick = onBackClick) { Text("Voltar") }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Categorias", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(categories) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable(enabled = !category.isLocked) { onCategoryClick(category.id) },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(category.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(category.description)
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { category.progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(20.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text(if (category.isLocked) "Bloqueado" else "Disponível") }
                        )
                    }
                }
            }
        }
    }
}
