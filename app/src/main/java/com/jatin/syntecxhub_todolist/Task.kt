package com.jatin.syntecxhub_todolist

/**
 * Data class representing a single Task in the To-Do List.
 *
 * @property id Unique identifier for the task (usually timestamp).
 * @property title The description or name of the task.
 * @property isCompleted Status indicating if the task is finished.
 */
data class Task(
    val id: Long,
    var title: String,
    var isCompleted: Boolean = false
)