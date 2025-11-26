package com.biblioapp.model

data class Product(
    val id: Int,

    val title: String,

    val author: String,

    val genre: String,

    val price: Double?,

    val description: String?,

    val stock: Int?,

    val image: List<ProductImage>?
) : java.io.Serializable


