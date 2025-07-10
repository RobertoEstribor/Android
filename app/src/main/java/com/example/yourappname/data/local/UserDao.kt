package com.example.yourappname.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE entrada = :entrada")
    suspend fun getUserByEntrada(entrada: String): UserEntity?

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
