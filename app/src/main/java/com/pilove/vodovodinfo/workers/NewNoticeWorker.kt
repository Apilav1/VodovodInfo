package com.pilove.vodovodinfo.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.data.NoticeServer
import com.pilove.vodovodinfo.other.Constants
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.other.Constants.DEFAULT_VALUE_FOR_NOTICE_TITLE
import com.pilove.vodovodinfo.other.Constants.KEY_DEFAULT_LOCATION_STREET_NAME
import com.pilove.vodovodinfo.other.Constants.KEY_LATEST_NOTICE_ID
import com.pilove.vodovodinfo.other.Constants.KEY_NOTIFICATIONS_MODE
import com.pilove.vodovodinfo.other.Constants.NOTIFICATIONS_ALL
import com.pilove.vodovodinfo.other.Constants.NOTIFICATIONS_ONLY_MY_STREET
import com.pilove.vodovodinfo.other.Constants.NOTIFICATION_CHANNEL_ID
import com.pilove.vodovodinfo.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.pilove.vodovodinfo.other.Constants.NOTIFICATION_ID
import com.pilove.vodovodinfo.other.Constants.SHARED_PREFERENCES_NAME
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class NewNoticeWorker constructor(
    context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    private var latestNoticeId = 2600

    lateinit var sharedPreferences: SharedPreferences

    private var streetName = ""

    private var notificationMode = 2

    companion object {
        const val myName = "NewNoticeWorker"
        const val TAG = "NOTICEWORKER"
    }

    override fun doWork(): Result {

        setNotificationConfig()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        latestNoticeId = sharedPreferences.getInt(KEY_LATEST_NOTICE_ID, 2600)

        notificationMode = sharedPreferences.getInt(KEY_NOTIFICATIONS_MODE, 2)

        if(notificationMode == NOTIFICATIONS_ONLY_MY_STREET) {
            streetName = sharedPreferences.
                             getString(KEY_DEFAULT_LOCATION_STREET_NAME, "") ?: ""
        }

        try {

            val noticeServer = NoticeServer()

            val idResult = noticeServer.getNewestNoticeId()

            if(idResult > latestNoticeId) {

                val nextNotice = noticeServer.getNextNotice(idResult)

                if(nextNotice.title != DEFAULT_VALUE_FOR_NOTICE_TITLE) {

                    if(nextNotice.date.date == Date().date || nextNotice.dates.equals(Date().date)) {
                        var builder = NotificationCompat
                            .Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                            .setSmallIcon(R.drawable.water_icon_24)
                            .setContentTitle(applicationContext.
                                    getString(R.string.NOTIFICATION_TITLE)+ " "+latestNoticeId)
                            .setContentText(nextNotice.title)
                            .setStyle(NotificationCompat.BigTextStyle()
                                .bigText(nextNotice.text))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setDefaults(Notification.DEFAULT_SOUND)

                        with(NotificationManagerCompat.
                                    from(applicationContext)){
                            // notificationId is a unique int
                            // for each notification that you must define
                            if(notificationMode == NOTIFICATIONS_ONLY_MY_STREET) {
                                if(nextNotice.streets.contains(streetName))
                                    notify(NOTIFICATION_ID, builder.build())
                            } else if(notificationMode == NOTIFICATIONS_ALL) {
                                notify(NOTIFICATION_ID, builder.build())
                            }
                        }

                        Log.d(DEBUG_TAG, "from worker: app notified")
                    }
                }
            }
            else {
                var builder = NotificationCompat
                    .Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.water_icon_24)
                    .setContentTitle("Id: $latestNoticeId, nemam nisza")
                    .setContentText("nema nista")
                    .setStyle(NotificationCompat.BigTextStyle()
                        .bigText("id: $latestNoticeId"))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

                with(NotificationManagerCompat.
                from(applicationContext)){
                    notify(NOTIFICATION_ID, builder.build())
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