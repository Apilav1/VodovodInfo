package com.pilove.vodovodinfo.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Notice::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class NoticeDatabase: RoomDatabase() {

    abstract fun getNoticeDao(): NoticeDAO

}