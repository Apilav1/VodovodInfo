package com.pilove.vodovodinfo.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.pilove.vodovodinfo.BaseApplication
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.other.Constants.DEFAULT_VALUE_FOR_NOTICE_TITLE
import com.pilove.vodovodinfo.other.Constants.KEY_LATEST_NOTICE_ID
import com.pilove.vodovodinfo.other.Constants.SHARED_PREFERENCES_NAME
import com.pilove.vodovodinfo.utils.recognizeDates
import com.pilove.vodovodinfo.utils.recognizeStreets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/* Notice html structure example
            <div class="col-12 col-lg-8">
              <div class="col-12 mt-4">
                <p class="m-0">Objavljeno 14.08.2020 u 15:29</p>
                <h1 class="">Informacija o planiranim radovima
                    na vodovodnoj mreži za dan  15. i 16.08.2020.  </h1>
                <p class="">U subotu, 15.08.2020.g., u okviru redovnih aktivnosti
                 na održavanju vodovodnog sistema  izvodit će se radovi na popravkama
                 kvarova u ulicama Alifakovac, Veliki Alifakovac i Brezanska.
                 U nedelju, 16.08.2020. g., radovi na popravkama kvarova vršit
                 će se u ulici Muhameda ef. Pandže.</p>
              </div>
             </div>
  */
class NoticeServer @Inject constructor() {

    val currentNoticeIdUrl = "http://www.viksa.ba/vijesti/str/1"
    val noticeUrl = "http://www.viksa.ba/vijesti/"

    private var errorHappened = false

    lateinit var sharedPreferences: SharedPreferences

    fun getNotices(context: Context): LiveData<List<Notice>> {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val result = MutableLiveData<List<Notice>>()

        errorHappened = false

        latestNoticeId = getNewestNoticeId()

        sharedPreferences.edit()
            .putInt(KEY_LATEST_NOTICE_ID, latestNoticeId)


        if(!errorHappened) {
            result.postValue(getNewNotices())
        }
        else{
            result.postValue(listOf())
        }
        return result
    }

    fun getNewestNoticeId(): Int  = runBlocking(Dispatchers.IO) {
        var doc: Document? = null
        var result: String? = null

        try {
            doc = Jsoup.connect(currentNoticeIdUrl).get()

            doc.let {

                var elemt: Element? =
                    it?.getElementsByAttributeValue("id", "obavjestenja")?.first()

                result = elemt?.getElementsByClass("row ml-0 mr-0 mb-3 nodec")?.html()?.
                substringBefore("\">")?.
                substringAfterLast("/")

                Log.d(DEBUG_TAG, result)

            }

        } catch (e: Exception) {
            Log.d(DEBUG_TAG, "Error while getting newest notice number: " + e.message)
            errorHappened = true
        }

        result?.toInt() ?: 0
    }

    fun getNewNotices(): List<Notice> = runBlocking(Dispatchers.IO) {

        var notices = ArrayList<Notice>()
        var noticeId = latestNoticeId

        withTimeout(5000L) {
            while (true) {

                if(errorHappened) break

                var notice = getNextNotice(noticeId--)
                var noticeForBase = if (notices.isNotEmpty()) notices.first()
                else null

                if (notices.isNotEmpty()) {
                    if(notice.dateForComparison.before(noticeForBase?.dateForComparison) &&
                        notice.text != DEFAULT_VALUE_FOR_NOTICE_TITLE) {

                        if(notice.dates.size > 0 &&
                            notice.dates.any { it == noticeForBase?.dateForComparison }) {
                            notices.add(notice)
                        } else {
                            break
                        }
                    } else if(notice.dateForComparison == noticeForBase?.dateForComparison) {
                        notices.add(notice)
                    }
                } else if (notice.text != DEFAULT_VALUE_FOR_NOTICE_TITLE) {
                    notices.add(notice)
                }
            }
        }

        notices.forEach {
            Log.d(DEBUG_TAG, it.toString())
        }

        notices as List<Notice>
    }

    fun getNextNotice(noticeId: Int): Notice {

        var element : Element?
        var doc: Document? = null
        var newNotice: Notice

        try {

            element = Jsoup.connect(noticeUrl + noticeId).get().body()

            doc.let {
                //example of html structure that is being scraped is at the beginning of file

                var noticeBody = element!!.getElementsByClass("col-12 mt-4")
                var noticeElement = noticeBody.elementAt(0)
                var timeReleased = noticeElement.child(0).text()
                var title = noticeElement.child(1).text()
                var noticeBodyText = noticeElement.child(2).text()

                val titleSplit = title.split(" ").toTypedArray()

                if(titleSplit.first() == "Služba")
                    return@let

                val dateAndTime = timeReleased
                    .substringAfter("Objavljeno")
                    .removePrefix(" ")

                val date = SimpleDateFormat("dd.MM.yyyy 'u' HH:mm")
                    .parse(dateAndTime.removePrefix(" "))

                val dateForComparison = SimpleDateFormat("dd.MM.yyyy")
                    .parse(dateAndTime.substringBefore(" "))

                var listOfStreets = ArrayList<String>()

                recognizeStreets(noticeBodyText).forEach {
                    listOfStreets.add(it)
                }

                val dates = ArrayList<Date>()

                recognizeDates("$title $noticeBodyText").forEach {
                    dates.add(it)
                }

                newNotice = Notice(noticeId, title,
                    date!!, dateForComparison!!, noticeBodyText, listOfStreets, dates)

                return newNotice
            }

        } catch (e: Exception) {
            Log.d(DEBUG_TAG, "Error happened while getting today notices: ${e.message}")
            errorHappened = true
        }

        return Notice()
    }

    companion object {
        var latestNoticeId = 2599
    }

}