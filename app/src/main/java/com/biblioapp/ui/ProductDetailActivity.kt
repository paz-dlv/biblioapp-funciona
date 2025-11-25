package com.biblioapp.ui

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.biblioapp.databinding.ActivityProductDetailBinding
import com.biblioapp.model.Product
import com.biblioapp.ui.adapter.ImageSliderAdapter

class ProductDetailActivity : AppCompatActivity() { // La clase hereda de AppCompatActivity.

    // Declara una variable para el ViewBinding que se inicializará más tarde (lateinit).
    private lateinit var binding: ActivityProductDetailBinding

    // Este métodoo se llama cuando la actividad se crea por primera vez.
    override fun onCreate(savedInstanceState: Bundle?) {
        // Llama al métodoo onCreate de la clase padre.
        super.onCreate(savedInstanceState)
        // Infla el layout usando ViewBinding y asigna la referencia a nuestra variable 'binding'.
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        // Establece la vista de contenido de la actividad a la vista raíz de nuestro layout inflado.
        setContentView(binding.root)

        // --- HABILITAR EL BOTÓN DE "VOLVER ATRÁS" ---
        // Accede a la barra de acción (ActionBar) de soporte y, si existe (?.), habilita la visualización del botón "home".
        // El botón "home" se muestra como una flecha de retroceso (<-) por defecto.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // --- RECUPERAR EL PRODUCTO DEL INTENT ---
        // Verifica si la versión de Android es TIRAMISU (API 33) o superior.
        val product = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Si es Android 13+, usa la forma moderna y segura de obtener un objeto 'Serializable' del Intent.
            // Se especifica la clave "PRODUCT_EXTRA" y la clase del objeto esperado (Product::class.java).
            intent.getSerializableExtra("PRODUCT_EXTRA", Product::class.java)
        } else {
            // Si es una versión anterior a Android 13, usa el métodoo obsoleto.
            // Se suprime la advertencia de obsolescencia (@Suppress("DEPRECATION")).
            // Se hace un cast seguro (as? Product) al tipo de dato que esperamos.
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("PRODUCT_EXTRA") as? Product
        }

        // Usa un bloque 'let' con el operador de llamada segura (?.), que solo se ejecuta si 'product' no es nulo.
        product?.let {
            // Si el producto se recuperó exitosamente, llama al métodoo setupUI para configurar la pantalla.
            setupUI(it)
        }
    }

    // Este métodoo configura todos los elementos de la interfaz de usuario con los datos del producto.
    private fun setupUI(product: Product) {
        // --- TÍTULO DE LA ACTIVIDAD ---
        // Establece el título de la barra de acción (ActionBar) con el nombre del producto.
        title = product.title

        // --- CONFIGURAR LOS TEXTOS ---
        // Asigna el nombre del producto al TextView correspondiente.
        binding.tvProductName.text = "Nombre: ${product.title}"
        // Asigna el precio. Usa 'let' para formatear el texto solo si el precio no es nulo. Si es nulo, muestra un texto alternativo.
        binding.tvProductPrice.text = product.price?.let { "Precio: $it" } ?: "Precio no disponible"
        // Asigna la descripción. Si es nula, usa el operador Elvis (?:) para mostrar "Sin descripción.".
        binding.tvProductDescription.text = "Descripción: ${product.description ?: "Sin descripción."}" // Pequeña mejora aquí también
        // Asigna el stock usando una plantilla de string.
        binding.tvProductStock.text = "Stock: ${product.stock ?: "No especificado"}" // Pequeoñ mejora

        // --- CONFIGURAR EL CARRUSEL DE IMÁGENES ---
        // Usa 'let' para ejecutar este bloque solo si la lista de imágenes del producto no es nula.
        product.image?.let { imageList ->
            // ¡¡¡CORRECCIÓN CLAVE AQUÍ!!!
            // Usamos 'mapNotNull' en lugar de 'map'.
            // 'mapNotNull' transformará la lista a una lista de URLs y, al mismo tiempo,
            // filtrará y descartará cualquier URL que sea 'null'.
            // El resultado 'imageUrls' será de tipo 'List<String>', que es lo que el adaptador espera.
            val imageUrls = imageList.mapNotNull { it.url }

            // Ahora nos aseguramos de que el carrusel solo se configure si realmente hay URLs válidas.
            if (imageUrls.isNotEmpty()) {
                // Crea una nueva instancia de nuestro ImageSliderAdapter, pasándole la lista de URLs (sin nulos).
                val adapter = ImageSliderAdapter(imageUrls)
                // Asigna el adaptador recién creado al ViewPager2 en nuestro layout.
                binding.imageViewPager.adapter = adapter
            }
        }
    }

    // --- MANEJAR EL CLIC EN EL BOTÓN "VOLVER ATRÁS" ---
    // Este métodoo se llama automáticamente cuando se presiona un botón de la barra de acción.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Comprueba si el ID del item presionado es 'android.R.id.home'.
        // Este es el ID estándar para el botón de la flecha de retroceso (Up button).
        if (item.itemId == android.R.id.home) {
            // Si es el botón de retroceso, finaliza la actividad actual.
            // Esto destruye ProductDetailActivity y regresa a la actividad anterior en la pila (HomeActivity).
            finish()
            // Devuelve 'true' para indicar que hemos manejado el evento de clic con éxito.
            return true
        }
        // Si el botón presionado no es el que nos interesa, pasamos el evento al métodoo de la clase padre para que lo maneje.
        return super.onOptionsItemSelected(item)
    }
}