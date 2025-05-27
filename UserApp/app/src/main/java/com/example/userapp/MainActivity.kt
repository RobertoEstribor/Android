package com.example.userapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.userapp.data.db.AppDatabase
import com.example.userapp.data.network.SoapService
import com.example.userapp.data.repository.UserRepository
import com.example.userapp.util.CompanyCodeManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var etCompanyCode: EditText
    private lateinit var btnSaveCompanyCode: Button
    private lateinit var tvCurrentCompanyCode: TextView // Will be used for status messages too
    private lateinit var btnFetchUsers: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        etCompanyCode = findViewById(R.id.etCompanyCode)
        btnSaveCompanyCode = findViewById(R.id.btnSaveCompanyCode)
        tvCurrentCompanyCode = findViewById(R.id.tvCurrentCompanyCode) // Reusing for status
        btnFetchUsers = findViewById(R.id.btnFetchUsers)
        progressBar = findViewById(R.id.progressBar)

        // Instantiate UserRepository
        val userDao = AppDatabase.getDatabase(applicationContext).userDao()
        // SoapService is an object (singleton), so we pass it directly
        userRepository = UserRepository(SoapService, userDao)

        // Load and display existing company code
        loadAndDisplayCompanyCode()

        // Set OnClickListener for the save company code button
        btnSaveCompanyCode.setOnClickListener {
            val companyCode = etCompanyCode.text.toString().trim()
            if (companyCode.isNotBlank()) {
                CompanyCodeManager.saveCompanyCode(this, companyCode)
                tvCurrentCompanyCode.text = "Current Company Code: $companyCode"
                Toast.makeText(this, "Company code saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Company code cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Set OnClickListener for the fetch users button
        btnFetchUsers.setOnClickListener {
            val companyCode = CompanyCodeManager.getCompanyCode(this)
            if (companyCode.isNullOrEmpty()) {
                Toast.makeText(this, "Please save a company code first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // Update UI before starting
                tvCurrentCompanyCode.text = "Loading users for company: $companyCode..."
                progressBar.visibility = View.VISIBLE
                btnFetchUsers.isEnabled = false
                btnSaveCompanyCode.isEnabled = false // Also disable save button during fetch

                val success = userRepository.fetchAndSaveUsers(companyCode)

                // Update UI after completion
                progressBar.visibility = View.GONE
                btnFetchUsers.isEnabled = true
                btnSaveCompanyCode.isEnabled = true

                if (success) {
                    // To show user count, we'd need a method in UserRepository to get users from DB
                    // For now, just a success message.
                    // val usersInDb = userRepository.userDao.getAllUsers() // Example, assumes getAllUsers is suspend
                    // tvCurrentCompanyCode.text = "Users loaded: ${usersInDb.size}. Current Code: $companyCode"
                    tvCurrentCompanyCode.text = "Users loaded/updated successfully for code: $companyCode!"
                    Toast.makeText(this@MainActivity, "Users loaded/updated successfully!", Toast.LENGTH_LONG).show()
                } else {
                    tvCurrentCompanyCode.text = "Failed to load users for code: $companyCode. Check logs."
                    Toast.makeText(this@MainActivity, "Failed to load users. Check logs.", Toast.LENGTH_LONG).show()
                }
                // Re-display company code if it was overwritten by status message and fetch failed
                // Or, if successful, keep the success message for a bit or update with count.
                // For simplicity here, we'll leave the status message.
                // To revert to just company code:
                // loadAndDisplayCompanyCode() 
            }
        }
    }

    private fun loadAndDisplayCompanyCode() {
        val companyCode = CompanyCodeManager.getCompanyCode(this)
        if (companyCode != null && companyCode.isNotBlank()) {
            etCompanyCode.setText(companyCode)
            // Update the text view to show the current company code,
            // especially if it was previously used for status messages.
            tvCurrentCompanyCode.text = "Current Company Code: $companyCode"
        } else {
            tvCurrentCompanyCode.text = "Current Company Code: Not Set"
        }
    }
}
