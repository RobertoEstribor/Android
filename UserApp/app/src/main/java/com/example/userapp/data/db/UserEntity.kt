package com.example.userapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val login: String, // Users login, serves as unique ID
    val nombre: String?,
    val pass: String? // Stores the base64 encoded password
)
