package com.pilove.vodovodinfo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pilove.vodovodinfo.data.Converters
import com.pilove.vodovodinfo.data.Notice
import com.pilove.vodovodinfo.data.NoticeDAO

@Database(
    entities = [Notice::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class NoticeDatabase: RoomDatabase() {

    abstract fun getNoticeDao(): NoticeDAO

}