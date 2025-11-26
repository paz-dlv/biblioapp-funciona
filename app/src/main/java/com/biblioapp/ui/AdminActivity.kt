package com.biblioapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.biblioapp.R
import com.biblioapp.api.TokenManager
import com.biblioapp.databinding.ActivityAdminBinding
import com.biblioapp.ui.fragments.AdminDashboardFragment
import com.biblioapp.ui.fragments.ManageProductsFragment
import com.biblioapp.ui.fragments.OrdersFragment
import com.biblioapp.ui.fragments.UsersFragment

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        // Toolbar
        setSupportActionBar(binding.toolbarAdmin)
        supportActionBar?.apply {
            title = getString(R.string.admin_panel_title) // crea string si quieres, o usa texto directo
            setDisplayShowHomeEnabled(false)
        }

        // mostrar email/rol en subtitle (si están)
        val email = tokenManager.getUserEmail()
        val role = tokenManager.getRole()
        binding.toolbarAdmin.subtitle = listOfNotNull(email, role?.let { "Rol: $it" }).joinToString(" • ")

        // Navegación inicial
        if (savedInstanceState == null) {
            openFragment(AdminDashboardFragment())
            binding.bottomNavAdmin.selectedItemId = R.id.nav_dashboard
        }

        // Bottom navigation
        binding.bottomNavAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> openFragment(AdminDashboardFragment())
                R.id.nav_products -> openFragment(ManageProductsFragment())
                R.id.nav_orders -> openFragment(OrdersFragment())
                R.id.nav_users -> openFragment(UsersFragment())
                else -> false
            }
            true
        }

        // FAB contextual
        binding.fabAdminAction.setOnClickListener {
            val current = supportFragmentManager.findFragmentById(binding.fragmentContainer.id)
            if (current is ManageProductsFragment) {
                current.onAddProductClicked()
            } else {
                // por defecto llevar a productos
                binding.bottomNavAdmin.selectedItemId = R.id.nav_products
                openFragment(ManageProductsFragment())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_admin_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                tokenManager.clear()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}