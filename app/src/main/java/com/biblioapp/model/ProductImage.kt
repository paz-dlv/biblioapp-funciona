package com.biblioapp.model

import com.google.gson.annotations.SerializedName


data class ProductImage(
    // El @SerializedName("path") le dice a GSON: "Cuando veas la clave 'path' en el JSON,
    // guarda su valor en esta variable 'path'".
    @SerializedName("path")
    val path: String,

    // Mapeamos la clave 'name' del JSON a nuestra variable 'name'.
    @SerializedName("name")
    val name: String?,

    // Mapeamos la clave 'type' del JSON.
    @SerializedName("type")
    val type: String?,

    // Mapeamos la clave 'size' del JSON.
    @SerializedName("size")
    val size: Int?,

    // Mapeamos la clave 'mime' del JSON.
    @SerializedName("mime")
    val mime: String?,

    // Mapeamos la clave 'access' del JSON.
    @SerializedName("access")
    val access: String?,

    // Mapeamos la clave 'url' del JSON, que puede ser nula.
    @SerializedName("url")
    val url: String?,

    // 'meta' es un objeto anidado dentro del JSON. También necesita su propio data class.
    @SerializedName("meta")
    val meta: ImageMeta?
) : java.io.Serializable // Implementa Serializable para poder pasar este objeto entre fragmentos/actividades si es necesario.

/**
 * ImageMeta: Representa el objeto anidado 'meta' que contiene el ancho y alto de la imagen.
 */
data class ImageMeta(
    @SerializedName("width")
    val width: Int?,

    @SerializedName("height")
    val height: Int?
) : java.io.Serializable // También es buena idea hacerlo serializable.
