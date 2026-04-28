package com.jetpackcomposeexecise.timeflies.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_date_table")
data class RecordDateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordDate: String // 格式: "yyyy-MM-dd"
)
