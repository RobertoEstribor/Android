package com.example.userapp.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("SELECT * FROM users WHERE login = :login LIMIT 1")
    suspend fun getUserByLogin(login: String): UserDbModel?

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity> // For debugging or other purposes
}
