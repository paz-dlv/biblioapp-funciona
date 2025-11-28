package com.biblioapp.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyUtils {
    // Formatea CLP: 19990 -> $19.990 (sin decimales)
    fun formatClp(amount: Double?): String {
        if (amount == null) return "$0"
        // Si tu backend devuelve enteros como 19990 para $19.990, usamos la unidad tal cual
        val symbols = DecimalFormatSymbols(Locale("es", "CL")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val df = DecimalFormat("#,###", symbols)
        return "$${df.format(amount)}"
    }
}