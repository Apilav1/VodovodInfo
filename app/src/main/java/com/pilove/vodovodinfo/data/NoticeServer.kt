package com.pilove.vodovodinfo.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.utils.recognizeDates
import com.pilove.vodovodinfo.utils.recognizeStreets
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

    private var job: Job = Job()

    private var latestNoticeId = 2500

    private var errorHappened = false

    fun getNotices(): LiveData<List<Notice>> {

        val result = MediatorLiveData<List<Notice>>()

        errorHappened = false

        latestNoticeId = getNewestNoticeId()

        if(!errorHappened) {
            result.postValue(getNewNotices())
        }
        else{
            result.postValue(listOf())
        }
        return result
    }

    private fun getNewestNoticeId(): Int  = runBlocking(Dispatchers.IO) {
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

    private fun getNewNotices(): List<Notice> = runBlocking(Dispatchers.IO) {

        var notices = ArrayList<Notice>()
        var noticeId = latestNoticeId

        withTimeout(5000L) {
            while (true) {

                if(errorHappened) break

                var notice = getNextNotice(noticeId--)
                var noticeForBase = if (!notices.isEmpty()) notices.first()
                else null

                if (!notices.isEmpty() &&
                    notice.dateForComparison.before(noticeForBase?.dateForComparison) &&
                    notice.text != "default"
                ) {
                    break
                } else if (notice.text != "default") {
                    notices.add(notice)
                }
            }
        }

        notices.forEach {
            Log.d(DEBUG_TAG, it.toString())
        }

        notices as List<Notice>
    }

    private fun getNextNotice(noticeId: Int): Notice {

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

                recognizeDates(noticeBodyText).forEach {
                    dates.add(it)
                }

                newNotice = Notice(noticeId, title, date, dateForComparison, noticeBodyText, listOfStreets, dates)

                return newNotice
            }

        } catch (e: Exception) {
            Log.d(DEBUG_TAG, "Error happened while getting today notices: ${e.message}")
            errorHappened = true
        }

        return Notice()
    }

}