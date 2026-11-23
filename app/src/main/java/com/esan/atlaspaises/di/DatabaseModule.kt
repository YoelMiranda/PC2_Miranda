package com.esan.atlaspaises.di

import android.content.Context
import androidx.room.Room
import com.esan.atlaspaises.data.local.AtlasDatabase
import com.esan.atlaspaises.data.local.PaisDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AtlasDatabase =
        Room.databaseBuilder(context, AtlasDatabase::class.java, "atlas_paises.db").build()

    @Provides
    fun providePaisDao(database: AtlasDatabase): PaisDao = database.paisDao()
}
