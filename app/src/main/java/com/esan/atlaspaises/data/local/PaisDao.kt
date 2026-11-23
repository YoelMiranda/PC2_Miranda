package com.esan.atlaspaises.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PaisDao {

    @Query("SELECT * FROM paises WHERE codigo IN (:codigos) ORDER BY nombreComun COLLATE NOCASE")
    abstract fun observarPorCodigos(codigos: List<String>): Flow<List<PaisEntity>>

    @Query("SELECT * FROM paises ORDER BY nombreComun COLLATE NOCASE")
    abstract fun observarTodos(): Flow<List<PaisEntity>>

    @Query("SELECT * FROM paises WHERE region = :region ORDER BY nombreComun COLLATE NOCASE")
    abstract fun observarPorRegion(region: String): Flow<List<PaisEntity>>

    @Query(
        "SELECT * FROM paises WHERE nombreComun LIKE '%' || :texto || '%' " +
            "OR nombreOficial LIKE '%' || :texto || '%' ORDER BY nombreComun COLLATE NOCASE"
    )
    abstract fun observarPorNombre(texto: String): Flow<List<PaisEntity>>

    @Query("SELECT * FROM paises WHERE codigo = :codigo")
    abstract fun observarPorCodigo(codigo: String): Flow<PaisEntity?>

    @Query("SELECT * FROM paises WHERE estado IS NOT NULL ORDER BY nombreComun COLLATE NOCASE")
    abstract fun observarGuardados(): Flow<List<PaisEntity>>

    @Query("SELECT codigo, estado FROM paises WHERE codigo IN (:codigos) AND estado IS NOT NULL")
    abstract suspend fun estadosDe(codigos: List<String>): List<CodigoEstado>

    @Query("UPDATE paises SET estado = :estado WHERE codigo = :codigo")
    abstract suspend fun actualizarEstado(codigo: String, estado: String?)

    @Upsert
    abstract suspend fun upsert(paises: List<PaisEntity>)

    /**
     * Guarda los países que llegan de la API sin perder el estado (Por visitar / Visitado)
     * de los que ya estaban en la colección del usuario.
     */
    @Transaction
    open suspend fun guardarDesdeApi(paises: List<PaisEntity>) {
        if (paises.isEmpty()) return
        val estados = estadosDe(paises.map { it.codigo }).associate { it.codigo to it.estado }
        upsert(paises.map { it.copy(estado = estados[it.codigo]) })
    }
}
