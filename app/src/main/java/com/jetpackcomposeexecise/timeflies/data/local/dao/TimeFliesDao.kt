package com.jetpackcomposeexecise.timeflies.data.local.dao

import androidx.room.*
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventRecordEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.RecordDateEntity
import com.jetpackcomposeexecise.timeflies.data.local.model.DateWithEvents
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeFliesDao {

    // --- RecordDate ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDate(date: RecordDateEntity): Long

    @Query("SELECT * FROM record_date_table WHERE recordDate = :dateString")
    suspend fun getDateByString(dateString: String): RecordDateEntity?

    // --- Event (Category) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT * FROM event_table ORDER BY id ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM event_table WHERE event = :name LIMIT 1")
    suspend fun getEventByName(name: String): EventEntity?

    // --- EventRecord (Intersection) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: EventRecordEntity)

    @Query("DELETE FROM event_record_table WHERE dateId = :dateId AND eventId = :eventId")
    suspend fun deleteRecord(dateId: Long, eventId: Long)

    // --- Complex Queries ---
    @Transaction
    @Query("SELECT * FROM record_date_table WHERE recordDate = :dateString")
    fun getDateWithEvents(dateString: String): Flow<DateWithEvents?>
}
