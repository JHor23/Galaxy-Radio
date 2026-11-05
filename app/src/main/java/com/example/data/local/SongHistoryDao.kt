package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongHistoryDao {

    @Query("SELECT * FROM song_history WHERE stationId = :stationId OR streamUrl = :streamUrl ORDER BY playedAtTimestamp DESC LIMIT 100")
    fun getHistoryForStation(stationId: String, streamUrl: String): Flow<List<SongHistoryEntity>>

    @Query("SELECT * FROM song_history ORDER BY playedAtTimestamp DESC LIMIT 200")
    fun getAllHistory(): Flow<List<SongHistoryEntity>>

    @Query("SELECT * FROM song_history WHERE stationId = :stationId OR streamUrl = :streamUrl ORDER BY playedAtTimestamp DESC LIMIT 1")
    suspend fun getLatestSongForStation(stationId: String, streamUrl: String): SongHistoryEntity?

    @Query("SELECT COUNT(*) FROM song_history")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: SongHistoryEntity): Long

    @Query("DELETE FROM song_history WHERE stationId = :stationId OR streamUrl = :streamUrl")
    suspend fun deleteHistoryForStation(stationId: String, streamUrl: String)

    @Query("DELETE FROM song_history")
    suspend fun clearAllHistory()
}
