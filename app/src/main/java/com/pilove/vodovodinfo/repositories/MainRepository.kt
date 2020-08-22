package com.pilove.vodovodinfo.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pilove.vodovodinfo.data.NoticeServer
import com.pilove.vodovodinfo.data.Notice as DbNotice
import com.pilove.vodovodinfo.data.NoticeDAO
import com.pilove.vodovodinfo.networks.ConnectionLiveData
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import javax.inject.Inject

class MainRepository
@Inject constructor(
    val noticeDAO: NoticeDAO,
    val noticeServer: NoticeServer
) {

    suspend fun insertNotice(notice: DbNotice) = noticeDAO.insert(notice)

    suspend fun deleteNotice(notice: DbNotice) = noticeDAO.delete(notice)

    fun getNoticesFromDb(): LiveData<List<DbNotice>>
            = noticeDAO.getNotices()


    fun getNoticesFromServer() = noticeServer.getNotices()


    fun getConnectionLiveData(context: Context) = ConnectionLiveData(context)

}