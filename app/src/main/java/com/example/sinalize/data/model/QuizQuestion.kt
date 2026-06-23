package com.example.sinalize.data.model

data class QuizQuestion(
    val id: Int,
    val lessonId: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)
