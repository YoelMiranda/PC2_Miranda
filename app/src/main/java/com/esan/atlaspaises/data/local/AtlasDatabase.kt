package com.esan.atlaspaises.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PaisEntity::class], version = 1, exportSchema = false)
abstract class AtlasDatabase : RoomDatabase() {
    abstract fun paisDao(): PaisDao
}
