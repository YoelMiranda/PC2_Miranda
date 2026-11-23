package com.esan.atlaspaises.ui.explorar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.esan.atlaspaises.data.local.PaisEntity
import com.esan.atlaspaises.domain.Region
import com.esan.atlaspaises.ui.NO_DISPONIBLE
import com.esan.atlaspaises.ui.componentes.AvisoError
import com.esan.atlaspaises.ui.componentes.Bandera
import com.esan.atlaspaises.ui.componentes.MensajeVacio

@Composable
fun ExplorarScreen(
    viewModel: ExplorarViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Al cambiar de búsqueda o de región, la lista vuelve arriba.
    LaunchedEffect(estado.consulta) { listState.scrollToItem(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Text(
            "Explorar",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        if (estado.cargando && estado.paises.isNotEmpty()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        estado.error?.let { mensaje ->
            AvisoError(
                mensaje = mensaje,
                onReintentar = viewModel::reintentar,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                estado.cargando && estado.paises.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                estado.sinResultados -> {
                    MensajeVacio(titulo = "No se encontraron países", icono = Icons.Filled.SearchOff)
                }
                estado.paises.isEmpty() && estado.error != null -> {
                    MensajeVacio(
                        titulo = "No hay países guardados para mostrar",
                        detalle = "Los países que consultes con conexión quedarán guardados en el teléfono.",
                        icono = Icons.Filled.CloudOff
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(estado.paises, key = { it.codigo }) { pais ->
                            FilaPais(
                                pais = pais
                            )
                        }
                        // "Cargar más" solo aparece cuando la API indica que quedan resultados.
                        if (estado.hayMas) {
                            item(key = "cargar_mas") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (estado.cargandoMas) {
                                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                    } else {
                                        FilledTonalButton(onClick = viewModel::cargarMas) {
                                            Text("Cargar más")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaPais(
    pais: PaisEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Bandera(
                url = pais.banderaUrl,
                nombre = pais.nombreComun,
                modifier = Modifier.size(width = 60.dp, height = 40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pais.nombreComun,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    Region.traducir(pais.region) ?: NO_DISPONIBLE,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Capital: ${pais.capital ?: "Sin capital"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
