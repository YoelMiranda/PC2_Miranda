# Atlas de países

Práctica Calificada 2 (recuperación) del curso Desarrollo de Aplicaciones Móviles, ciclo 2026-2.

Esta app Android está hecha en Kotlin y Jetpack Compose. Consume la API [REST Countries](https://restcountries.com) (v5) y guarda los países en una base SQLite local con Room.

## Funcionalidades

- **Explorar**
  - Lista los países en orden alfabético, de 25 en 25, con un botón **Cargar más** que aparece solo si quedan resultados.
  - Búsqueda por nombre, que se ejecuta con la tecla de búsqueda del teclado o con el botón.
  - Filtro por región: Todas, África, América, Asia, Europa y Oceanía.
- **Detalle**
  - Consulta la API con el código del país.
  - Muestra la bandera, el nombre común y el oficial, el código, la capital, la región, la subregión, la población, el área, las monedas, los idiomas y los países limítrofes.
- **Mis países**
  - Lista los países guardados como *Por visitar* o *Visitado*, con filtros y el resumen "Visitados: X de Y".
- **Persistencia**
  - Todo lo que llega de la API se guarda en Room, y las pantallas leen desde Room.
  - Al volver a consultar un país, se conserva el estado que le diste.
- **Estados de pantalla**
  - Se muestra un indicador de carga mientras llega la respuesta.
  - Si la consulta falla, aparece un mensaje de error que se entiende, y los datos guardados se siguen mostrando.

## Configuración de la clave de API

La clave **no se sube al repositorio**. Para configurarla:

1. Crea una cuenta en <https://restcountries.com/sign-up> y genera tu clave.
2. En la raíz del proyecto, abre (o crea) el archivo `local.properties` y agrega esta línea:

   ```properties
   RESTCOUNTRIES_API_KEY=tu_clave_aqui
   ```

3. Sincroniza Gradle. La clave llega al código como `BuildConfig.RESTCOUNTRIES_API_KEY`.

`local.properties` está en `.gitignore`.

## Detalles de la API (v5)

| Uso | Endpoint |
|---|---|
| Listar | `GET /countries/v5?limit=25&offset=0` |
| Buscar por nombre | `GET /countries/v5/name?q={texto}&limit=25&offset=0` |
| Filtrar por región | `GET /countries/v5/region/{Africa\|Americas\|Asia\|Europe\|Oceania}` |
| Detalle por código | `GET /countries/v5/codes.alpha_3/{código}` |

- **URL base:** `https://api.restcountries.com/`
- **Autenticación:** cabecera `Authorization: Bearer <clave>`, que agrega un interceptor de OkHttp.
- **Respuesta:** viene en `{ "data": { "objects": [...], "meta": { "total", "count", "limit", "offset", "more" } } }`.
- **Paginación:** usa `limit` y `offset`. El campo `meta.more` indica si quedan resultados.
- **Campos pedidos:** con `response_fields` se piden solo los campos que usa la app, para ahorrar datos.

## Arquitectura

```
data/
  remote/      Retrofit (RestCountriesApi), DTO y conversión DTO -> Entity
  local/       Room: PaisEntity, PaisDao (suspend + Flow), AtlasDatabase
  repository/  PaisRepository: API -> Room, las pantallas observan Room
di/            Módulos de Hilt (red y base de datos)
domain/        EstadoVisita, Region (traducción al español), Consulta
ui/            Pantallas Compose + ViewModels (Explorar, Detalle, Mis países)
```

## Requisitos

- Android Studio con JDK 17
- minSdk 24, targetSdk 35
