package com.jatin.syntecxhub_todolist.data.repo

import com.jatin.syntecxhub_todolist.data.db.TaskDao
import com.jatin.syntecxhub_todolist.data.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    fun getTasksByUser(userId: Int): Flow<List<Task>>    = dao.getTasksByUser(userId)
    fun getTasksByCategory(userId: Int, cat: String)     = dao.getTasksByCategory(userId, cat)
    fun searchTasks(userId: Int, query: String)          = dao.searchTasks(userId, query)
    fun getCategories(userId: Int)                       = dao.getCategoriesByUser(userId)
    fun getTotalCount(userId: Int)                       = dao.getTotalCount(userId)
    fun getCompletedCount(userId: Int)                   = dao.getCompletedCount(userId)
    fun getCountByPriority(userId: Int, priority: Int)   = dao.getCountByPriority(userId, priority)

    suspend fun insert(task: Task): Long = dao.insert(task)
    suspend fun update(task: Task)       = dao.update(task)
    suspend fun delete(task: Task)       = dao.delete(task)
}
