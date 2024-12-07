package com.example.eventosapp.models

data class Comment(
    val id: String = "",
    val text: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)