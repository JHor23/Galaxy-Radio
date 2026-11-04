package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RadioStationDao {

    @Query("SELECT * FROM radio_stations ORDER BY createdAt DESC")
    fun getAllStations(): Flow<List<CustomStationEntity>>

    @Query("SELECT * FROM radio_stations WHERE isCustom = 1 ORDER BY createdAt DESC")
    fun getCustomStations(): Flow<List<CustomStationEntity>>

    @Query("SELECT * FROM radio_stations WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteStations(): Flow<List<CustomStationEntity>>

    @Query("SELECT * FROM radio_stations WHERE id = :id LIMIT 1")
    suspend fun getStationById(id: String): CustomStationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: CustomStationEntity)

    @Update
    suspend fun updateStation(station: CustomStationEntity)

    @Delete
    suspend fun deleteStation(station: CustomStationEntity)

    @Query("DELETE FROM radio_stations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE radio_stations SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("SELECT stationId FROM hidden_curated_stations")
    fun getHiddenCuratedStationIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun hideCuratedStation(hidden: HiddenCuratedStationEntity)

    @Query("DELETE FROM hidden_curated_stations WHERE stationId = :stationId")
    suspend fun unhideCuratedStation(stationId: String)

    @Query("DELETE FROM hidden_curated_stations")
    suspend fun clearAllHiddenCuratedStations()
}
