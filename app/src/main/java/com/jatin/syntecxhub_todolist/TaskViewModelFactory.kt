package com.jatin.syntecxhub_todolist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TaskViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db         = TaskDatabase.getDatabase(context)
        val repository = TaskRepository(db.taskDao())
        return TaskViewModel(repository) as T
    }
}
