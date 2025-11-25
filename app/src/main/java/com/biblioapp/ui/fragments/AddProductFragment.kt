package com.biblioapp.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.test.isEnabled
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.biblioapp.api.RetrofitClient
import com.biblioapp.databinding.FragmentAddProductBinding
import com.biblioapp.model.CreateProductRequest
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



class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val selectedImageUris = mutableListOf<Uri>()
    private lateinit var imagePreviewAdapter: ImagePreviewAdapter

    private val pickImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImageUris.clear()
            selectedImageUris.addAll(uris)
            imagePreviewAdapter.notifyDataSetChanged()
            binding.rvImagePreview.visibility = View.VISIBLE
        }
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
    }

    private fun setupRecyclerView() {
        imagePreviewAdapter = ImagePreviewAdapter(selectedImageUris)
        binding.rvImagePreview.adapter = imagePreviewAdapter
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
                // FASE 1: Subida de imágenes
                val upload = mutableListOf<ProductImage>()
                if (selectedImageUris.isNotEmpty()) {
                    val uploadTasks = selectedImageUris.map { uri ->
                        async(Dispatchers.IO) { uploadImage(uri) }
                    }
                    val results = uploadTasks.awaitAll()
                    upload.addAll(results.filterNotNull())
                }

                // FASE 2: Creación del producto
                val service = RetrofitClient.createProductService(requireContext())
                val imageList: List<ProductImage>? = if (upload.isNotEmpty()) upload else null

                val req = CreateProductRequest(
                    title = title,
                    author = author,
                    genre = genre,
                    description = description,
                    price = price,
                    stock = stock,
                    image = imageList
                )

                Log.d("AddProductFragment", "Enviando petición para crear producto: $req")
                val resp = withContext(Dispatchers.IO) { service.createProduct(req) }
                if (resp != null) {
                    Toast.makeText(requireContext(), "Producto creado exitosamente", Toast.LENGTH_SHORT).show()
                    clearForm()
                } else {
                    Toast.makeText(requireContext(), "Error: No se recibió confirmación del servidor", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("AddProductFragment", "Error al crear el producto", e)
                Toast.makeText(requireContext(), "Error al crear el producto: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.progress.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
            }
        }
    }

    private suspend fun uploadImage(uri: Uri): ProductImage? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = requireContext().contentResolver
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IOException("No se pudo abrir el stream para la URI: $uri")

            val requestBody = bytes.toRequestBody(contentResolver.getType(uri)?.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("content", "image.jpg", requestBody)

            val uploadService = RetrofitClient.createUploadService(requireContext())
            val imageList: List<ProductImage> = uploadService.uploadImage(part)
            imageList.firstOrNull()
        } catch (e: Exception) {
            Log.e("AddProductFragment", "Falló la subida de la imagen: $uri", e)
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
        binding.rvImagePreview.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}