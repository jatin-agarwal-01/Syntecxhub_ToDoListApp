package com.jatin.syntecxhub_todolist.data.repo

import com.jatin.syntecxhub_todolist.data.db.UserDao
import com.jatin.syntecxhub_todolist.data.model.User

class UserRepository(private val dao: UserDao) {

    suspend fun register(user: User): Long = dao.insert(user)

    suspend fun login(email: String, password: String): User? =
        dao.login(email, password)

    suspend fun getUserByEmail(email: String): User? =
        dao.getUserByEmail(email)

    suspend fun getUserById(id: Int): User? =
        dao.getUserById(id)

    suspend fun update(user: User) = dao.update(user)
}
