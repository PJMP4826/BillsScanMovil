package com.example.camerax

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.Source

class AuthViewModel: ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    val db = FirebaseFirestore.getInstance()

    fun login(context: Context, user: String, contraseña: String, navController: NavHostController){
        auth.signInWithEmailAndPassword(user, contraseña)
            .addOnCompleteListener{task ->
                if(task.isSuccessful){
                    val TheUser = auth.currentUser
                    TheUser?.let{AuthUser ->
                        val userDocRef = db.collection("users").document(AuthUser.uid)

                        userDocRef.get()
                            .addOnSuccessListener{document ->
                                if (document.exists()){
                                    Toast.makeText(context, "Inicio de sesion exitoso", Toast.LENGTH_LONG).show()
                                    navController.navigate("pantallaInicio")
                                } else{
                                    Toast.makeText(context,"No se encontraron datos del usuario", Toast.LENGTH_LONG).show()
                                }
                            }

                    }
                } else{
                    val mensageError = task.exception?.message?: "Error desconocido"
                    Log.e("Login","Error; $mensageError")
                }
            }
    }

    fun registro(context: Context, nombre: String,user: String, contraseña: String, navController: NavHostController){
        auth.createUserWithEmailAndPassword(user, contraseña)
            .addOnCompleteListener{task ->
                if(task.isSuccessful){
                    val currentUser = auth.currentUser
                    currentUser?.let{firebaseUser ->
                        val dataUser = hashMapOf(
                            "uid" to firebaseUser.uid,
                            "nombre" to nombre,
                            "user" to user
                        )

                        db.collection("users").document(firebaseUser.uid)
                            .set(dataUser)
                            .addOnFailureListener{e ->
                                Toast.makeText(context,"Datos enviados, pero ocurrio un error al guardar el usuario: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_LONG).show()
                    navController.navigate("pantallaInicio")
                } else{
                    val mensageError = task.exception?.message?: "Error desconocido"
                    Toast.makeText(context, "Error; $mensageError", Toast.LENGTH_LONG).show()
                }
            }
    }
}