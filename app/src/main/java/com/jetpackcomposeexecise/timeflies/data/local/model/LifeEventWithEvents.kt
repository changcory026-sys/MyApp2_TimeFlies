package com.jetpackcomposeexecise.timeflies.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.LifeEventEntity

data class LifeEventWithEvents(
    @Embedded val lifeEvent: LifeEventEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "lifeEventId"
    )
    val events: List<EventEntity>
)
