package com.esan.atlaspaises.domain

/** Estado con el que el usuario guarda un país en su colección "Mis países". */
enum class EstadoVisita(val etiqueta: String) {
    POR_VISITAR("Por visitar"),
    VISITADO("Visitado");

    companion object {
        fun desde(valor: String?): EstadoVisita? = entries.firstOrNull { it.name == valor }
    }
}
