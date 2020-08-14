package com.pilove.vodovodinfo.repositories

import com.pilove.vodovodinfo.data.NoticeServer
import com.pilove.vodovodinfo.db.Notice
import com.pilove.vodovodinfo.db.NoticeDAO
import javax.inject.Inject

class MainRepository
@Inject constructor(
    val noticeDAO: NoticeDAO,
    val noticeServer: NoticeServer
) {

    suspend fun insertNotice(notice: Notice) = noticeDAO.insert(notice)

    suspend fun deleteNotice(notice: Notice) = noticeDAO.delete(notice)

    fun getLastTenNotices() = noticeDAO.getLastTenNotices()

    fun getTenNoticesBeforeId(id: Int) = noticeDAO.getTenNoticesBeforeId(id)

    suspend fun getNoticesFromServer() {
        noticeServer.getNewestNoticeNumber()
        noticeServer.getTodayNotices()
    }
}