package com.biblioapp.ui.fragments

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.biblioapp.api.RetrofitClient
import com.biblioapp.databinding.FragmentAddProductBinding
import com.biblioapp.model.CreateProductRequest
import com.biblioapp.model.CreateProductResponse
import com.biblioapp.model.Product
import com.biblioapp.model.ProductImage
import com.biblioapp.ui.adapter.ImagePreviewAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Fragment para crear/editar productos.
 *
 * - Si se le pasa argumento "product_id" (Int) editará el producto; si no, creará uno nuevo.
 * - Permite seleccionar múltiples imágenes, subirlas y enviar la petición al backend.
 *
 * Requiere:
 * - RetrofitClient.createUploadService(context): UploadService
 * - RetrofitClient.createProductService(context): ProductService
 * - CreateProductRequest/CreateProductResponse y ProductImage model definidos.
 */
class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val selectedImageUris = mutableListOf<Uri>()
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter

    private var editingProductId: Int? = null

    private val pickImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImageUris.clear()
            selectedImageUris.addAll(uris)
            imagePreviewAdapter.notifyDataSetChanged()
            binding.rvImagePreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // leer argumento opcional product_id (AddProductActivity pasa EXTRA_PRODUCT_ID)
        editingProductId = arguments?.getInt(com.biblioapp.ui.AddProductActivity.EXTRA_PRODUCT_ID) ?: 0
        if (editingProductId == 0) editingProductId = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        binding.btnSelectImage.setOnClickListener { pickImages.launch("image/*") }
        binding.btnSubmit.setOnClickListener { submit() }

        // Si estamos editando, cargar producto y prellenar campos
        editingProductId?.let { loadProductForEdit(it) }
    }

    private fun setupRecyclerView() {
        imagePreviewAdapter = ImagePreviewAdapter(selectedImageUris)
        binding.rvImagePreview.adapter = imagePreviewAdapter
        binding.rvImagePreview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun loadProductForEdit(productId: Int) {
        binding.progress.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val service = RetrofitClient.createProductService(requireContext())
                val product: Product = withContext(Dispatchers.IO) { service.getProduct(productId) }

                // Prefill
                binding.etTitle.setText(product.title)
                binding.etAuthor.setText(product.author)
                binding.etGenre.setText(product.genre)
                binding.etDescription.setText(product.description)
                binding.etPrice.setText(product.price?.toInt()?.toString() ?: "")
                binding.etStock.setText(product.stock?.toString() ?: "")

                // si el producto ya tiene imágenes, no las añadimos como URIs (no accesible localmente)
                // pero mostramos la primera URL en un field opcional (no incluido ahora). Si quieres,
                // agregamos un campo etImageUrl y lo rellenamos con product.image?.firstOrNull()?.url
            } catch (e: Exception) {
                Log.e("AddProductFragment", "Error cargando producto para edición", e)
                Toast.makeText(requireContext(), "No se pudo cargar el producto: ${e.message}", Toast.LENGTH_LONG).show()
                // opcional: cerrar actividad si no se puede cargar
                requireActivity().finish()
            } finally {
                binding.progress.visibility = View.GONE
            }
        }
    }

    private fun submit() {
        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val author = binding.etAuthor.text?.toString()?.trim().orEmpty()
        val genre = binding.etGenre.text?.toString()?.trim().orEmpty()
        val description = binding.etDescription.text?.toString()?.trim()
        val price = binding.etPrice.text?.toString()?.trim()?.toDoubleOrNull()
        val stock = binding.etStock.text?.toString()?.trim()?.toIntOrNull()

        if (title.isBlank() || author.isBlank() || genre.isBlank() || price == null || stock == null) {
            Toast.makeText(requireContext(), "Todos los campos obligatorios deben estar completos", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Subida de imágenes (si se seleccionaron)
                val uploadedImages = mutableListOf<ProductImage>()
                if (selectedImageUris.isNotEmpty()) {
                    val tasks = selectedImageUris.map { uri ->
                        async(Dispatchers.IO) { uploadImage(uri) }
                    }
                    val results = tasks.awaitAll()
                    uploadedImages.addAll(results.filterNotNull())
                }

                val productService = RetrofitClient.createProductService(requireContext())

                if (editingProductId != null) {
                    // UPDATE: build map con campos a actualizar
                    val body = mutableMapOf<String, Any>()
                    body["title"] = title
                    body["author"] = author
                    body["genre"] = genre
                    description?.let { body["description"] = it }
                    body["price"] = price
                    body["stock"] = stock
                    if (uploadedImages.isNotEmpty()) {
                        // enviar lista de maps con url si tu backend lo espera así
                        val imgs = uploadedImages.map { mapOf("url" to (it.url ?: it.path ?: "")) }
                        body["image"] = imgs
                    }

                    withContext(Dispatchers.IO) { productService.updateProduct(editingProductId!!, body) }
                    Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
                    // informar al caller y cerrar
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                } else {
                    // CREATE
                    val imageList: List<ProductImage>? = if (uploadedImages.isNotEmpty()) uploadedImages else null
                    val req = CreateProductRequest(
                        title = title,
                        author = author,
                        genre = genre,
                        description = description,
                        price = price,
                        stock = stock,
                        image = imageList
                    )

                    val resp: CreateProductResponse = withContext(Dispatchers.IO) { productService.createProduct(req) }

                    // Manejo flexible: si el backend devuelve wrapper o directamente Product, ajusta según sea necesario.
                    if (resp != null && resp.product != null) {
                        Toast.makeText(requireContext(), "Producto creado exitosamente", Toast.LENGTH_SHORT).show()
                        // notificar y cerrar host (opcional) o limpiar formulario
                        requireActivity().setResult(Activity.RESULT_OK)
                        requireActivity().finish()
                    } else {
                        // Si tu API devuelve directamente Product, la llamada arriba fallará al parsear; en ese caso
                        // adapta ProductService.createProduct para devolver Product y modifica aquí la comprobación.
                        Log.w("AddProductFragment", "Respuesta inesperada al crear producto: $resp")
                        Toast.makeText(requireContext(), "Producto creado (respuesta inesperada)", Toast.LENGTH_SHORT).show()
                        requireActivity().setResult(Activity.RESULT_OK)
                        requireActivity().finish()
                    }
                }
            } catch (e: Exception) {
                Log.e("AddProductFragment", "Error al enviar producto", e)
                Toast.makeText(requireContext(), "Error al crear/actualizar: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progress.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): ProductImage? = withContext(Dispatchers.IO) {
        try {
            val cr = requireContext().contentResolver
            val input = cr.openInputStream(uri) ?: throw IOException("No se pudo abrir stream para URI: $uri")
            val bytes = input.use { it.readBytes() }

            val mime = cr.getType(uri) ?: "image/jpeg"
            val requestBody = bytes.toRequestBody(mime.toMediaTypeOrNull())
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val part = MultipartBody.Part.createFormData("content", fileName, requestBody)

            val uploadService = RetrofitClient.createUploadService(requireContext())
            val images: List<ProductImage> = uploadService.uploadImage(part)
            images.firstOrNull()
        } catch (e: Exception) {
            Log.e("AddProductFragment", "Falló subida imagen: $uri", e)
            null
        }
    }

    private fun clearForm() {
        binding.etTitle.text?.clear()
        binding.etAuthor.text?.clear()
        binding.etGenre.text?.clear()
        binding.etDescription.text?.clear()
        binding.etPrice.text?.clear()
        binding.etStock.text?.clear()
        selectedImageUris.clear()
        imagePreviewAdapter.notifyDataSetChanged()
        binding.rvImagePreview.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}