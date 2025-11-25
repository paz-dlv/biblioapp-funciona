package com.biblioapp.model

data class Product(
    val id: Int,

    val title: String,

    val author: String,

    val genre: String,

    val description: String,

    val price: Double,

    val stock: Int,

    val image: List<ProductImage>?
) : java.io.Serializable


