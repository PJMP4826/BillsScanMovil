package com.example.camerax

import androidx.compose.foundation.Image
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Registro(
    nombre: String = "",
    user: String ="",
    contraseña: String = "",
    onNombreChange: (String) -> Unit = {},
    onUserChange: (String) -> Unit = {},
    onContraseñaChange: (String) -> Unit = {},
    onRegistroClcik: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()){
        Image(
            painter = painterResource(id = R.drawable.fondo_imagen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column (modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally){
            Spacer(modifier = Modifier.height(150.dp))
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(333.dp)
            ){
                TextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    placeholder = { Text("Nombre de usuario") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(111.dp)
                )
                TextField(
                    value = user,
                    onValueChange = onUserChange,
                    placeholder = { Text("Correo electronico") },
                    modifier = Modifier.fillMaxWidth().height(111.dp)
                )
                TextField(
                    value = contraseña,
                    onValueChange = onContraseñaChange,
                    placeholder = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth().height(111.dp)
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = onRegistroClcik,
                modifier = Modifier.width(150.dp).height(50.dp)
            ) {
                Text("Crear cuenta")
            }


        }
    }

}

@Composable
fun UserRegister(navHostController: NavHostController, viewModel: AuthViewModel){
    val context = LocalContext.current
    var nombre by remember{ mutableStateOf("") }
    var user by remember{ mutableStateOf("") }
    var contraseña by remember{ mutableStateOf("") }

    Registro(
        nombre = nombre,
        user = user,
        contraseña = contraseña,
        onNombreChange = { nombre = it },
        onUserChange = { user = it },
        onContraseñaChange = { contraseña = it },
        onRegistroClcik = { viewModel.registro( context ,nombre, user, contraseña, navController = navHostController) },
    )
}