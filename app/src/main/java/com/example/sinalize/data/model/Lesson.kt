package com.example.sinalize.data.model

data class Lesson(
    val id: Int,
    val categoryId: Int,
    val title: String,
    val description: String,
    val movementTip: String,
    val imageName: String = "",
    val expectedGesture: String = "",
    val isCompleted: Boolean = false
)
