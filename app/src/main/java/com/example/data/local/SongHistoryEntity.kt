package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "song_history",
    indices = [
        Index(value = ["stationId"]),
        Index(value = ["streamUrl"]),
        Index(value = ["playedAtTimestamp"])
    ]
)
data class SongHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stationId: String,
    val stationName: String,
    val streamUrl: String,
    val trackTitle: String,
    val artist: String,
    val playedAtTimestamp: Long = System.currentTimeMillis()
)
