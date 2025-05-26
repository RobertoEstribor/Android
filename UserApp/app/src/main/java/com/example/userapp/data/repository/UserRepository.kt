package com.example.userapp.data.repository

import android.util.Log
import com.example.userapp.data.model.User // Ensure this is the updated User class
import com.example.userapp.data.network.ApiService
// RetrofitInstance import is not strictly needed here if ApiService is passed in constructor

class UserRepository(private val apiService: ApiService) {

    suspend fun fetchUsersFromServer(nempresa: String): List<User>? {
        try {
            val response = apiService.getUsuarios(nempresa)
            if (response.isSuccessful) {
                val userList = response.body()
                if (userList != null) {
                    Log.d("UserRepository", "Successfully fetched ${userList.size} users.")
                    // Log first user details for confirmation (if list is not empty)
                    userList.firstOrNull()?.let {
                        Log.d("UserRepository", "First user: login=${it.login}, nombre=${it.nombre}, pass=${it.pass}")
                    }
                } else {
                    Log.d("UserRepository", "Fetched user list is null.")
                }
                return userList
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("UserRepository", "Error fetching users: ${response.code()} - ${response.message()}")
                if (!errorBody.isNullOrEmpty()) {
                    Log.e("UserRepository", "Error body: $errorBody")
                }
                return null
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception fetching users", e)
            return null
        }
    }
}
