package com.jetpackcomposeexecise.timeflies.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "life_event_table",
    indices = [Index(value = ["lifeEvent"], unique = true)]
)
data class LifeEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lifeEvent: String
)
