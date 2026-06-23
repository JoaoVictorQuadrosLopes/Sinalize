package com.example.sinalize.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.sinalize.data.model.Category
import com.example.sinalize.data.model.Lesson
import com.example.sinalize.data.model.QuizQuestion
import com.example.sinalize.data.model.UserProgress
import com.example.sinalize.data.repository.LibrasRepository

class LibrasViewModel : ViewModel() {

    private val repository = LibrasRepository()

    var categories = mutableStateOf<List<Category>>(emptyList())
        private set

    var lessons = mutableStateOf<List<Lesson>>(emptyList())
        private set

    var selectedLesson = mutableStateOf<Lesson?>(null)
        private set

    var quizQuestions = mutableStateOf<List<QuizQuestion>>(emptyList())
        private set

    var userProgress = mutableStateOf(UserProgress())
        private set

    var selectedAnswer = mutableStateOf<String?>(null)
        private set

    var isAnswerCorrect = mutableStateOf<Boolean?>(null)
        private set

    init {
        loadCategories()
    }

    fun loadCategories() {
        categories.value = repository.getCategories()
    }

    fun loadLessonsByCategory(categoryId: Int) {
        lessons.value = repository.getLessonsByCategory(categoryId)
    }

    fun loadLesson(lessonId: Int) {
        val lesson = repository.getLessonById(lessonId)
        selectedLesson.value = lesson
        quizQuestions.value = lesson?.let { repository.getQuizByLesson(it.id) } ?: emptyList()
        selectedAnswer.value = null
        isAnswerCorrect.value = null
    }

    fun answerQuestion(answer: String) {
        selectedAnswer.value = answer
        val question = quizQuestions.value.firstOrNull()
        if (question != null) {
            val correct = answer == question.correctAnswer
            isAnswerCorrect.value = correct
            if (correct) addXp(10)
        }
    }

    fun completeLesson() {
        val lesson = selectedLesson.value ?: return
        userProgress.value = userProgress.value.copy(
            totalXp = userProgress.value.totalXp + 5,
            completedLessons = userProgress.value.completedLessons + 1,
            lastLessonId = lesson.id
        )
    }

    private fun addXp(amount: Int) {
        userProgress.value = userProgress.value.copy(totalXp = userProgress.value.totalXp + amount)
    }
}
