package com.jetpackcomposeexecise.timeflies.data.local.dao

import androidx.room.*
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventRecordEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.LifeEventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.RecordDateEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.TimerSessionEntity
import com.jetpackcomposeexecise.timeflies.data.local.model.DateWithEvents
import com.jetpackcomposeexecise.timeflies.data.local.model.LifeEventWithEvents
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeFliesDao {

    // --- RecordDate ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDate(date: RecordDateEntity): Long

    @Query("SELECT * FROM record_date_table WHERE recordDate = :dateString")
    suspend fun getDateByString(dateString: String): RecordDateEntity?

    // --- LifeEvent (类别) ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLifeEvent(lifeEvent: LifeEventEntity): Long

    @Update
    suspend fun updateLifeEvent(lifeEvent: LifeEventEntity)

    @Delete
    suspend fun deleteLifeEvent(lifeEvent: LifeEventEntity)

    @Query("SELECT * FROM life_event_table ORDER BY id ASC")
    fun getAllLifeEvents(): Flow<List<LifeEventEntity>>

    @Query("SELECT * FROM life_event_table WHERE lifeEvent = :name LIMIT 1")
    suspend fun getLifeEventByName(name: String): LifeEventEntity?

    // --- Event (具体事项) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT * FROM event_table WHERE lifeEventId = :lifeEventId")
    fun getEventsByLifeEventId(lifeEventId: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM event_table ORDER BY id ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM event_table WHERE event = :name LIMIT 1")
    suspend fun getEventByName(name: String): EventEntity?

    // --- EventRecord (关联表) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: EventRecordEntity)

    @Query("DELETE FROM event_record_table WHERE dateId = :dateId AND eventId = :eventId")
    suspend fun deleteRecord(dateId: Long, eventId: Long)

    // --- Timer Session ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimerSession(session: TimerSessionEntity)

    @Query("SELECT * FROM timer_session_table WHERE id = 0")
    fun getTimerSession(): Flow<TimerSessionEntity?>

    @Query("DELETE FROM timer_session_table")
    suspend fun deleteTimerSession()

    // --- Complex Queries ---
    @Transaction
    @Query("SELECT * FROM record_date_table WHERE recordDate = :dateString")
    fun getDateWithEvents(dateString: String): Flow<DateWithEvents?>

    @Transaction
    @Query("SELECT * FROM life_event_table")
    fun getAllLifeEventsWithEvents(): Flow<List<LifeEventWithEvents>>
}
