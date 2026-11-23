package com.esan.atlaspaises.data.remote.dto

import com.esan.atlaspaises.data.local.PaisEntity

/**
 * Convierte un país de la API (DTO) en la Entity de Room.
 * Devuelve null si el país no trae código alfa-3 o nombre, porque sin ellos no se puede guardar.
 * El estado del usuario no se toca aquí: lo conserva PaisDao.guardarDesdeApi.
 */
fun CountryDto.toEntity(): PaisEntity? {
    val codigos = codes ?: return null
    val nombres = names ?: return null
    val codigo = codigos.alpha3?.uppercase()?.takeIf { it.isNotBlank() } ?: return null
    val nombre = nombres.common?.takeIf { it.isNotBlank() } ?: return null
    val alfa2 = codigos.alpha2?.takeIf { it.isNotBlank() }

    return PaisEntity(
        codigo = codigo,
        codigoAlfa2 = alfa2?.uppercase(),
        nombreComun = nombre,
        nombreOficial = nombres.official?.takeIf { it.isNotBlank() },
        // Si hay varias capitales se usa la primera.
        capital = capitals?.firstNotNullOfOrNull { it.name?.takeIf(String::isNotBlank) },
        region = region?.takeIf { it.isNotBlank() },
        subregion = subregion?.takeIf { it.isNotBlank() },
        poblacion = population,
        areaKm2 = area?.kilometers,
        monedas = currencies.orEmpty()
            .mapNotNull { it.formatear() }
            .joinToString(", ")
            .ifBlank { null },
        idiomas = languages.orEmpty()
            .mapNotNull { it.name?.takeIf(String::isNotBlank) }
            .joinToString(", ")
            .ifBlank { null },
        limitrofes = borders.orEmpty()
            .filter { it.isNotBlank() }
            .joinToString(", ")
            .ifBlank { null },
        banderaUrl = flag?.urlPng?.takeIf { it.isNotBlank() }
            ?: alfa2?.let { "https://flags.restcountries.com/v5/w320/${it.lowercase()}.png" }
    )
}

/** "Sol peruano (S/)"; si no hay símbolo, solo el nombre. */
private fun CurrencyDto.formatear(): String? {
    val nombreMoneda = name?.takeIf { it.isNotBlank() } ?: code?.takeIf { it.isNotBlank() } ?: return null
    val simbolo = symbol?.takeIf { it.isNotBlank() }
    return if (simbolo != null) "$nombreMoneda ($simbolo)" else nombreMoneda
}
