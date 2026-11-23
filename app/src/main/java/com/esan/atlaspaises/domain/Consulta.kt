package com.esan.atlaspaises.domain

/** Qué está mostrando la pantalla Explorar. */
sealed interface Consulta {
    data object Todos : Consulta
    data class PorNombre(val texto: String) : Consulta
    data class PorRegion(val region: Region) : Consulta
}
