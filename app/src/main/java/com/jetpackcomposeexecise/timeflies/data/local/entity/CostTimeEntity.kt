package com.jetpackcomposeexecise.timeflies.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cost_time_table",
    primaryKeys = ["dateId", "eventId"], // 联合主键，确保同一天同一个事件只有一条耗时记录
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
data class CostTimeEntity(
    val dateId: Long,
    val eventId: Long,
    val costTime: Double
)
