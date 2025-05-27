package com.example.userapp.data.repository

import android.util.Log
import com.example.userapp.data.model.User // Ensure this is the updated User class
import com.example.userapp.data.network.SoapService // Import SoapService
import com.example.userapp.data.db.UserDao // Import UserDao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonSyntaxException // Specific exception for Gson parsing issues
import com.example.userapp.data.db.UserEntity // Added import for UserEntity

class UserRepository(
    private val soapService: SoapService,
    private val userDao: UserDao // Added UserDao for later use (as per instructions)
) {

    suspend fun getUsersFromSoap(companyCode: String): List<User>? {
        val jsonResponse = soapService.getUsers(companyCode)

        if (jsonResponse.isNullOrEmpty()) {
            Log.e("UserRepository", "SOAP response is null or empty for company code: $companyCode")
            return null
        }

        Log.d("UserRepository", "Received JSON response: $jsonResponse") // Log the actual JSON

        return try {
            val gson = Gson()
            // Assuming User.kt (com.example.userapp.data.model.User) matches the JSON structure
            // The JSON is expected to be an array of User objects.
            val userListType = object : TypeToken<List<User>>() {}.type
            val users: List<User> = gson.fromJson(jsonResponse, userListType)
            
            Log.d("UserRepository", "Successfully parsed ${users.size} users from SOAP response.")
            if (users.isNotEmpty()) {
                // Log details of the first user to help confirm parsing structure
                Log.d("UserRepository", "First parsed user: Login=${users[0].login}, Nombre=${users[0].nombre}, Pass=${users[0].pass}")
            }
            users
        } catch (e: JsonSyntaxException) { 
            Log.e("UserRepository", "JSON Syntax Error parsing SOAP response: ${e.message}", e)
            null
        } catch (e: Exception) { // Catch other potential exceptions during parsing
            Log.e("UserRepository", "Generic error parsing JSON from SOAP response: ${e.message}", e)
            null
        }
    }

    // The old fetchUsersFromServer method using ApiService has been removed.
    // If local data operations are needed, they would be added here, e.g.:
    // suspend fun saveUsersToDb(users: List<User>) {
    //     userDao.insertAll(users.map { UserEntity(login = it.login, nombre = it.nombre, pass = it.pass) })
    // }
    // suspend fun getUsersFromDb(): List<UserEntity> {
    //     return userDao.getAllUsers()
    // }

    suspend fun fetchAndSaveUsers(companyCode: String): Boolean {
        val users: List<User>? = getUsersFromSoap(companyCode) // This already handles parsing

        if (users != null && users.isNotEmpty()) {
            return try {
                val userEntities = users.map { user ->
                    UserEntity(login = user.login, nombre = user.nombre, pass = user.pass)
                }
                
                // Perform database operations
                Log.d("UserRepository", "Clearing old users from database.")
                userDao.clearAllUsers()
                Log.d("UserRepository", "Inserting ${userEntities.size} new users into database.")
                userDao.insertAll(userEntities)
                Log.d("UserRepository", "Successfully saved ${userEntities.size} users to database.")
                true
            } catch (e: Exception) {
                Log.e("UserRepository", "Error saving users to database: ${e.message}", e)
                false
            }
        } else {
            Log.d("UserRepository", "No users to save. Either fetch/parse failed or list was empty for company code: $companyCode")
            return false
        }
    }

    suspend fun authenticateUser(username: String, providedPass: String): Boolean {
        val userEntity = userDao.getUserByUsername(username)

        if (userEntity == null) {
            Log.d("UserRepository", "Authentication failed: User '$username' not found.")
            return false
        }

        // Direct string comparison for the password.
        // If userEntity.pass were Base64 encoded and needed decoding here:
        // try {
        //     val decodedPasswordBytes = android.util.Base64.decode(userEntity.pass, android.util.Base64.DEFAULT)
        //     val decodedPassword = String(decodedPasswordBytes)
        //     passwordsMatch = decodedPassword == providedPass
        // } catch (e: IllegalArgumentException) {
        //     Log.e("UserRepository", "Error decoding Base64 password for '$username'. Assuming mismatch.", e)
        //     passwordsMatch = false
        // }
        val passwordsMatch = userEntity.pass == providedPass

        if (passwordsMatch) {
            Log.d("UserRepository", "Authentication successful for user '$username'.")
            return true
        } else {
            Log.d("UserRepository", "Authentication failed: Password mismatch for user '$username'.")
            return false
        }
    }
}
