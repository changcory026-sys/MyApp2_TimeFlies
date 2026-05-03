package com.jetpackcomposeexecise.timeflies.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "event_record_table",
    primaryKeys = ["dateId", "eventId", "timeSlot"], // 联合主键确保同一天同一事件同一时间段仅一条记录
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
    indices = [Index("dateId"), Index("eventId"), Index("timeSlot")]
)
data class EventRecordEntity(
    val dateId: Long,
    val eventId: Long,
    val timeSlot: Int, // 时间段：8=8:00-9:00, 9=9:00-10:00, ..., 23=23:00-24:00
    val costTime: Double // 存储该时间段该事件的耗时（小时）
)
