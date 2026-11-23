package com.esan.atlaspaises.data.remote.dto

import com.google.gson.annotations.SerializedName

/*
 * DTO de la API REST Countries v5. Toda respuesta viene envuelta así:
 * { "data": { "objects": [ ... ], "meta": { "total", "count", "limit", "offset", "more" } } }
 * Incluso el detalle por código devuelve un arreglo `objects` con un solo elemento.
 * Todos los campos son anulables porque la API puede omitir datos de algunos países.
 */

data class CountriesResponseDto(
    @SerializedName("data") val data: CountriesDataDto?
)

data class CountriesDataDto(
    @SerializedName("objects") val objects: List<CountryDto>?,
    @SerializedName("meta") val meta: MetaDto?
)

data class MetaDto(
    @SerializedName("total") val total: Int?,
    @SerializedName("count") val count: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("offset") val offset: Int?,
    @SerializedName("more") val more: Boolean?
)

data class CountryDto(
    @SerializedName("names") val names: NamesDto?,
    @SerializedName("codes") val codes: CodesDto?,
    @SerializedName("capitals") val capitals: List<CapitalDto>?,
    @SerializedName("region") val region: String?,
    @SerializedName("subregion") val subregion: String?,
    @SerializedName("flag") val flag: FlagDto?,
    @SerializedName("population") val population: Long?,
    @SerializedName("area") val area: AreaDto?,
    @SerializedName("currencies") val currencies: List<CurrencyDto>?,
    @SerializedName("languages") val languages: List<LanguageDto>?,
    @SerializedName("borders") val borders: List<String>?
)

data class NamesDto(
    @SerializedName("common") val common: String?,
    @SerializedName("official") val official: String?
)

data class CodesDto(
    @SerializedName("alpha_2") val alpha2: String?,
    @SerializedName("alpha_3") val alpha3: String?
)

data class CapitalDto(
    @SerializedName("name") val name: String?
)

data class FlagDto(
    @SerializedName("url_png") val urlPng: String?,
    @SerializedName("url_svg") val urlSvg: String?
)

data class AreaDto(
    @SerializedName("kilometers") val kilometers: Double?
)

data class CurrencyDto(
    @SerializedName("code") val code: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("symbol") val symbol: String?
)

data class LanguageDto(
    @SerializedName("name") val name: String?
)
