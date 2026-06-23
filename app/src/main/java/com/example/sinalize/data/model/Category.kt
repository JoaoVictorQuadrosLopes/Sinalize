package com.example.sinalize.data.model

data class Category(
    val id: Int,
    val name: String,
    val description: String,
    val progress: Float = 0f,
    val isLocked: Boolean = false
)
