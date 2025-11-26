package com.biblioapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.biblioapp.R
import com.biblioapp.api.RetrofitClient
import com.biblioapp.api.TokenManager
import com.biblioapp.databinding.ActivityMainBinding
import com.biblioapp.model.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.java

private lateinit var binding: ActivityMainBinding
private lateinit var tokenManager: TokenManager

class MainActivity : AppCompatActivity() { // Activity principal de login

    private lateinit var binding: ActivityMainBinding // Referencia a ViewBinding para acceder a vistas
    private lateinit var tokenManager: TokenManager // Manejador de sesión/token del usuario

    override fun onCreate(savedInstanceState: Bundle?) { // Ciclo de vida: creación de la Activity
        super.onCreate(savedInstanceState) // Llamamos al métodoo base
        binding = ActivityMainBinding.inflate(layoutInflater) // Inflamos el layout con ViewBinding
        setContentView(binding.root) // Establecemos el contenido de la Activity

        tokenManager = TokenManager(this) // Inicializamos TokenManager con contexto

        // === NUEVO: si ya hay sesión, nos aseguramos de tener el role guardado antes de ir a Home ===
        if (tokenManager.isLoggedIn()) { // Consultamos si hay token guardado
            val existingRole = tokenManager.getRole()
            if (!existingRole.isNullOrBlank()) {
                // Ya tenemos role, podemos seguir directamente
                goToHome()
                return
            } else {
                // Si no tenemos role, intentamos obtener el perfil (requiere token)
                lifecycleScope.launch {
                    try {
                        val privateAuthService = RetrofitClient.createAuthService(this@MainActivity, requiresAuth = true)
                        val profile = withContext(Dispatchers.IO) {
                            privateAuthService.getMe()
                        }
                        profile.role?.let { tokenManager.saveRole(it) }
                    } catch (e: Exception) {
                        Log.w("MainActivity", "No se pudo obtener role al iniciar: ${e.message}")
                        // fallback a CLIENT si falla
                        tokenManager.saveRole("CLIENT")
                    } finally {
                        goToHome()
                    }
                }
                return // esperamos al launch para navegar
            }
        }

        // Listener para el link de registro:
        binding.tvSignUpLink.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Si ya hay sesión, vamos directo a Home
        if (tokenManager.isLoggedIn()) { // Consultamos si hay token guardado
            goToHome() // Navegamos a Home
            return // Terminamos onCreate para no mostrar login
        }

        binding.btnLogin.setOnClickListener { // Click en botón Login
            val email = binding.etEmail.text?.toString()?.trim().orEmpty() // Obtenemos email
            val password = binding.etPassword.text?.toString()?.trim().orEmpty() // Obtenemos password

            if (email.isBlank() || password.isBlank()) { // Validación simple
                Toast.makeText(this, "Completa email y password", Toast.LENGTH_SHORT).show() // Feedback
                return@setOnClickListener // No seguimos si faltan datos
            }

            // Mostramos progreso
            binding.progress.visibility = View.VISIBLE // Indicador visible
            binding.btnLogin.isEnabled = false // Bloqueamos botón para evitar múltiples clics

            // Corrutina para llamar a la API de login
            lifecycleScope.launch {
                try {
                    // --- FASE 1: LOGIN (usando el servicio PÚBLICO) ---
                    // Llamamos a createAuthService sin el segundo parámetro (o con 'false'),
                    // para obtener un servicio sin token.
                    val publicAuthService = RetrofitClient.createAuthService(this@MainActivity)
                    val loginResponse = withContext(Dispatchers.IO) {
                        publicAuthService.login(LoginRequest(email = email, password = password))
                    }

                    // --- PASO CLAVE: GUARDADO DEL TOKEN ---
                    val authToken = loginResponse.authToken

                    // --- PASO CLAVE: GUARDADO TEMPORAL MANUAL ---
                    // Guardamos el token en SharedPreferences para que la siguiente llamada lo encuentre.
                    getSharedPreferences("session", Context.MODE_PRIVATE).edit().apply {
                        putString("jwt_token", authToken)
                        apply()
                    }

                    // --- FASE 2: OBTENCIÓN DE DATOS (usando el servicio PRIVADO) ---
                    // Ahora llamamos a createAuthService con 'requiresAuth = true'.
                    // Esto creará un servicio que SÍ incluye el interceptor con el token.
                    val privateAuthService = RetrofitClient.createAuthService(this@MainActivity, requiresAuth = true)
                    val userProfile = withContext(Dispatchers.IO) {
                        privateAuthService.getMe() // ¡Esta llamada ahora funcionará!
                    }

                    // --- FASE 3: GUARDADO COMPLETO Y FORMAL ---
                    tokenManager.saveAuth(
                        token = authToken,
                        userName = userProfile.name ?: "",
                        userEmail = userProfile.email ?: "",
                        role = userProfile.role
                    )
                    // === NUEVO: guardar role si viene en el profile ===
                    userProfile.role?.let { tokenManager.saveRole(it) }

                    // --- FASE 4: BIENVENIDA Y NAVEGACIÓN ---
                    Toast.makeText(this@MainActivity, "¡Bienvenido, ${userProfile.name}!", Toast.LENGTH_SHORT).show()
                    goToHome() // Navegamos a Home

                } catch (e: Exception) {
                    Log.e("MainActivity", "Login o GetProfile error", e)
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    tokenManager.clear() // Si falla, limpiamos el token
                } finally {
                    binding.progress.visibility = View.GONE // Indicador invisible
                    binding.btnLogin.isEnabled = true
                }
            }
        }
    }

    private fun goToHome() { // Navegar a la pantalla de Home
        val intent = Intent(this, HomeActivity::class.java) // Creamos el Intent explícito
        startActivity(intent) // Lanzamos la nueva Activity
        finish() // Cerramos la Activity actual para no volver con back
    }
}