package com.pilove.vodovodinfo.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.pilove.vodovodinfo.data.NoticeServer
import com.pilove.vodovodinfo.data.Notice as DbNotice
import com.pilove.vodovodinfo.data.NoticeDAO
import javax.inject.Inject

class MainRepository
@Inject constructor(
    private val noticeDAO: NoticeDAO,
    private val noticeServer: NoticeServer
) {

    suspend fun insertNotice(notice: DbNotice) = noticeDAO.insert(notice)

    suspend fun deleteNotice(notice: DbNotice) = noticeDAO.delete(notice)

    fun getNoticesFromDb(): LiveData<List<DbNotice>>
            = noticeDAO.getNotices()


    fun getNoticesFromServer(context: Context) = noticeServer.getNotices(context)

}