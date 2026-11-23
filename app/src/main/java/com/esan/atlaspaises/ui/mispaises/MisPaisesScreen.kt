package com.esan.atlaspaises.ui.mispaises

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.esan.atlaspaises.data.local.PaisEntity
import com.esan.atlaspaises.domain.EstadoVisita
import com.esan.atlaspaises.ui.componentes.Bandera
import com.esan.atlaspaises.ui.componentes.BotonEstado
import com.esan.atlaspaises.ui.componentes.MensajeVacio

@Composable
fun MisPaisesScreen(
    onAbrirPais: (String) -> Unit,
    viewModel: MisPaisesViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Text(
            "Mis países",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        ResumenVisitados(
            visitados = estado.visitados,
            total = estado.totalGuardados,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(FiltroMisPaises.entries) { filtro ->
                FilterChip(
                    selected = estado.filtro == filtro,
                    onClick = { viewModel.seleccionarFiltro(filtro) },
                    label = { Text(filtro.etiqueta) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when {
                estado.cargando -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                estado.totalGuardados == 0 -> MensajeVacio(
                    titulo = "Aún no guardas países",
                    detalle = "En Explorar o en el detalle de un país, toca \"Guardar\" y elige Por visitar o Visitado.",
                    icono = Icons.Filled.TravelExplore
                )
                estado.paises.isEmpty() -> MensajeVacio(
                    titulo = when (estado.filtro) {
                        FiltroMisPaises.POR_VISITAR -> "No tienes países por visitar"
                        FiltroMisPaises.VISITADOS -> "Aún no marcas países como visitados"
                        FiltroMisPaises.TODOS -> "No hay países guardados"
                    },
                    icono = Icons.Filled.BookmarkBorder
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(estado.paises, key = { it.codigo }) { pais ->
                        FilaGuardado(
                            pais = pais,
                            onClick = { onAbrirPais(pais.codigo) },
                            onCambiarEstado = { viewModel.cambiarEstado(pais.codigo, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenVisitados(visitados: Int, total: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Visitados: $visitados de $total",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else visitados.toFloat() / total },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small)
            )
        }
    }
}

@Composable
private fun FilaGuardado(
    pais: PaisEntity,
    onClick: () -> Unit,
    onCambiarEstado: (EstadoVisita?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            Text(
                pais.nombreComun,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            BotonEstado(
                estado = EstadoVisita.desde(pais.estado),
                onCambiar = onCambiarEstado
            )
        }
    }
}
