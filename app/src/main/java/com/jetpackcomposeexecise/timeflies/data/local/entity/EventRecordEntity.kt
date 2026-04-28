package com.jetpackcomposeexecise.timeflies.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "event_record_table",
    primaryKeys = ["dateId", "eventId"], // 联合主键确保同一天同一事件仅一条汇总记录
    foreignKeys = [
        ForeignKey(
            entity = RecordDateEntity::class,
            parentColumns = ["id"],
            childColumns = ["dateId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dateId"), Index("eventId")]
)
data class EventRecordEntity(
    val dateId: Long,
    val eventId: Long,
    val costTime: Double // 存储该日该事件的具体耗时
)
