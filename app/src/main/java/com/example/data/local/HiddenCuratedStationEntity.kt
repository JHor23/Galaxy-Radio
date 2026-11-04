package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidden_curated_stations")
data class HiddenCuratedStationEntity(
    @PrimaryKey
    val stationId: String,
    val hiddenAt: Long = System.currentTimeMillis()
)
