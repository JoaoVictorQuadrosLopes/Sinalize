package com.example.sinalize.data.repository

import com.example.sinalize.data.model.Category
import com.example.sinalize.data.model.Lesson
import com.example.sinalize.data.model.QuizQuestion

class LibrasRepository {

    fun getCategories(): List<Category> {
        return listOf(
            Category(1, "Cumprimentos", "Aprenda sinais básicos para iniciar uma conversa.", 0.3f),
            Category(2, "Alfabeto", "Conheça o alfabeto manual em Libras.", 0f),
            Category(3, "Números", "Aprenda os números básicos em Libras.", 0f),
            Category(4, "Família", "Sinais relacionados a familiares e pessoas próximas.", 0f, true),
            Category(5, "Cores", "Aprenda sinais de cores usadas no dia a dia.", 0f, true)
        )
    }

    fun getLessonsByCategory(categoryId: Int): List<Lesson> {
        return getLessons().filter { it.categoryId == categoryId }
    }

    fun getLessonById(lessonId: Int): Lesson? {
        return getLessons().find { it.id == lessonId }
    }

    fun getQuizByLesson(lessonId: Int): List<QuizQuestion> {
        return getQuizQuestions().filter { it.lessonId == lessonId }
    }

    private fun getLessons(): List<Lesson> {
        return listOf(
            Lesson(1, 1, "Olá", "O sinal de 'Olá' é usado para cumprimentar alguém.", "Levante a mão aberta e faça um pequeno movimento lateral.", "ola", "open_hand"),
            Lesson(2, 1, "Bom dia", "O sinal de 'Bom dia' é usado pela manhã.", "Faça o movimento com expressão facial amigável.", "bom_dia", "open_hand"),
            Lesson(3, 1, "Obrigado", "O sinal de 'Obrigado' expressa agradecimento.", "Leve a mão aberta próxima ao queixo e mova-a para frente.", "obrigado", "open_hand"),
            Lesson(4, 2, "Letra A", "A letra A faz parte do alfabeto manual em Libras.", "Feche a mão, mantendo o polegar encostado ao lado.", "letra_a", "closed_fist"),
            Lesson(5, 2, "Letra B", "A letra B faz parte do alfabeto manual em Libras.", "Mantenha os dedos juntos e estendidos, com o polegar dobrado.", "letra_b", "open_hand"),
            Lesson(6, 3, "Número 1", "O número 1 é um dos sinais numéricos básicos.", "Levante apenas o dedo indicador.", "numero_1", "number_one"),
            Lesson(7, 3, "Número 2", "O número 2 é representado com dois dedos levantados.", "Levante indicador e médio, mantendo os outros dedos fechados.", "numero_2", "number_two"),
            Lesson(8, 3, "Número 5", "O número 5 usa a mão aberta.", "Abra todos os dedos e mantenha a palma visível.", "numero_5", "open_hand")
        )
    }

    private fun getQuizQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(1, 1, "Qual é o significado desse sinal?", listOf("Tchau", "Olá", "Obrigado", "Boa noite"), "Olá"),
            QuizQuestion(2, 2, "Qual sinal representa um cumprimento pela manhã?", listOf("Bom dia", "Desculpa", "Família", "Azul"), "Bom dia"),
            QuizQuestion(3, 3, "Qual alternativa representa agradecimento?", listOf("Olá", "Obrigado", "Boa tarde", "Número 1"), "Obrigado"),
            QuizQuestion(4, 4, "Esse conteúdo pertence a qual categoria?", listOf("Cores", "Família", "Alfabeto", "Cumprimentos"), "Alfabeto"),
            QuizQuestion(5, 6, "Qual dedo deve ficar levantado no número 1?", listOf("Indicador", "Polegar", "Anelar", "Mindinho"), "Indicador"),
            QuizQuestion(6, 7, "Quantos dedos ficam levantados no número 2?", listOf("1", "2", "3", "5"), "2"),
            QuizQuestion(7, 8, "Como fica a mão no número 5?", listOf("Fechada", "Aberta", "Só indicador", "Só polegar"), "Aberta")
        )
    }
}
