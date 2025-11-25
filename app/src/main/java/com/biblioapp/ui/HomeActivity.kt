package com.biblioapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.biblioapp.api.TokenManager
import com.biblioapp.databinding.ActivityHomeBinding
import com.biblioapp.ui.fragments.AddProductFragment
import com.biblioapp.ui.fragments.ProductsFragment
import com.biblioapp.ui.fragments.ProfileFragment

class HomeActivity : AppCompatActivity() { // Declaramos la Activity Home, que gestiona los fragments

    private lateinit var binding: ActivityHomeBinding // Referencia al ViewBinding para acceder a vistas
    private lateinit var tokenManager: TokenManager // Manejador de token y datos de usuario

    override fun onCreate(savedInstanceState: Bundle?) { // Métodoo de ciclo de vida: se llama al crear la Activity
        super.onCreate(savedInstanceState) // Llamamos a la implementación base
        binding = ActivityHomeBinding.inflate(layoutInflater) // Inflamos el layout a través de ViewBinding
        setContentView(binding.root) // Establecemos la vista raíz del binding como contenido de la Activity

        tokenManager = TokenManager(this) // Inicializamos el TokenManager con el contexto de la Activity

        // Cargamos inicialmente el fragmento de Productos
        replaceFragment(ProductsFragment()) // Reemplazamos el contenedor por el fragmento de productos

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.biblioapp.R.id.nav_products -> replaceFragment(ProductsFragment()) // Ir a productos
                com.biblioapp.R.id.nav_add -> replaceFragment(AddProductFragment()) // Ir a agregar producto
                com.biblioapp.R.id.nav_profile -> replaceFragment(ProfileFragment()) // Ir a perfil
            }
            true // Devolvemos true para indicar que el evento fue manejado
        }
    }

    private fun replaceFragment(fragment: Fragment) { // Función auxiliar para reemplazar el fragment actual
        supportFragmentManager.beginTransaction() // Iniciamos una transacción de fragmentos
            .replace(binding.fragmentContainer.id, fragment) // Reemplazamos el contenedor con el fragmento dado
            .commit() // Confirmamos la transacción
    }
}