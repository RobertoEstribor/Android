package com.example.userapp.data.model

// Represents the user object as received from the web service
data class User(
    val login: String,
    val nombre: String,
    val pass: String // Stores the base64 encoded password from JSON
)
