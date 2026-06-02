package com.jatin.syntecxhub_todolist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Int = 1,  // 0=High, 1=Medium, 2=Low
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
