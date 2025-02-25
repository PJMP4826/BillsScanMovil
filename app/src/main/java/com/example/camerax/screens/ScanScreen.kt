package com.example.camerax.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.example.camerax.R
import com.example.camerax.TicketResponse
import android.net.Uri

@Composable
fun ScanScreen(
    scannedImageUri: List<Uri>,
    ticketResponse: TicketResponse?,
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        scannedImageUri.forEach { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        }

        ticketResponse?.let { response ->
            Text(text = "Empresa: ${response.resultado.encabezado.nombre_empresa}")
            Text(text = "Fecha: ${response.resultado.encabezado.fecha}")
            Text(text = "Hora: ${response.resultado.encabezado.hora}")
            response.resultado.detalle_compra.forEach { detalle ->
                Text(text = "${detalle.cantidad} x ${detalle.descripcion} - \$${detalle.subtotal}")
            }
        }

        Button(onClick = onScanClick) {
            Text(text = stringResource(id = R.string.scan))
        }
    }
}
