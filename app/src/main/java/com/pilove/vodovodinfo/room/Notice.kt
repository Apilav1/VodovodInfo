package com.pilove.vodovodinfo.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "Notices")
data class Notice (
    val date: Date,
    val text: String
) {
    @PrimaryKey(autoGenerate = false)
    val id: Int? = null
}