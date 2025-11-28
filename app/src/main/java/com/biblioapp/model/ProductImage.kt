package com.biblioapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Model para las imágenes de producto según el JSON del backend.
 * Campos marcados como nullable con valores por defecto para evitar errores de parsing
 * si alguna propiedad falta en algún objeto.
 *
 * Implementa Serializable para poder pasar instancias por Intents/Bundle si lo necesitas.
 */
data class ProductImage(
    @SerializedName("path")
    val path: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("size")
    val size: Int? = null,

    @SerializedName("mime")
    val mime: String? = null,

    @SerializedName("access")
    val access: String? = null,

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("meta")
    val meta: ImageMeta? = null
) : Serializable

data class ImageMeta(
    @SerializedName("width")
    val width: Int? = null,

    @SerializedName("height")
    val height: Int? = null
) : Serializable