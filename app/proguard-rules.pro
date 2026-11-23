# Reglas ProGuard/R8 del módulo :app.
# Los DTO de la API se leen por reflexión con Gson: se conservan sus campos.
-keep class com.esan.atlaspaises.data.remote.dto.** { *; }
