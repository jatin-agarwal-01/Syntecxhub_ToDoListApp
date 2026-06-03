package com.jatin.syntecxhub_todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allTasks.collect { list ->
                _tasks.value = list
            }
        }
    }

    fun addTask(
        title: String,
        description: String,
        priority: Int,
        reminderTime: Long? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insert(
                Task(
                    title        = title.trim(),
                    description  = description.trim(),
                    priority     = priority,
                    reminderTime = reminderTime
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repository.update(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.delete(task) }
    }
}