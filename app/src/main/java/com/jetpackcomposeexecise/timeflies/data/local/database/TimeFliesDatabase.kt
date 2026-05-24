package com.jetpackcomposeexecise.timeflies.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jetpackcomposeexecise.timeflies.data.local.dao.TimeFliesDao
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventRecordEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.LifeEventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.RecordDateEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.TimerSessionEntity

@Database(
    entities = [
        RecordDateEntity::class,
        EventEntity::class,
        EventRecordEntity::class,
        LifeEventEntity::class,
        TimerSessionEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class TimeFliesDatabase : RoomDatabase() {
    abstract fun timeFliesDao(): TimeFliesDao
}
