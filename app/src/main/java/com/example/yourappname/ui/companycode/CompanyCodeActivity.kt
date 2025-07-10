package com.example.yourappname.ui.companycode

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.yourappname.data.local.AppDatabase
import com.example.yourappname.data.local.UserEntity
import com.example.yourappname.data.model.UserData
import com.example.yourappname.data.network.RetrofitClient
import com.example.yourappname.databinding.ActivityCompanyCodeBinding
import com.example.yourappname.ui.login.LoginActivity
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class CompanyCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyCodeBinding
    private val companyCodeFileName = "company_code.txt"
    private val userDao by lazy { AppDatabase.getDatabase(applicationContext).userDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCompanyCode()

        binding.buttonSaveCompanyCode.setOnClickListener {
            val companyCode = binding.editTextCompanyCode.text.toString().trim()
            if (companyCode.isNotEmpty()) {
                saveCompanyCode(companyCode)
                Toast.makeText(this, "Código de empresa guardado.", Toast.LENGTH_SHORT).show()
                fetchDataAndProceed(companyCode)
            } else {
                binding.editTextCompanyCode.error = "El código no puede estar vacío"
                Toast.makeText(this, "Por favor, introduce un código de empresa.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchDataAndProceed(companyCode: String) {
        // Podríamos añadir un ProgressBar aquí para feedback visual
        binding.buttonSaveCompanyCode.isEnabled = false // Deshabilitar botón mientras se carga

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getUsers(companyCode)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.table != null) {
                        val usersToSave = apiResponse.table.mapNotNull { userData ->
                            // Solo guardamos si ENTRADA no es nulo, ya que es nuestra PK
                            if (userData.entrada != null) {
                                UserEntity(
                                    entrada = userData.entrada,
                                    confirmaN = userData.confirmaN,
                                    nombre = userData.nombre
                                )
                            } else {
                                null
                            }
                        }
                        if (usersToSave.isNotEmpty()) {
                            userDao.deleteAllUsers() // Borramos datos antiguos antes de insertar nuevos
                            userDao.insertAll(usersToSave)
                            Toast.makeText(this@CompanyCodeActivity, "Datos sincronizados correctamente.", Toast.LENGTH_SHORT).show()
                            navigateToLogin()
                        } else {
                            Toast.makeText(this@CompanyCodeActivity, "No se encontraron datos de usuario válidos para guardar.", Toast.LENGTH_LONG).show()
                            binding.buttonSaveCompanyCode.isEnabled = true
                        }
                    } else {
                        Toast.makeText(this@CompanyCodeActivity, "Respuesta de la API vacía o malformada.", Toast.LENGTH_LONG).show()
                        Log.e("CompanyCodeActivity", "Respuesta vacía o malformada: ${response.body()}")
                        binding.buttonSaveCompanyCode.isEnabled = true
                    }
                } else {
                    Toast.makeText(this@CompanyCodeActivity, "Error de API: ${response.code()} - ${response.message()}", Toast.LENGTH_LONG).show()
                    Log.e("CompanyCodeActivity", "Error API: ${response.code()} - ${response.errorBody()?.string()}")
                    binding.buttonSaveCompanyCode.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(this@CompanyCodeActivity, "Error de red o procesamiento: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("CompanyCodeActivity", "Excepción al obtener datos", e)
                binding.buttonSaveCompanyCode.isEnabled = true
            }
        }
    }

    private fun navigateToLogin() {
        Intent(this, LoginActivity::class.java).also {
            startActivity(it)
            finish() // Finaliza esta actividad para que el usuario no pueda volver con el botón "atrás"
        }
    }

    private fun saveCompanyCode(code: String) {
        try {
            // Guardar en almacenamiento interno específico de la app
            openFileOutput(companyCodeFileName, MODE_PRIVATE).use {
                it.write(code.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar el código de empresa.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCompanyCode() {
        try {
            if (File(filesDir, companyCodeFileName).exists()) {
                val code = openFileInput(companyCodeFileName).bufferedReader().useLines { lines ->
                    lines.joinToString("\n")
                }
                binding.editTextCompanyCode.setText(code)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // No mostrar Toast si el archivo simplemente no existe al inicio
            if (File(filesDir, companyCodeFileName).exists()) {
                 Toast.makeText(this, "Error al cargar el código de empresa.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
