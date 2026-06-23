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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sinalize.viewmodel.LibrasViewModel

@Composable
fun QuizScreen(
    viewModel: LibrasViewModel,
    onBackClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    val lesson = viewModel.selectedLesson.value
    val question = viewModel.quizQuestions.value.firstOrNull()
    val selectedAnswer = viewModel.selectedAnswer.value
    val isCorrect = viewModel.isAnswerCorrect.value

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        OutlinedButton(onClick = onBackClick) { Text("Voltar") }
        Spacer(modifier = Modifier.height(16.dp))
        Text(lesson?.title ?: "Aula", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(lesson?.description ?: "")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Dica: ${lesson?.movementTip ?: ""}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (question != null) {
            Text(question.question, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            question.options.forEach { option ->
                OutlinedButton(
                    onClick = { viewModel.answerQuestion(option) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) { Text(option) }
            }

            if (selectedAnswer != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(if (isCorrect == true) "Resposta correta! +10 XP" else "Resposta incorreta. Revise esse sinal.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onFinishClick, modifier = Modifier.fillMaxWidth()) { Text("Finalizar aula") }
            }
        } else {
            Text("Nenhum quiz encontrado para esta aula.")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onFinishClick, modifier = Modifier.fillMaxWidth()) { Text("Finalizar aula") }
        }
    }
}
