package com.esan.atlaspaises.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * País guardado en la base local. Se llena con lo que devuelve la API y además
 * guarda el [estado] que eligió el usuario ("POR_VISITAR", "VISITADO" o null si no está en su colección).
 * Las listas de la API (monedas, idiomas, limítrofes) se guardan ya formateadas como texto.
 */
@Entity(tableName = "paises")
data class PaisEntity(
    @PrimaryKey val codigo: String,
    val codigoAlfa2: String?,
    val nombreComun: String,
    val nombreOficial: String?,
    val capital: String?,
    val region: String?,
    val subregion: String?,
    val poblacion: Long?,
    val areaKm2: Double?,
    val monedas: String?,
    val idiomas: String?,
    val limitrofes: String?,
    val banderaUrl: String?,
    val estado: String? = null,
    val actualizadoEn: Long = System.currentTimeMillis()
)

/** Proyección mínima para recuperar el estado guardado antes de sobrescribir un país. */
data class CodigoEstado(
    val codigo: String,
    val estado: String?
)
