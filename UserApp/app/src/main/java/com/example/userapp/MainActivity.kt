package com.example.userapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.userapp.R // Assuming R is in com.example.userapp
import com.example.userapp.data.db.AppDatabase // Import AppDatabase
import com.example.userapp.data.db.UserDao // Import UserDao
import com.example.userapp.data.db.UserEntity // Import UserEntity
import com.example.userapp.data.model.User // Import User model
import com.example.userapp.data.network.RetrofitInstance
import com.example.userapp.data.repository.UserRepository
import com.example.userapp.ui.login.LoginActivity
import com.example.userapp.util.CompanyCodeManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var companyCodeStatusText: TextView
    private lateinit var userDao: UserDao // Declare UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userRepository = UserRepository(RetrofitInstance.api)
        userDao = AppDatabase.getDatabase(applicationContext).userDao() // Initialize UserDao

        companyCodeStatusText = findViewById(R.id.companyCodeStatusText)
        val syncDataButton: Button = findViewById(R.id.syncDataButton)

        syncDataButton.setOnClickListener {
            checkCompanyCodeAndFetchUsers(forceShowDialog = true)
        }

        checkCompanyCodeAndFetchUsers()
    }

    private fun checkCompanyCodeAndFetchUsers(forceShowDialog: Boolean = false) {
        val companyCode = CompanyCodeManager.getCompanyCode(this)
        if (companyCode == null || forceShowDialog) {
            promptForCompanyCode()
        } else {
            companyCodeStatusText.text = "Company Code: $companyCode. Fetching users..."
            fetchUsers(companyCode)
        }
    }

    private fun promptForCompanyCode() {
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Enter Company Code")
            .setView(editText)
            .setPositiveButton("Save") { dialog, _ ->
                val code = editText.text.toString().trim()
                if (code.isNotBlank()) {
                    CompanyCodeManager.saveCompanyCode(this, code)
                    companyCodeStatusText.text = "Company Code: $code. Fetching users..."
                    fetchUsers(code)
                } else {
                    Toast.makeText(this, "Company code cannot be empty", Toast.LENGTH_SHORT).show()
                    companyCodeStatusText.text = "Company code not set. Navigating to login..."
                    navigateToLogin() // Navigate if code is empty after trying to save
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                companyCodeStatusText.text = "Company code not set. Navigating to login..."
                navigateToLogin() // Navigate on cancel
                dialog.cancel()
            }
            .setOnDismissListener {
                if (CompanyCodeManager.getCompanyCode(this) == null) {
                     if (!companyCodeStatusText.text.toString().endsWith("Navigating to login...")) {
                        companyCodeStatusText.text = "Company code entry skipped. Navigating to login..."
                        navigateToLogin()
                     }
                }
            }
            .setCancelable(true)
            .show()
    }

    private fun fetchUsers(code: String) {
        lifecycleScope.launch {
            val userList: List<User>? = userRepository.fetchUsersFromServer(code)
            if (userList != null) {
                Log.d("MainActivity", "Successfully fetched ${userList.size} users from server.")
                companyCodeStatusText.append("\nFetched ${userList.size} users from server.")

                // Map User to UserEntity
                val userEntities = userList.map { user ->
                    UserEntity(login = user.login, nombre = user.nombre, pass = user.pass)
                }

                try {
                    userDao.clearAllUsers()
                    Log.d("MainActivity", "Cleared old users from DB.")
                    userDao.insertAll(userEntities)
                    Log.d("MainActivity", "Successfully inserted ${userEntities.size} users into DB.")
                    companyCodeStatusText.append("\nStored ${userEntities.size} users in local DB.")
                    Toast.makeText(applicationContext, "Users synchronized to local DB", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error saving users to DB", e)
                    companyCodeStatusText.append("\nError saving users to local DB.")
                    Toast.makeText(applicationContext, "Error saving users locally: ${e.message}", Toast.LENGTH_LONG).show()
                }

            } else {
                Log.e("MainActivity", "Failed to fetch users from server.")
                companyCodeStatusText.append("\nFailed to fetch data from server.")
                Toast.makeText(applicationContext, "Failed to fetch data from server", Toast.LENGTH_LONG).show()
            }
            navigateToLogin() // Navigate after attempting fetch and DB ops
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish() 
    }
}
