package com.jatin.syntecxhub_todolist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,   // stored as plain text (fine for local/internship)
    val avatarInitials: String = name.take(2).uppercase(),
    val createdAt: Long = System.currentTimeMillis()
)
