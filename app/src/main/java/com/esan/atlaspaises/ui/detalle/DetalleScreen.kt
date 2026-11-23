package com.esan.atlaspaises.ui.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.esan.atlaspaises.data.local.PaisEntity
import com.esan.atlaspaises.domain.EstadoVisita
import com.esan.atlaspaises.domain.Region
import com.esan.atlaspaises.ui.componentes.AvisoError
import com.esan.atlaspaises.ui.componentes.Bandera
import com.esan.atlaspaises.ui.componentes.BotonEstado
import com.esan.atlaspaises.ui.componentes.MensajeVacio
import com.esan.atlaspaises.ui.formatearArea
import com.esan.atlaspaises.ui.formatearPoblacion
import com.esan.atlaspaises.ui.oNoDisponible

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    onVolver: () -> Unit,
    viewModel: DetalleViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(estado.pais?.nombreComun ?: "Detalle del país") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val pais = estado.pais
            if (estado.cargando && pais != null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            estado.error?.let {
                AvisoError(
                    mensaje = it,
                    onReintentar = viewModel::refrescar,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            when {
                pais != null -> ContenidoDetalle(pais = pais, onCambiarEstado = viewModel::cambiarEstado)
                estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                else -> MensajeVacio(
                    titulo = "No hay datos guardados de este país",
                    detalle = "Vuelve a intentarlo cuando tengas conexión.",
                    icono = Icons.Filled.CloudOff
                )
            }
        }
    }
}

@Composable
private fun ContenidoDetalle(
    pais: PaisEntity,
    onCambiarEstado: (EstadoVisita?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Bandera(
            url = pais.banderaUrl,
            nombre = pais.nombreComun,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 2f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            pais.nombreComun,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            pais.nombreOficial.oNoDisponible(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        BotonEstado(estado = EstadoVisita.desde(pais.estado), onCambiar = onCambiarEstado)
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                val datos = listOf(
                    "Código del país" to pais.codigo,
                    "Capital" to pais.capital.oNoDisponible(),
                    "Región" to Region.traducir(pais.region).oNoDisponible(),
                    "Subregión" to pais.subregion.oNoDisponible(),
                    "Población" to formatearPoblacion(pais.poblacion),
                    "Área" to formatearArea(pais.areaKm2),
                    "Monedas" to pais.monedas.oNoDisponible(),
                    "Idiomas" to pais.idiomas.oNoDisponible(),
                    "Países limítrofes" to pais.limitrofes.oNoDisponible()
                )
                datos.forEachIndexed { indice, (etiqueta, valor) ->
                    FilaDato(etiqueta, valor)
                    if (indice < datos.lastIndex) HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}
