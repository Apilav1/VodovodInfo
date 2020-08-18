package com.pilove.vodovodinfo.repositories

import android.content.Context
import com.pilove.vodovodinfo.data.NoticeServer
import com.pilove.vodovodinfo.data.Notice as DbNotice
import com.pilove.vodovodinfo.data.NoticeDAO
import com.pilove.vodovodinfo.networks.ConnectionLiveData
import javax.inject.Inject

class MainRepository
@Inject constructor(
    val noticeDAO: NoticeDAO,
    val noticeServer: NoticeServer
) {

    suspend fun insertNotice(notice: DbNotice) = noticeDAO.insert(notice)

    suspend fun deleteNotice(notice: DbNotice) = noticeDAO.delete(notice)

    fun getLastTenNotices() = noticeDAO.getLastTenNotices()

    fun getTenNoticesBeforeId(id: Int) = noticeDAO.getTenNoticesBeforeId(id)

    fun getNoticesFromServer() = noticeServer.getNotices()


    fun getConnectionLiveData(context: Context) = ConnectionLiveData(context)

}