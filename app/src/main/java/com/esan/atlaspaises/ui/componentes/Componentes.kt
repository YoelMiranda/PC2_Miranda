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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.esan.atlaspaises.domain.EstadoVisita

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

/**
 * Botón para guardar el país en "Mis países". Muestra el estado actual
 * (Guardar / Por visitar / Visitado) y al tocarlo deja cambiarlo o quitar el país.
 */
@Composable
fun BotonEstado(
    estado: EstadoVisita?,
    onCambiar: (EstadoVisita?) -> Unit,
    modifier: Modifier = Modifier
) {
    var abierto by remember { mutableStateOf(false) }
    val (texto, icono) = when (estado) {
        null -> "Guardar" to Icons.Filled.BookmarkBorder
        EstadoVisita.POR_VISITAR -> EstadoVisita.POR_VISITAR.etiqueta to Icons.Filled.Schedule
        EstadoVisita.VISITADO -> EstadoVisita.VISITADO.etiqueta to Icons.Filled.CheckCircle
    }
    val colores = when (estado) {
        null -> AssistChipDefaults.assistChipColors()
        EstadoVisita.POR_VISITAR -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
            leadingIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        EstadoVisita.VISITADO -> AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }

    Box(modifier = modifier) {
        AssistChip(
            onClick = { abierto = true },
            label = { Text(texto) },
            leadingIcon = { Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp)) },
            colors = colores,
            border = if (estado == null) AssistChipDefaults.assistChipBorder(enabled = true) else null
        )
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            OpcionMenu("Por visitar", Icons.Filled.Schedule, seleccionada = estado == EstadoVisita.POR_VISITAR) {
                abierto = false
                onCambiar(EstadoVisita.POR_VISITAR)
            }
            OpcionMenu("Visitado", Icons.Filled.CheckCircle, seleccionada = estado == EstadoVisita.VISITADO) {
                abierto = false
                onCambiar(EstadoVisita.VISITADO)
            }
            if (estado != null) {
                OpcionMenu("Quitar de Mis países", Icons.Filled.Delete, seleccionada = false) {
                    abierto = false
                    onCambiar(null)
                }
            }
        }
    }
}

@Composable
private fun OpcionMenu(texto: String, icono: ImageVector, seleccionada: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                texto,
                color = if (seleccionada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        leadingIcon = { Icon(icono, contentDescription = null) },
        onClick = onClick
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
