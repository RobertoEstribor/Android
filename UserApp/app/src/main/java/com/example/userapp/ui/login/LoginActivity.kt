package com.example.userapp.ui.login

import android.os.Bundle
import android.util.Base64 // Import Base64
import android.util.Log // Import Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.userapp.data.db.AppDatabase
import com.example.userapp.data.db.UserDao
import com.example.userapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets // For specifying charset

class LoginActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = AppDatabase.getDatabase(applicationContext).userDao()

        binding.buttonLogin.setOnClickListener {
            val login = binding.editTextUsername.text.toString().trim()
            val plainTextPasswordInput = binding.editTextPassword.text.toString().trim() // User inputs plain text

            if (login.isNotEmpty() && plainTextPasswordInput.isNotEmpty()) {
                lifecycleScope.launch {
                    val user = userDao.getUserByLogin(login)
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
                            Log.e("LoginActivity", "Error decoding password for user: $login", e)
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
