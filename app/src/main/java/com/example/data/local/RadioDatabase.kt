package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CustomStationEntity::class, HiddenCuratedStationEntity::class], version = 2, exportSchema = false)
abstract class RadioDatabase : RoomDatabase() {

    abstract fun stationDao(): RadioStationDao

    companion object {
        @Volatile
        private var INSTANCE: RadioDatabase? = null

        fun getInstance(context: Context): RadioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RadioDatabase::class.java,
                    "galaxy_radio.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
