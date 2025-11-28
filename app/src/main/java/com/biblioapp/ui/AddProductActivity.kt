package com.biblioapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.biblioapp.ui.fragments.AddProductFragment

/**
 * Host Activity para AddProductFragment.
 *
 * Uso:
 *  - Para crear: AddProductActivity.start(context)
 *  - Para editar: AddProductActivity.start(context, productId = 123)
 *
 * Devuelve RESULT_OK al caller si la operación (crear/editar) fue exitosa
 * (el fragment llama requireActivity().setResult(Activity.RESULT_OK) y finish()).
 */
class AddProductActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"

        fun start(context: Context, productId: Int? = null) {
            val intent = Intent(context, AddProductActivity::class.java)
            productId?.takeIf { it != 0 }?.let { intent.putExtra(EXTRA_PRODUCT_ID, it) }
            // si el contexto no es Activity se necesita FLAG_ACTIVITY_NEW_TASK, pero normalmente lo llamas desde Activity/Fragment
            if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        /**
         * Helper para startActivityForResult desde una Activity:
         *   startActivityForResult(AddProductActivity.intent(context, id), REQUEST_CODE)
         * (opcional, si prefieres pasar el Intent en vez de usar start())
         */
        fun intent(context: Context, productId: Int? = null): Intent {
            val intent = Intent(context, AddProductActivity::class.java)
            productId?.takeIf { it != 0 }?.let { intent.putExtra(EXTRA_PRODUCT_ID, it) }
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Leer productId (si existe) y pasarlo como argumento al fragment
        val productId = intent?.extras?.getInt(EXTRA_PRODUCT_ID) ?: 0
        val fragment = AddProductFragment().apply {
            if (productId != 0) arguments = bundleOf(EXTRA_PRODUCT_ID to productId)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit()
        }
    }
}