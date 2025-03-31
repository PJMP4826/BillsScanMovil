package com.example.camerax.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    companion object {
        private const val TAG = "AuthViewModel"
    }

    fun signIn(
        context: Context,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                if (email.isEmpty() || password.isEmpty()) {
                    _error.value = "Por favor complete todos los campos"
                    return@launch
                }

                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Log.d(TAG, "Login exitoso")
                        Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error en login", e)
                        _error.value = e.message
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado en login", e)
                _error.value = e.message
                Toast.makeText(context, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(
        context: Context,
        nombre: String,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    _error.value = "Por favor complete todos los campos"
                    return@launch
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        try {
                            // Crear documento del usuario en Firestore
                            val user = hashMapOf(
                                "nombre" to nombre,
                                "email" to email,
                                "createdAt" to System.currentTimeMillis()
                            )

                            authResult.user?.uid?.let { uid ->
                                firestore.collection("users")
                                    .document(uid)
                                    .set(user)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Usuario registrado en Firestore")
                                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                        // Cerrar sesión después del registro para que el usuario tenga que iniciar sesión
                                        auth.signOut()
                                        onSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "Error al guardar en Firestore", e)
                                        // No bloqueamos el flujo por error de Firestore
                                        // Procedemos con el registro exitoso
                                        Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                        auth.signOut()
                                        onSuccess()
                                    }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al guardar en Firestore", e)
                            // No bloqueamos el flujo por error de Firestore
                            Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error en registro", e)
                        _error.value = e.message
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado en registro", e)
                _error.value = e.message
                Toast.makeText(context, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        try {
            auth.signOut()
            Log.d(TAG, "Sesión cerrada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión", e)
        }
    }

    fun getCurrentUser() = auth.currentUser

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}