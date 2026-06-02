package com.jatin.syntecxhub_todolist

data class Task(
    val id: Long,
    val title: String,
    var isCompleted: Boolean = false
)