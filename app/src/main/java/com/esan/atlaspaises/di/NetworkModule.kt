package com.esan.atlaspaises.di

import com.esan.atlaspaises.BuildConfig
import com.esan.atlaspaises.data.remote.RestCountriesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        // La clave se lee de BuildConfig (viene de local.properties) y se envía como Bearer en cada petición.
        val autorizacion = Interceptor { chain ->
            val clave = BuildConfig.RESTCOUNTRIES_API_KEY
            val peticion = chain.request().newBuilder().apply {
                if (clave.isNotBlank()) header("Authorization", "Bearer $clave")
            }.build()
            chain.proceed(peticion)
        }
        // BASIC registra solo método, URL y código de respuesta: nunca la cabecera con la clave.
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(autorizacion)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.RESTCOUNTRIES_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideRestCountriesApi(retrofit: Retrofit): RestCountriesApi =
        retrofit.create(RestCountriesApi::class.java)
}
