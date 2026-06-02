package com.jatin.syntecxhub_todolist

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun addTask(task: Task) {
        taskDao.insert(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.update(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.delete(task)
    }

    suspend fun deleteAllTasks() {
        taskDao.deleteAll()
    }
}
