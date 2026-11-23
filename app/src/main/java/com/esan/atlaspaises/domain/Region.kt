package com.esan.atlaspaises.domain

/** Regiones del filtro de Explorar: valor que espera la API (inglés) y texto que ve el usuario. */
enum class Region(val valorApi: String, val etiqueta: String) {
    AFRICA("Africa", "África"),
    AMERICAS("Americas", "América"),
    ASIA("Asia", "Asia"),
    EUROPA("Europe", "Europa"),
    OCEANIA("Oceania", "Oceanía");

    companion object {
        /** Traduce al español la región que devuelve la API. */
        fun traducir(valorApi: String?): String? {
            if (valorApi.isNullOrBlank()) return null
            entries.firstOrNull { it.valorApi.equals(valorApi, ignoreCase = true) }?.let { return it.etiqueta }
            return when (valorApi.lowercase()) {
                "antarctic", "antarctica" -> "Antártida"
                else -> valorApi
            }
        }
    }
}
