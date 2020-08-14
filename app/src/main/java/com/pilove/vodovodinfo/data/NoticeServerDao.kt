package com.pilove.vodovodinfo.data

interface NoticeServerDao {

    suspend fun getNewestNoticeNumber()

    suspend fun getTodayNotices()

}