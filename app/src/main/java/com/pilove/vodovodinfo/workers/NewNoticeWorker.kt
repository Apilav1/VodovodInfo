package com.pilove.vodovodinfo.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.data.NoticeServer
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.other.Constants.DEFAULT_VALUE_FOR_NOTICE_TITLE
import com.pilove.vodovodinfo.other.Constants.NOTIFICATION_CHANNEL_ID
import com.pilove.vodovodinfo.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.pilove.vodovodinfo.other.Constants.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class NewNoticeWorker constructor(
    context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    private var isNotificationConfigSet = false
    private var i = 0
    private var currentNoticeId = 2600

    companion object {
        const val myName = "NewNoticeWorker"
        const val TAG = "NOTICEWORKER"
    }

    override fun doWork(): Result {

        if(!isNotificationConfigSet) {
            setNotificationConfig()
            isNotificationConfigSet = true
        }

        try {

            val noticeServer = NoticeServer()

            val idResult = noticeServer.getNewestNoticeId()

            if(idResult > currentNoticeId) {
                currentNoticeId = idResult

                val nextNotice = noticeServer.getNextNotice(currentNoticeId)
                if(nextNotice.title != DEFAULT_VALUE_FOR_NOTICE_TITLE) {

                    if(nextNotice.date.date == Date().date) {

                        var builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.drawable.google_maps_icon_120x80)
                            .setContentTitle("Obavještenje")
                            .setContentText(nextNotice.title)
                            .setStyle(NotificationCompat.BigTextStyle()
                                .bigText(nextNotice.text))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)

                        with(NotificationManagerCompat.from(applicationContext)) {
                            // notificationId is a unique int for each notification that you must define
                            notify(NOTIFICATION_ID, builder.build())
                        }

                        Log.d(DEBUG_TAG, "from worker: app notified")
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "error: {${e.message}")
        }

        return Result.success()
    }

    private fun setNotificationConfig() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}