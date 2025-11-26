package com.biblioapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.biblioapp.R
import com.biblioapp.api.RetrofitClient
import com.biblioapp.api.TokenManager
import com.biblioapp.model.RegisterUserRequest
import com.biblioapp.model.User
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val nameEditText = findViewById<EditText>(R.id.etName)
        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val addressEditText = findViewById<EditText>(R.id.etShippingAddress)
        val phoneEditText = findViewById<EditText>(R.id.etPhone)
        val signUpButton = findViewById<Button>(R.id.btnSignUp)

        val tokenManager = TokenManager(this) // para guardar role/email si vienen

        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()

            if (!validateFields(name, email, password, address, phone)) return@setOnClickListener

            val request = RegisterUserRequest(
                name = name,
                email = email,
                password = password,
                shipping_address = address,
                phone = phone
            )

            val authService = RetrofitClient.createAuthService(this)

            // Usamos lifecycleScope y llamamos al método suspend directamente
            lifecycleScope.launch {
                try {
                    val response: Response<User> = authService.signUp(request)

                    Log.d("SignUp", "Response body: ${response.body()}")
                    Log.d("SignUp", "Response error: ${response.errorBody()?.string()}")

                    if (response.isSuccessful && response.body() != null) {
                        val createdUser = response.body()!!

                        // Guardar role si el backend lo devuelve (minimo cambio)
                        try {
                            createdUser.role?.let { role ->
                                tokenManager.saveRole(role)
                            }
                        } catch (t: Throwable) {
                            // Si TokenManager aún no tiene saveRole en tu versión, evitamos crash.
                            Log.w("SignUp", "No se pudo guardar role en TokenManager: ${t.message}")
                        }

                        // Opcional: guardar el email/nombre en SharedPreferences (compatible con TokenManager existente)
                        try {
                            val prefs = getSharedPreferences("session", MODE_PRIVATE)
                            prefs.edit().putString("user_name", createdUser.name).putString("user_email", createdUser.email).apply()
                        } catch (t: Throwable) {
                            Log.w("SignUp", "No se pudo guardar user info en prefs: ${t.message}")
                        }

                        Toast.makeText(
                            this@SignUpActivity,
                            "Usuario registrado exitosamente. Ahora puedes iniciar sesión.",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = parseErrorMessage(errorBody)
                        Toast.makeText(this@SignUpActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@SignUpActivity, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("SignUp", "Failure: ${e.message}")
                }
            }
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrEmpty()) return "Error al registrar usuario."
        return try {
            val json = JSONObject(errorBody)
            if (json.has("message")) json.getString("message") else "Error al registrar usuario."
        } catch (e: Exception) {
            "Error al registrar usuario."
        }
    }

    private fun validateFields(name: String, email: String, password: String, address: String, phone: String): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email no válido.", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}