package com.jatin.syntecxhub_todolist.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jatin.syntecxhub_todolist.data.db.AppDatabase
import com.jatin.syntecxhub_todolist.data.model.Task
import com.jatin.syntecxhub_todolist.data.repo.TaskRepository
import com.jatin.syntecxhub_todolist.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(app: Application) : AndroidViewModel(app) {

    private val repo    = TaskRepository(AppDatabase.getInstance(app).taskDao())
    private val session = SessionManager(app)
    val userId          = session.getUserId()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Task>>(emptyList())
    val searchResults: StateFlow<List<Task>> = _searchResults.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getTasksByUser(userId).collect { _tasks.value = it }
        }
    }

    fun addTask(
        title: String,
        description: String,
        priority: Int,
        category: String,
        reminderTime: Long? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repo.insert(
                Task(
                    userId       = userId,
                    title        = title.trim(),
                    description  = description.trim(),
                    priority     = priority,
                    category     = category,
                    reminderTime = reminderTime
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { repo.update(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repo.delete(task) }
    }

    fun search(query: String) {
        viewModelScope.launch {
            repo.searchTasks(userId, query).collect { _searchResults.value = it }
        }
    }

    fun getTotalCount()                  = repo.getTotalCount(userId)
    fun getCompletedCount()              = repo.getCompletedCount(userId)
    fun getCountByPriority(p: Int)       = repo.getCountByPriority(userId, p)
    fun getCategories()                  = repo.getCategories(userId)
}
