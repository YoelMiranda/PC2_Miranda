package com.esan.atlaspaises.data.remote

import com.esan.atlaspaises.data.remote.dto.CountriesResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints de REST Countries v5 (https://restcountries.com/docs/countries).
 * La clave se envía en la cabecera `Authorization: Bearer <clave>` (ver NetworkModule).
 * La paginación usa `limit` y `offset`; la respuesta indica en `meta.more` si quedan resultados.
 */
interface RestCountriesApi {

    /** Lista todos los países, ordenados por nombre común. */
    @GET("countries/v5")
    suspend fun listar(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("response_fields") campos: String? = CAMPOS
    ): CountriesResponseDto

    /** Busca por nombre (común, oficial, alternativos y nativos). */
    @GET("countries/v5/name")
    suspend fun buscarPorNombre(
        @Query("q") texto: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("response_fields") campos: String? = CAMPOS
    ): CountriesResponseDto

    /** Filtra por región exacta (Africa, Americas, Asia, Europe, Oceania). */
    @GET("countries/v5/region/{region}")
    suspend fun listarPorRegion(
        @Path("region") region: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("response_fields") campos: String? = CAMPOS
    ): CountriesResponseDto

    /** Detalle de un país por su código ISO alfa-3. Devuelve `objects` con un solo elemento. */
    @GET("countries/v5/codes.alpha_3/{codigo}")
    suspend fun detallePorCodigo(
        @Path("codigo") codigo: String,
        @Query("response_fields") campos: String? = CAMPOS
    ): CountriesResponseDto

    companion object {
        const val TAMANO_PAGINA = 25

        /**
         * Solo se piden los campos que usa la app, para que las respuestas pesen menos.
         * Si se cambia a null, Retrofit no envía el parámetro y la API devuelve todos los campos.
         */
        val CAMPOS: String? = "names.common,names.official,codes,capitals,region,subregion," +
            "flag,population,area,currencies,languages,borders"
    }
}
