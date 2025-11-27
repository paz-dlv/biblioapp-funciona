package com.biblioapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.biblioapp.api.RetrofitClient
import com.biblioapp.api.TokenManager
import com.biblioapp.databinding.ActivityMainBinding
import com.biblioapp.model.LoginRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() { // Activity principal de login

    private lateinit var binding: ActivityMainBinding // Referencia a ViewBinding para acceder a vistas
    private lateinit var tokenManager: TokenManager // Manejador de sesión/token del usuario

    override fun onCreate(savedInstanceState: Bundle?) { // Ciclo de vida: creación de la Activity
        super.onCreate(savedInstanceState) // Llamamos al métodoo base
        binding = ActivityMainBinding.inflate(layoutInflater) // Inflamos el layout con ViewBinding
        setContentView(binding.root) // Establecemos el contenido de la Activity

        tokenManager = TokenManager(this) // Inicializamos TokenManager con contexto

        // === NUEVO: si ya hay sesión, nos aseguramos de tener el role guardado antes de ir a la vista correspondiente ===
        if (tokenManager.isLoggedIn()) { // Consultamos si hay token guardado
            val existingRole = tokenManager.getRole()
            if (!existingRole.isNullOrBlank()) {
                // Ya tenemos role, navegamos según rol
                navigateByRole(existingRole)
                return
            } else {
                // Si no tenemos role, intentamos obtener el perfil (requiere token)
                lifecycleScope.launch {
                    try {
                        val privateAuthService = RetrofitClient.createAuthService(this@MainActivity, requiresAuth = true)
                        val profile = withContext(Dispatchers.IO) {
                            privateAuthService.getMe()
                        }
                        // DEBUG: muestra el profile recibido
                        Log.d("MainActivity", "DEBUG userProfile (onStart): $profile")

                        // Guardar role y user id si vienen en el profile
                        profile.role?.let { tokenManager.saveRole(it) }
                        profile.id?.let {
                            tokenManager.saveUserId(it)
                            Log.d("MainActivity", "Saved user id (onStart): ${tokenManager.getUserId()}")
                        }
                        navigateByRole(profile.role)
                    } catch (e: Exception) {
                        Log.w("MainActivity", "No se pudo obtener role al iniciar: ${e.message}")
                        // fallback a CLIENT si falla
                        tokenManager.saveRole("CLIENT")
                        navigateByRole("CLIENT")
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
                    val publicAuthService = RetrofitClient.createAuthService(this@MainActivity)
                    val loginResponse = withContext(Dispatchers.IO) {
                        publicAuthService.login(LoginRequest(email = email, password = password))
                    }

                    // --- PASO CLAVE: GUARDADO DEL TOKEN ---
                    val authToken = loginResponse.authToken

                    // Guardamos token temporalmente para que el interceptor lo use en la siguiente llamada.
                    tokenManager.saveAuth(authToken, "", "", null)

                    // --- FASE 2: OBTENCIÓN DE DATOS (usando el servicio PRIVADO) ---
                    val privateAuthService = RetrofitClient.createAuthService(this@MainActivity, requiresAuth = true)
                    val userProfile = withContext(Dispatchers.IO) {
                        privateAuthService.getMe() // obtenemos profile (incluye role)
                    }

                    // DEBUG: muestra el profile recibido en el login flow
                    Log.d("MainActivity", "DEBUG userProfile (onLogin): $userProfile")

                    // --- FASE 3: GUARDADO COMPLETO Y FORMAL ---
                    // Usamos la versión unificada de saveAuth para guardar token + user info + userId en un solo paso
                    tokenManager.saveAuth(
                        token = authToken,
                        userName = userProfile.name ?: "",
                        userEmail = userProfile.email ?: "",
                        role = userProfile.role,
                        userId = userProfile.id
                    )

                    // (No es necesario llamar a saveUserId o saveRole por separado aquí)

                    // --- FASE 4: BIENVENIDA Y NAVEGACIÓN SEGÚN ROL ---
                    Log.d("MainActivity", "Saved user id (onLogin): ${tokenManager.getUserId()}")
                    Toast.makeText(this@MainActivity, "¡Bienvenido, ${userProfile.name}!", Toast.LENGTH_SHORT).show()
                    navigateByRole(userProfile.role)

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

    /**
     * Navega a la Activity correspondiente según el role.
     * - ADMIN -> AdminActivity
     * - CLIENT / otro -> HomeActivity
     *
     * Limpia la pila de actividades para que no se pueda volver al login.
     */
    private fun navigateByRole(roleRaw: String?) {
        val role = roleRaw?.trim()?.uppercase()
        val destClass = if (role == "ADMIN" || role == "ROLE_ADMIN") {
            AdminActivity::class.java
        } else {
            HomeActivity::class.java
        }
        val intent = Intent(this, destClass)
        // Limpiamos la pila para que no puedan volver al login con back
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Deprecated goToHome kept for reference (no longer used)
    @Suppress("unused")
    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}