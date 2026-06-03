package com.jatin.syntecxhub_todolist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int = -1,           // -1 = guest user
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Int = 1,          // 0=High | 1=Medium | 2=Low
    val category: String = "General",
    val reminderTime: Long? = null, // null = no reminder
    val createdAt: Long = System.currentTimeMillis()
)
