package com.jetpackcomposeexecise.timeflies.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventRecordEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.RecordDateEntity

/**
 * 包装 [EventRecordEntity] 及其关联的 [EventEntity] 详情。
 * 体现了“每个事件记录对应一个具体的事件类型和一段耗时”。
 */
data class EventWithCost(
    @Embedded val record: EventRecordEntity,
    @Relation(
        parentColumn = "eventId",
        entityColumn = "id"
    )
    val eventDetails: EventEntity
)

/**
 * 核心查询模型：包装 [RecordDateEntity] 及其包含的所有事件。
 * 严格遵循“日期 -> 多个事件 (EventWithCost)”的层级关系。
 */
data class DateWithEvents(
    @Embedded val recordDate: RecordDateEntity,
    @Relation(
        entity = EventRecordEntity::class,
        parentColumn = "id",
        entityColumn = "dateId"
    )
    val events: List<EventWithCost>
)
