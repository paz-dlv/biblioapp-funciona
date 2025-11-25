package com.biblioapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.biblioapp.api.TokenManager
import com.biblioapp.databinding.FragmentProfileBinding
import com.biblioapp.ui.MainActivity


class ProfileFragment : Fragment() { // Declaramos la clase del fragmento que hereda de Fragment

    private var _binding: FragmentProfileBinding? = null // Referencia mutable al binding (válida entre onCreateView y onDestroyView)
    private val binding get() = _binding!! // Acceso no nulo al binding mientras la vista existe

    override fun onCreateView( // Métodoo para crear/infla la vista del fragmento
        inflater: LayoutInflater, // Inflador para convertir XML en objetos View
        container: ViewGroup?, // Contenedor padre donde se insertará la vista
        savedInstanceState: Bundle? // Estado previamente guardado (no usado aquí)
    ): View { // Retornamos un View
        _binding = FragmentProfileBinding.inflate(inflater, container, false) // Inflamos el layout usando ViewBinding
        return binding.root // Retornamos la raíz del layout inflado
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // Métodoo llamado cuando la vista ya fue creada
        super.onViewCreated(view, savedInstanceState) // Llamamos a la superclase
        val tm = TokenManager(requireContext()) // Instanciamos el TokenManager para leer datos del usuario
        binding.tvName.text = tm.getUserName() // Asignamos el nombre del usuario al TextView correspondiente
        binding.tvEmail.text = tm.getUserEmail() // Asignamos el email del usuario al TextView correspondiente

        binding.btnLogout.setOnClickListener { // Asociamos un listener al botón de Cerrar sesión
            tm.clear() // Limpiamos token y datos del usuario de SharedPreferences
            // Creamos un Intent para ir a MainActivity (pantalla de login)
            val intent = Intent(requireContext(), MainActivity::class.java) // Intent explícito hacia MainActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // Limpiamos el back stack para que no se pueda volver con atrás
            startActivity(intent) // Lanzamos la actividad de login
            requireActivity().finish() // Cerramos la HomeActivity actual para completar el logout
        }
    }

    override fun onDestroyView() { // Métodoo llamado cuando la vista del fragmento se está destruyendo
        super.onDestroyView() // Llamamos a la superclase
        _binding = null // Liberamos el binding para evitar fugas de memoria
    }
}