package cz.bezcisobe.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceDao {
    @Query("SELECT * FROM races ORDER BY date ASC")
    fun observeRaces(): Flow<List<RaceEntity>>

    @Query("SELECT * FROM races WHERE id = :id")
    suspend fun getById(id: String): RaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(races: List<RaceEntity>)

    @Query("DELETE FROM races")
    suspend fun clear()
}
