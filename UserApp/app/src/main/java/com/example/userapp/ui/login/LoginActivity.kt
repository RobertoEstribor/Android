package com.example.userapp.ui.login

import android.os.Bundle
import android.util.Base64 // Import Base64
import android.util.Log // Import Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.userapp.R
import com.example.userapp.data.db.AppDatabase
import com.example.userapp.data.db.UserDao
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets // For specifying charset

class LoginActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao
    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userDao = AppDatabase.getDatabase(applicationContext).userDao()

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val plainTextPasswordInput = editTextPassword.text.toString().trim() // User inputs plain text

            if (username.isNotEmpty() && plainTextPasswordInput.isNotEmpty()) {
                lifecycleScope.launch {
                    val user = userDao.getUserByUsername(username)
                    if (user != null && user.pass != null) { // Check if user and stored pass are not null
                        try {
                            // Decode the stored Base64 password
                            val decodedPasswordBytes = Base64.decode(user.pass, Base64.DEFAULT)
                            val decodedPasswordStored = String(decodedPasswordBytes, StandardCharsets.UTF_8)

                            if (decodedPasswordStored == plainTextPasswordInput) {
                                Toast.makeText(applicationContext, "Login Correcto", Toast.LENGTH_LONG).show()
                                // TODO: Navigate to another part of the app if login is successful
                            } else {
                                Toast.makeText(applicationContext, "Invalid username or password", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: IllegalArgumentException) {
                            // Handle cases where user.pass is not a valid Base64 string
                            Log.e("LoginActivity", "Error decoding password for user: $username", e)
                            Toast.makeText(applicationContext, "Login error: Invalid stored password format", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // User not found or stored password is null
                        Toast.makeText(applicationContext, "Invalid username or password", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(applicationContext, "Please enter username and password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
