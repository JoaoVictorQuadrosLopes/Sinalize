package com.example.sinalize.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sinalize.viewmodel.LibrasViewModel

@Composable
fun LessonScreen(
    viewModel: LibrasViewModel,
    onBackClick: () -> Unit,
    onLessonClick: (Int) -> Unit
) {
    val lessons = viewModel.lessons.value
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        OutlinedButton(onClick = onBackClick) { Text("Voltar") }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Aulas de Libras", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(lessons) { lesson ->
                val imageResId = context.resources.getIdentifier(lesson.imageName, "drawable", context.packageName)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { onLessonClick(lesson.id) },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = "Imagem do sinal ${lesson.title}",
                                modifier = Modifier.fillMaxWidth().height(170.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Text(lesson.title, style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(lesson.description)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Dica: ${lesson.movementTip}")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { onLessonClick(lesson.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Praticar com câmera")
                        }
                    }
                }
            }
        }
    }
}
