package com.example.userapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
// Toast can be removed if tvLoginError is the primary feedback mechanism
// import android.widget.Toast 
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.userapp.MainActivity // Import MainActivity
import com.example.userapp.R
import com.example.userapp.data.db.AppDatabase
// SoapService is needed for UserRepository instantiation
import com.example.userapp.data.network.SoapService 
import com.example.userapp.data.repository.UserRepository
import kotlinx.coroutines.launch
// No need for Base64 or Log here if UserRepository handles all logic and logging

class LoginActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var etLoginUsername: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvLoginError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Instantiate UserRepository
        val userDao = AppDatabase.getDatabase(applicationContext).userDao()
        // SoapService is an object, so we pass it directly.
        userRepository = UserRepository(SoapService, userDao)

        // Initialize UI Elements
        etLoginUsername = findViewById(R.id.etLoginUsername)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvLoginError = findViewById(R.id.tvLoginError)

        btnLogin.setOnClickListener {
            val username = etLoginUsername.text.toString().trim()
            // Password should not be trimmed, as spaces can be part of a password.
            val password = etLoginPassword.text.toString() 

            if (username.isEmpty() || password.isEmpty()) {
                tvLoginError.text = "Username and password cannot be empty."
                tvLoginError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvLoginError.visibility = View.GONE // Clear previous error

            lifecycleScope.launch {
                val isAuthenticated = userRepository.authenticateUser(username, password)
                if (isAuthenticated) {
                    // tvLoginError.visibility = View.GONE // Already done before launch
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish() // Remove LoginActivity from the back stack
                } else {
                    tvLoginError.text = "Invalid username or password."
                    tvLoginError.visibility = View.VISIBLE
                }
            }
        }
    }
}
