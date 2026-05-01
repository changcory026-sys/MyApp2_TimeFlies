package com.jetpackcomposeexecise.timeflies.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event_table",
    foreignKeys = [
        ForeignKey(
            entity = LifeEventEntity::class,
            parentColumns = ["id"],
            childColumns = ["lifeEventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // 添加唯一索引，确保同一个类别下不会有重复的事项名称
    indices = [
        Index("lifeEventId"),
        Index(value = ["lifeEventId", "event"], unique = true)
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lifeEventId: Long,
    val event: String
)
