package com.esan.atlaspaises.ui

import com.esan.atlaspaises.data.repository.PaisNoEncontradoException
import retrofit2.HttpException
import java.io.IOException
import java.text.Collator
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

const val NO_DISPONIBLE = "No disponible"

private val simbolos = DecimalFormatSymbols(Locale.US).apply {
    groupingSeparator = ','
    decimalSeparator = '.'
}

/** 33715471 -> "33,715,471" */
fun formatearPoblacion(poblacion: Long?): String =
    poblacion?.let { DecimalFormat("#,##0", simbolos).format(it) } ?: NO_DISPONIBLE

/** 1285216.0 -> "1,285,216 km²" */
fun formatearArea(areaKm2: Double?): String =
    areaKm2?.let { "${DecimalFormat("#,##0.##", simbolos).format(it)} km²" } ?: NO_DISPONIBLE

fun String?.oNoDisponible(): String = if (isNullOrBlank()) NO_DISPONIBLE else this

/** Ordena por nombre en español, ignorando tildes y mayúsculas (Åland junto a Albania, Perú junto a Peru). */
private val collator: Collator = Collator.getInstance(Locale("es")).apply { strength = Collator.PRIMARY }
val comparadorNombres: Comparator<String> = Comparator { a, b -> collator.compare(a, b) }

/** Traduce un error de red o de la API a un mensaje que el usuario entienda. */
fun mensajeDeError(error: Throwable): String = when (error) {
    is PaisNoEncontradoException -> "No se encontró información de este país."
    is HttpException -> when (error.code()) {
        401 -> "La clave de la API no es válida o falta. Revisa RESTCOUNTRIES_API_KEY en local.properties."
        403 -> "La API rechazó la consulta: se alcanzó el límite del plan o la cuenta no tiene acceso."
        429 -> "Se hicieron demasiadas consultas seguidas. Espera unos segundos e inténtalo de nuevo."
        in 500..599 -> "El servicio de países no está disponible en este momento. Inténtalo más tarde."
        else -> "No se pudo completar la consulta (error ${error.code()})."
    }
    is IOException -> "No hay conexión a internet. Revisa tu red e inténtalo de nuevo."
    else -> "Ocurrió un error inesperado al consultar los países."
}
