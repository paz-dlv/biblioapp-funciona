package com.biblioapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.biblioapp.api.RetrofitClient
import com.biblioapp.databinding.FragmentProductsBinding
import com.biblioapp.model.Product
import com.biblioapp.ui.adapter.ProductAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductsFragment : Fragment() { // Fragment que lista y filtra productos

    private var _binding: FragmentProductsBinding? = null // Backing field opcional para ViewBinding
    private val binding get() = _binding!! // Exponemos binding no-null dentro del ciclo de vida de la vista

    private lateinit var adapter: ProductAdapter // Adaptador de productos
    private var allProducts: List<Product> = emptyList() // Cache local de todos los productos

    override fun onCreateView( // Inflamos la vista del fragment
        inflater: LayoutInflater, // Inflater para convertir XML en Views
        container: ViewGroup?, // Contenedor padre
        savedInstanceState: Bundle? // Estado guardado
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false) // Inflamos con ViewBinding
        return binding.root // Devolvemos la raíz de la vista
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // Vista creada: configuramos UI y carga
        super.onViewCreated(view, savedInstanceState)
        setupRecycler() // Preparamos RecyclerView
        setupSearch() // Configuramos barra de búsqueda
        loadProducts() // Cargamos datos desde API
    }

    private fun setupRecycler() { // Inicializa RecyclerView con layout manager y adaptador
        adapter = ProductAdapter() // Instanciamos adaptador simple
        binding.recyclerProducts.layoutManager = LinearLayoutManager(requireContext()) // Lista vertical
        binding.recyclerProducts.adapter = adapter // Asociamos adaptador
    }

    private fun setupSearch() { // Configura callbacks de búsqueda
        // Usamos el SearchView de AppCompat (asegúrate que el view en el layout es androidx.appcompat.widget.SearchView)
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { // Al enviar búsqueda
                filter(query) // Aplicamos filtro
                return true // Indicamos que manejamos el evento
            }

            override fun onQueryTextChange(newText: String?): Boolean { // Mientras cambia el texto
                filter(newText) // Aplicamos filtro en tiempo real
                return true
            }
        })
    }

    private fun filter(query: String?) { // Filtra lista local por nombre
        val q = query?.trim()?.lowercase().orEmpty() // Normalizamos query a minúsculas
        if (q.isBlank()) { // Si vacío, mostramos todos
            adapter.updateData(allProducts) // Reset
        } else {
            adapter.updateData(allProducts.filter { it.title.lowercase().contains(q) }) // Filtro simple
        }
    }

    private fun loadProducts() { // Carga productos desde API con corrutinas
        // Corrutina para carga de productos
        viewLifecycleOwner.lifecycleScope.launch { // Lanzamos en el ciclo de vida del fragment
            try {
                val service = RetrofitClient.createProductService(requireContext()) // Obtenemos servicio de productos
                val products = withContext(Dispatchers.IO) { // Ejecutamos llamada en hilo de IO
                    service.getProducts() // GET /products
                }
                allProducts = products // Guardamos lista completa
                adapter.updateData(products) // Actualizamos la UI
            } catch (e: Exception) {
                // Podrías mostrar un Snackbar/Toast en caso de error
            }
        }
    }

    override fun onDestroyView() { // Limpieza de ViewBinding para evitar memory leaks
        super.onDestroyView()
        _binding = null // Nulificamos binding cuando se destruye la vista
    }
}