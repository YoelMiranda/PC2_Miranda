package com.esan.atlaspaises.ui.detalle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esan.atlaspaises.data.local.PaisEntity
import com.esan.atlaspaises.data.repository.PaisRepository
import com.esan.atlaspaises.domain.EstadoVisita
import com.esan.atlaspaises.ui.mensajeDeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetalleUiState(
    val pais: PaisEntity? = null,
    val cargando: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DetalleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repositorio: PaisRepository
) : ViewModel() {

    val codigo: String = checkNotNull(savedStateHandle["codigo"])

    private data class Carga(val cargando: Boolean = true, val error: String? = null)

    private val carga = MutableStateFlow(Carga())

    // El país se lee de Room; la consulta a la API solo actualiza esa copia.
    val uiState: StateFlow<DetalleUiState> = combine(repositorio.observarPais(codigo), carga) { pais, carga ->
        DetalleUiState(pais = pais, cargando = carga.cargando, error = carga.error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUiState())

    init {
        refrescar()
    }

    /** Consulta el detalle del país por su código en la API. */
    fun refrescar() {
        viewModelScope.launch {
            carga.value = Carga(cargando = true)
            try {
                repositorio.refrescarDetalle(codigo)
                carga.value = Carga(cargando = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                carga.update { it.copy(cargando = false, error = mensajeDeError(e)) }
            }
        }
    }

    fun cambiarEstado(estado: EstadoVisita?) {
        viewModelScope.launch { repositorio.cambiarEstado(codigo, estado) }
    }
}
