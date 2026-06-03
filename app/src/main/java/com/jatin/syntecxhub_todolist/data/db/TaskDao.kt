package com.jatin.syntecxhub_todolist.data.db

import androidx.room.*
import com.jatin.syntecxhub_todolist.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTasksByUser(userId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND category = :category ORDER BY createdAt DESC")
    fun getTasksByCategory(userId: Int, category: String): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks 
        WHERE userId = :userId 
        AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchTasks(userId: Int, query: String): Flow<List<Task>>

    @Query("SELECT DISTINCT category FROM tasks WHERE userId = :userId")
    fun getCategoriesByUser(userId: Int): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId")
    fun getTotalCount(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId AND isCompleted = 1")
    fun getCompletedCount(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId AND priority = :priority")
    fun getCountByPriority(userId: Int, priority: Int): Flow<Int>
}
