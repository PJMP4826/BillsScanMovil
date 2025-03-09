package com.example.camerax

import android.graphics.drawable.PaintDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuthCard(
    user: String = "",
    contraseña: String = "",
    onUserChange:(String) -> Unit = {},
    onPasswordChange:(String) -> Unit = {},
    LoginClick: () -> Unit = {},
    RegistroClick: () -> Unit = {}
) {
    Box (
        modifier = Modifier.fillMaxSize()
    ){
        Image(
            painter = painterResource(id = R.drawable.fondo_imagen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column (modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally){
            Spacer(modifier = Modifier.height(110.dp))
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(150.dp)
                    .padding(5.dp)
                    .align(Alignment.CenterHorizontally)
            ){

                TextField(
                    value = user,
                    onValueChange = onUserChange,
                    placeholder = { Text("Correo electronico") },
                    modifier = Modifier.fillMaxWidth().height(75.dp)
                )
                TextField(
                    value = contraseña,
                    onValueChange = onPasswordChange,
                    placeholder = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth().height(75.dp)

                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = { LoginClick() },
                modifier = Modifier
                    .width(180.dp)
                    .height(50.dp)
            ){
                Text(text="Iniciar sesion")
            }
            Spacer(modifier = Modifier.height(200.dp))

            Text(
                text="¿Todavia no tienes una cuenta en BillsScan? \n Puedes registrarte ahorra",
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { RegistroClick() },
                modifier = Modifier
                    .width(180.dp)
                    .height(50.dp)
            ) {
                Text(text="Registro")
            }
        }
    }
}

@Composable
fun Auth(navHostController: NavHostController,viewModel: AuthViewModel){
    val context = LocalContext.current
    var user by remember{ mutableStateOf("") }
    var contraseña by remember{ mutableStateOf("") }

    AuthCard(
        user = user,
        contraseña = contraseña,
        onUserChange = { user = it },
        onPasswordChange = { contraseña = it },
        LoginClick = {viewModel.login(context, user, contraseña, navController = navHostController)},
        RegistroClick = { navHostController.navigate("registro")}
    )
}