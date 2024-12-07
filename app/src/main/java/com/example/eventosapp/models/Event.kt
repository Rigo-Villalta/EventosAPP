package com.example.eventosapp.models

data class Event(
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val location: String = "",
    val createdBy: String = "",
    val timestamp: Long = System.currentTimeMillis()
)