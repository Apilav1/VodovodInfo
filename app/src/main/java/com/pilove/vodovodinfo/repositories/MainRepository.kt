package com.pilove.vodovodinfo.repositories

import com.pilove.vodovodinfo.room.Notice
import com.pilove.vodovodinfo.room.NoticeDAO
import javax.inject.Inject

class MainRepository
@Inject constructor(
    val noticeDAO: NoticeDAO
) {

    suspend fun insertNotice(notice: Notice) = noticeDAO.insert(notice)

    suspend fun deleteNotice(notice: Notice) = noticeDAO.delete(notice)

    fun getLastTenNotices() = noticeDAO.getLastTenNotices()

    fun getTenNoticesBeforeId(id: Int) = noticeDAO.getTenNoticesBeforeId(id)
}