package com.esan.atlaspaises.ui.explorar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esan.atlaspaises.data.local.PaisEntity
import com.esan.atlaspaises.data.repository.PaisRepository
import com.esan.atlaspaises.domain.Consulta
import com.esan.atlaspaises.domain.EstadoVisita
import com.esan.atlaspaises.domain.Region
import com.esan.atlaspaises.ui.comparadorNombres
import com.esan.atlaspaises.ui.mensajeDeError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExplorarUiState(
    val textoBusqueda: String = "",
    val consulta: Consulta = Consulta.Todos,
    val paises: List<PaisEntity> = emptyList(),
    val cargando: Boolean = false,
    val cargandoMas: Boolean = false,
    val hayMas: Boolean = false,
    val error: String? = null,
    val sinResultados: Boolean = false
) {
    val regionSeleccionada: Region? get() = (consulta as? Consulta.PorRegion)?.region
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExplorarViewModel @Inject constructor(
    private val repositorio: PaisRepository
) : ViewModel() {

    /**
     * Estado de la consulta en curso. [codigos] son los países que devolvió la API para esta consulta
     * (en el orden en que se fueron cargando); es null mientras no haya una respuesta exitosa,
     * y en ese caso se muestra lo que ya estaba guardado en Room para la misma consulta.
     */
    private data class Control(
        val consulta: Consulta = Consulta.Todos,
        val codigos: List<String>? = null,
        val siguienteOffset: Int = 0,
        val hayMas: Boolean = false,
        val cargando: Boolean = false,
        val cargandoMas: Boolean = false,
        val error: String? = null
    )

    private val texto = MutableStateFlow("")
    private val control = MutableStateFlow(Control())
    private var trabajo: Job? = null

    // La lista siempre se lee de Room, nunca de la respuesta de Retrofit.
    private val paises = control
        .map { it.consulta to it.codigos }
        .distinctUntilChanged()
        .flatMapLatest { (consulta, codigos) ->
            if (codigos != null) repositorio.observarPorCodigos(codigos) else repositorio.observarLocal(consulta)
        }
        .map { lista -> lista.sortedWith(compareBy(comparadorNombres) { it.nombreComun }) }

    val uiState: StateFlow<ExplorarUiState> = combine(texto, control, paises) { texto, control, paises ->
        ExplorarUiState(
            textoBusqueda = texto,
            consulta = control.consulta,
            paises = paises,
            cargando = control.cargando,
            cargandoMas = control.cargandoMas,
            hayMas = control.hayMas,
            error = control.error,
            sinResultados = !control.cargando && control.error == null &&
                control.codigos != null && paises.isEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExplorarUiState(cargando = true))

    init {
        // Solo se consulta la API al crear el ViewModel; al cambiar de pestaña se conserva lo cargado.
        cargar(Consulta.Todos)
    }

    fun onTextoChange(nuevo: String) {
        texto.value = nuevo
    }

    /** Se llama con la tecla de búsqueda del teclado o con el botón, no en cada letra. */
    fun buscar() {
        val consultaTexto = texto.value.trim()
        if (consultaTexto.isEmpty()) {
            cargarSiCambio(Consulta.Todos)
        } else {
            cargarSiCambio(Consulta.PorNombre(consultaTexto))
        }
    }

    fun limpiarBusqueda() {
        texto.value = ""
        if (control.value.consulta is Consulta.PorNombre) cargar(Consulta.Todos)
    }

    /** null = "Todas". El filtro por región reemplaza a la búsqueda por nombre. */
    fun seleccionarRegion(region: Region?) {
        texto.value = ""
        cargarSiCambio(region?.let { Consulta.PorRegion(it) } ?: Consulta.Todos)
    }

    fun cargarMas() {
        val actual = control.value
        if (!actual.hayMas || actual.cargando || actual.cargandoMas) return
        trabajo = viewModelScope.launch {
            control.update { it.copy(cargandoMas = true, error = null) }
            try {
                val pagina = repositorio.cargarPagina(actual.consulta, actual.siguienteOffset)
                control.update {
                    it.copy(
                        codigos = (it.codigos.orEmpty() + pagina.codigos).distinct(),
                        siguienteOffset = it.siguienteOffset + pagina.recibidos,
                        hayMas = pagina.hayMas,
                        cargandoMas = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                control.update { it.copy(cargandoMas = false, error = mensajeDeError(e)) }
            }
        }
    }

    fun reintentar() {
        val actual = control.value
        if (actual.codigos == null) cargar(actual.consulta) else cargarMas()
    }

    fun cambiarEstado(codigo: String, estado: EstadoVisita?) {
        viewModelScope.launch { repositorio.cambiarEstado(codigo, estado) }
    }

    /** Evita repetir la misma consulta a la API si ya está cargada sin errores (el plan gratuito tiene límite). */
    private fun cargarSiCambio(consulta: Consulta) {
        val actual = control.value
        if (actual.consulta == consulta && actual.codigos != null && actual.error == null) return
        cargar(consulta)
    }

    private fun cargar(consulta: Consulta) {
        trabajo?.cancel()
        control.value = Control(consulta = consulta, cargando = true)
        trabajo = viewModelScope.launch {
            try {
                val pagina = repositorio.cargarPagina(consulta, offset = 0)
                control.update {
                    it.copy(
                        codigos = pagina.codigos,
                        siguienteOffset = pagina.recibidos,
                        hayMas = pagina.hayMas,
                        cargando = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                control.update { it.copy(cargando = false, error = mensajeDeError(e)) }
            }
        }
    }
}
