package com.example.yourappname.ui.login

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.yourappname.data.local.AppDatabase
import com.example.yourappname.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val userDao by lazy { AppDatabase.getDatabase(applicationContext).userDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
            val username = binding.editTextUsername.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Usuario y contraseña no pueden estar vacíos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }
    }

    private fun performLogin(username: String, passwordFromInput: String) {
        lifecycleScope.launch {
            val user = userDao.getUserByEntrada(username)

            if (user != null) {
                // Comprobar si confirmaN es null o vacío en la BD
                // Si la API devuelve null para CONFIRMA_N, y el usuario no introduce contraseña,
                // podría considerarse un login válido si esa es la lógica de negocio.
                // Aquí asumimos que si user.confirmaN es null/empty, cualquier contraseña (o ninguna) es válida.
                // O, si se requiere que coincida, y es null, la contraseña de input también debe ser vacía/null.

                // Para este ejemplo, vamos a asumir que si user.confirmaN es null o está vacío,
                // el login es exitoso si el usuario TAMBIÉN deja la contraseña vacía.
                // Si user.confirmaN tiene un valor, debe coincidir.

                val passwordInDb = user.confirmaN ?: "" // Tratar null como string vacío para la comparación

                if (passwordInDb == passwordFromInput) {
                    Toast.makeText(this@LoginActivity, "Login exitoso. Bienvenido ${user.nombre ?: username}!", Toast.LENGTH_LONG).show()
                    // Aquí iría la navegación a la pantalla principal de la aplicación
                    // Por ejemplo:
                    // Intent(this@LoginActivity, MainActivity::class.java).also {
                    //     startActivity(it)
                    //     finish()
                    // }
                } else {
                    Toast.makeText(this@LoginActivity, "Contraseña incorrecta.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@LoginActivity, "Usuario no encontrado.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
