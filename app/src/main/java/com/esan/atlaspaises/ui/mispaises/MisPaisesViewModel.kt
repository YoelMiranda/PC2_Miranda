package com.esan.atlaspaises.ui.mispaises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esan.atlaspaises.data.local.PaisEntity
import com.esan.atlaspaises.data.repository.PaisRepository
import com.esan.atlaspaises.domain.EstadoVisita
import com.esan.atlaspaises.ui.comparadorNombres
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FiltroMisPaises(val etiqueta: String, val estado: EstadoVisita?) {
    TODOS("Todos", null),
    POR_VISITAR("Por visitar", EstadoVisita.POR_VISITAR),
    VISITADOS("Visitados", EstadoVisita.VISITADO)
}

data class MisPaisesUiState(
    val filtro: FiltroMisPaises = FiltroMisPaises.TODOS,
    val paises: List<PaisEntity> = emptyList(),
    val visitados: Int = 0,
    val totalGuardados: Int = 0,
    val cargando: Boolean = true
)

@HiltViewModel
class MisPaisesViewModel @Inject constructor(
    private val repositorio: PaisRepository
) : ViewModel() {

    private val filtro = MutableStateFlow(FiltroMisPaises.TODOS)

    // Mis países se lee completamente desde Room: no consulta la API.
    val uiState: StateFlow<MisPaisesUiState> =
        combine(repositorio.observarGuardados(), filtro) { guardados, filtro ->
            val ordenados = guardados.sortedWith(compareBy(comparadorNombres) { it.nombreComun })
            MisPaisesUiState(
                filtro = filtro,
                paises = filtro.estado?.let { estado -> ordenados.filter { it.estado == estado.name } } ?: ordenados,
                visitados = guardados.count { it.estado == EstadoVisita.VISITADO.name },
                totalGuardados = guardados.size,
                cargando = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MisPaisesUiState())

    fun seleccionarFiltro(nuevo: FiltroMisPaises) {
        filtro.value = nuevo
    }

    fun cambiarEstado(codigo: String, estado: EstadoVisita?) {
        viewModelScope.launch { repositorio.cambiarEstado(codigo, estado) }
    }
}
