package com.esan.atlaspaises.data.repository

import com.esan.atlaspaises.data.local.PaisDao
import com.esan.atlaspaises.data.local.PaisEntity
import com.esan.atlaspaises.data.remote.RestCountriesApi
import com.esan.atlaspaises.data.remote.RestCountriesApi.Companion.TAMANO_PAGINA
import com.esan.atlaspaises.data.remote.dto.CountriesResponseDto
import com.esan.atlaspaises.data.remote.dto.toEntity
import com.esan.atlaspaises.domain.Consulta
import com.esan.atlaspaises.domain.EstadoVisita
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

/** Resultado de pedir una página a la API: qué países llegaron (ya guardados en Room) y si quedan más. */
data class Pagina(
    val codigos: List<String>,
    val recibidos: Int,
    val hayMas: Boolean
)

class PaisNoEncontradoException : Exception("País no encontrado")

/**
 * Única puerta de entrada a los datos. Lo que llega de Retrofit se guarda en Room y
 * las pantallas siempre leen desde Room (los métodos `observar*`).
 */
@Singleton
class PaisRepository @Inject constructor(
    private val api: RestCountriesApi,
    private val dao: PaisDao
) {

    /** Pide a la API una página de la consulta, la guarda en Room y devuelve los códigos recibidos. */
    suspend fun cargarPagina(consulta: Consulta, offset: Int): Pagina {
        val respuesta: CountriesResponseDto = try {
            when (consulta) {
                Consulta.Todos -> api.listar(TAMANO_PAGINA, offset)
                is Consulta.PorNombre -> api.buscarPorNombre(consulta.texto, TAMANO_PAGINA, offset)
                is Consulta.PorRegion -> api.listarPorRegion(consulta.region.valorApi, TAMANO_PAGINA, offset)
            }
        } catch (e: HttpException) {
            // Una búsqueda sin coincidencias puede responder 404: se trata como lista vacía.
            if (e.code() == 404 && consulta !is Consulta.Todos) return Pagina(emptyList(), 0, false)
            throw e
        }

        val objetos = respuesta.data?.objects.orEmpty()
        val paises = objetos.mapNotNull { it.toEntity() }
        dao.guardarDesdeApi(paises)

        val meta = respuesta.data?.meta
        val hayMas = meta?.more ?: meta?.total?.let { total -> offset + objetos.size < total } ?: false
        return Pagina(paises.map { it.codigo }, objetos.size, hayMas)
    }

    /** Consulta el detalle de un país por su código y actualiza la copia local. */
    suspend fun refrescarDetalle(codigo: String) {
        val pais = try {
            api.detallePorCodigo(codigo).data?.objects?.firstOrNull()?.toEntity()
        } catch (e: HttpException) {
            if (e.code() == 404) null else throw e
        } ?: throw PaisNoEncontradoException()
        dao.guardarDesdeApi(listOf(pais))
    }

    fun observarPorCodigos(codigos: List<String>): Flow<List<PaisEntity>> = dao.observarPorCodigos(codigos)

    /** Lo que ya hay guardado en Room para una consulta; se usa mientras carga o si la API falla. */
    fun observarLocal(consulta: Consulta): Flow<List<PaisEntity>> = when (consulta) {
        Consulta.Todos -> dao.observarTodos()
        is Consulta.PorNombre -> dao.observarPorNombre(consulta.texto)
        is Consulta.PorRegion -> dao.observarPorRegion(consulta.region.valorApi)
    }

    fun observarPais(codigo: String): Flow<PaisEntity?> = dao.observarPorCodigo(codigo)

    fun observarGuardados(): Flow<List<PaisEntity>> = dao.observarGuardados()

    /** Guarda el país con el estado indicado, o lo quita de la colección si [estado] es null. */
    suspend fun cambiarEstado(codigo: String, estado: EstadoVisita?) {
        dao.actualizarEstado(codigo, estado?.name)
    }
}
