package com.jetpackcomposeexecise.timeflies.data.local.repository

import com.jetpackcomposeexecise.timeflies.data.local.dao.TimeFliesDao
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventRecordEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.RecordDateEntity
import com.jetpackcomposeexecise.timeflies.data.local.model.DateWithEvents
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeFliesRepository @Inject constructor(
    private val timeFliesDao: TimeFliesDao
) {
    // 获取所有预设事件
    fun getAllEvents(): Flow<List<EventEntity>> = timeFliesDao.getAllEvents()

    // 新增或更新事件类型
    suspend fun upsertEvent(eventName: String) {
        if (timeFliesDao.getEventByName(eventName) == null) {
            timeFliesDao.insertEvent(EventEntity(event = eventName))
        }
    }

    // 获取特定日期的所有记录
    fun getDateWithEvents(dateString: String): Flow<DateWithEvents?> =
        timeFliesDao.getDateWithEvents(dateString)

    /**
     * 保存耗时记录：自动处理日期实体的创建
     */
    suspend fun saveEventRecord(dateString: String, eventName: String, costTime: Double) {
        // 1. 获取或创建日期实体
        var dateEntity = timeFliesDao.getDateByString(dateString)
        val dateId = dateEntity?.id ?: timeFliesDao.insertDate(RecordDateEntity(recordDate = dateString))

        // 2. 获取或创建事件实体
        val eventEntity = timeFliesDao.getEventByName(eventName) 
            ?: return // 理论上 UI 选中的事件应已存在

        // 3. 保存/更新交叉引用记录
        timeFliesDao.insertOrUpdateRecord(
            EventRecordEntity(
                dateId = dateId,
                eventId = eventEntity.id,
                costTime = costTime
            )
        )
    }

    suspend fun deleteEvent(event: EventEntity) = timeFliesDao.deleteEvent(event)
}
