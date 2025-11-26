package com.biblioapp.model

data class UpdateProductRequest(
    val title: String? = null,
    val author: String? = null,
    val genre: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val image: List<ProductImage>? = null
)