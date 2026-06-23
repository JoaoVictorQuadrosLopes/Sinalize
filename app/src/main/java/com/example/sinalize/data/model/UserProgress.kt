package com.example.sinalize.data.model

data class UserProgress(
    val totalXp: Int = 0,
    val completedLessons: Int = 0,
    val completedCategories: Int = 0,
    val lastLessonId: Int? = null
)
