package com.jetpackcomposeexecise.timeflies.data.local.repository

import com.jetpackcomposeexecise.timeflies.data.local.dao.TimeFliesDao
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventRecordEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.LifeEventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.RecordDateEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.TimerSessionEntity
import com.jetpackcomposeexecise.timeflies.data.local.model.DateWithEvents
import com.jetpackcomposeexecise.timeflies.data.local.model.LifeEventWithEvents
import com.jetpackcomposeexecise.timeflies.data.local.model.TimeSlotAggregate
import com.jetpackcomposeexecise.timeflies.data.local.model.TimeSlotWithEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeFliesRepository @Inject constructor(
    private val timeFliesDao: TimeFliesDao
) {
    // --- LifeEvent (类别) 管理 ---
    fun getAllLifeEvents(): Flow<List<LifeEventEntity>> = timeFliesDao.getAllLifeEvents()

    suspend fun updateLifeEvent(lifeEvent: LifeEventEntity) {
        timeFliesDao.updateLifeEvent(lifeEvent)
    }

    /**
     * 获取或创建类别的 ID。Room 的 IGNORE 会返回 -1，这里做了兼容处理。
     */
    suspend fun getOrCreateLifeEventId(name: String): Long {
        val existing = timeFliesDao.getLifeEventByName(name)
        if (existing != null) return existing.id
        
        val newId = timeFliesDao.insertLifeEvent(LifeEventEntity(lifeEvent = name))
        return if (newId == -1L) {
            timeFliesDao.getLifeEventByName(name)?.id ?: -1L
        } else {
            newId
        }
    }

    suspend fun deleteLifeEvent(lifeEvent: LifeEventEntity) {
        timeFliesDao.deleteLifeEvent(lifeEvent)
    }

    fun getAllLifeEventsWithEvents(): Flow<List<LifeEventWithEvents>> = 
        timeFliesDao.getAllLifeEventsWithEvents()

    /**
     * 删除没有事项关联的空类别
     */
    suspend fun deleteEmptyLifeEvents() {
        val allWithEvents = timeFliesDao.getAllLifeEventsWithEvents().firstOrNull()
        allWithEvents?.forEach { group ->
            if (group.events.isEmpty()) {
                timeFliesDao.deleteLifeEvent(group.lifeEvent)
            }
        }
    }

    // --- Event (事项) 管理 ---
    fun getAllEvents(): Flow<List<EventEntity>> = timeFliesDao.getAllEvents()

    fun getEventsByLifeEventId(lifeEventId: Long): Flow<List<EventEntity>> = 
        timeFliesDao.getEventsByLifeEventId(lifeEventId)

    suspend fun insertEventToLifeEvent(lifeEventId: Long, eventName: String) {
        timeFliesDao.insertEvent(EventEntity(lifeEventId = lifeEventId, event = eventName))
    }

    suspend fun updateEvent(event: EventEntity) {
        timeFliesDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: EventEntity) {
        timeFliesDao.deleteEvent(event)
    }

    // --- 计时器会话管理 ---
    fun getTimerSession(): Flow<TimerSessionEntity?> = timeFliesDao.getTimerSession()

    suspend fun updateTimerSession(session: TimerSessionEntity) {
        timeFliesDao.insertTimerSession(session)
    }

    suspend fun clearTimerSession() {
        timeFliesDao.deleteTimerSession()
    }

    // --- 日期与记录管理 ---
    fun getDateWithEvents(dateString: String): Flow<DateWithEvents?> =
        timeFliesDao.getDateWithEvents(dateString)

    /**
     * 保存耗时记录
     * @param timeSlot 时间段（8=8:00-9:00, 9=9:00-10:00, ..., 23=23:00-24:00）
     */
    suspend fun saveEventRecord(dateString: String, eventName: String, timeSlot: Int, costTime: Double) {
        // 1. 获取或创建日期实体
        val dateEntity = timeFliesDao.getDateByString(dateString)
        val dateId = dateEntity?.id ?: timeFliesDao.insertDate(RecordDateEntity(recordDate = dateString))

        // 2. 获取事件实体（假设名称唯一）
        val eventEntity = timeFliesDao.getEventByName(eventName)
            ?: return

        // 3. 保存记录
        timeFliesDao.insertOrUpdateRecord(
            EventRecordEntity(
                dateId = dateId,
                eventId = eventEntity.id,
                timeSlot = timeSlot,
                costTime = costTime
            )
        )
    }

    /**
     * 删除单条耗时记录
     */
    suspend fun deleteEventRecord(dateId: Long, eventId: Long, timeSlot: Int) {
        timeFliesDao.deleteRecord(dateId, eventId, timeSlot)
    }

    // --- 时间段统计查询 ---
    fun getTimeSlotsByDate(dateString: String): Flow<List<TimeSlotWithEvent>> =
        timeFliesDao.getTimeSlotsByDate(dateString)

    fun getTimeSlotAggregates(dateString: String): Flow<List<TimeSlotAggregate>> =
        timeFliesDao.getTimeSlotAggregates(dateString)
}
