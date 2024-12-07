package com.example.eventosapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.eventosapp.databinding.ActivityLoginBinding
import com.example.eventosapp.models.User  // Importación necesaria para la clase User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase  // Importación necesaria para Firebase Database

class LoginActivity : AppCompatActivity() {
    // Declaramos nuestras variables a nivel de clase
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    // Creamos una propiedad para acceder más fácilmente al contenido incluido
    private val contentBinding get() = binding.contentLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializamos el binding y establecemos el contenido de la vista
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializamos Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Verificamos si hay una sesión activa
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        setupLoginButton()
        setupRegisterButton()
    }

    private fun setupLoginButton() {
        // Accedemos a las vistas a través del contentBinding
        with(contentBinding) {
            buttonLogin.setOnClickListener {
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()

                if (validateInputs(email, password)) {
                    performLogin(email, password)
                }
            }
        }
    }

    private fun setupRegisterButton() {
        with(contentBinding) {
            buttonRegister.setOnClickListener {
                val email = editTextEmail.text.toString()
                val password = editTextPassword.text.toString()

                if (validateInputs(email, password)) {
                    performRegistration(email, password)
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun performLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun performRegistration(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Guardamos la información adicional del usuario
                    auth.currentUser?.uid?.let { userId ->
                        saveUserToDatabase(userId, email)
                    }
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Error al registrarse", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToDatabase(userId: String, email: String) {
        val database = FirebaseDatabase.getInstance().reference
        val user = User(userId, email)
        database.child("users").child(userId).setValue(user)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}