package com.esan.atlaspaises.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

/** Bandera del país; si no carga (sin red o sin URL) muestra un globo en su lugar. */
@Composable
fun Bandera(
    url: String?,
    nombre: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val reemplazo: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Public, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    SubcomposeAsyncImage(
        model = url,
        contentDescription = "Bandera de $nombre",
        contentScale = contentScale,
        modifier = modifier.clip(RoundedCornerShape(6.dp)),
        loading = { reemplazo() },
        error = { reemplazo() }
    )
}

/** Aviso de error que deja reintentar; se muestra encima de los datos guardados, sin ocultarlos. */
@Composable
fun AvisoError(
    mensaje: String,
    onReintentar: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(mensaje, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (onReintentar != null) {
                TextButton(onClick = onReintentar) { Text("Reintentar") }
            }
        }
    }
}

/** Mensaje centrado para listas vacías. */
@Composable
fun MensajeVacio(
    titulo: String,
    detalle: String? = null,
    icono: ImageVector = Icons.Filled.Public,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (detalle != null) {
            Text(
                detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
