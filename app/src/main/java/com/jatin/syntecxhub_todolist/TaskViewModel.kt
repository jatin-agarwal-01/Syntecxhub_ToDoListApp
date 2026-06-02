package com.jatin.syntecxhub_todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    
    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _taskList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            repository.allTasks.collect { tasks ->
                _taskList.value = tasks
            }
        }
    }

    fun addTask(title: String, description: String = "", priority: Int = 1, dueDate: Long? = null) {
        if (title.isBlank()) return
        
        viewModelScope.launch {
            val newTask = Task(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate
            )
            repository.addTask(newTask)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredTasks(): List<Task> {
        val query = _searchQuery.value.lowercase()
        return if (query.isEmpty()) {
            _taskList.value
        } else {
            _taskList.value.filter { task ->
                task.title.lowercase().contains(query) ||
                task.description.lowercase().contains(query)
            }
        }
    }
}
