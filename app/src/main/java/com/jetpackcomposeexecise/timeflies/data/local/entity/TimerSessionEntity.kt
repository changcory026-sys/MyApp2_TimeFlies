package com.jetpackcomposeexecise.timeflies.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_session_table")
data class TimerSessionEntity(
    @PrimaryKey val id: Int = 0, // Only one session at a time
    val eventName: String?,
    val startTimeMillis: Long?, // Null means paused or not started
    val accumulatedSeconds: Long, // Seconds accumulated before pause/exit
    val isRunning: Boolean = false
)
