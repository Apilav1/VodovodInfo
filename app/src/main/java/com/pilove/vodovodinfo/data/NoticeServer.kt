package com.pilove.vodovodinfo.data

import android.util.Log
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.utils.recognizeDates
import com.pilove.vodovodinfo.utils.recognizeStreets
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.Date
import java.text.SimpleDateFormat
import javax.inject.Inject

class NoticeServer @Inject constructor() {

    val currentNoticeIdUrl = "http://www.viksa.ba/vijesti/str/1"
    val noticeUrl = "http://www.viksa.ba/vijesti/"

    private var job: Job = Job()

    companion object {
        var currentNumber = 2000
    }

    fun getTodayNotices(): ArrayList<Notice> {

        var result = ArrayList<Notice>()

        GlobalScope.launch {
            getNewestNoticeNumber()
            result = getNewNotices()
        }
        return result
    }

    private fun getNewestNoticeNumber() {
        var doc: Document? = null
        var stringic: String?

        try {


                doc = Jsoup.connect(currentNoticeIdUrl).get()

                doc.let {

                    var elemt: Element? =
                        it?.getElementsByAttributeValue("id", "obavjestenja")?.first()

                    stringic = elemt?.getElementsByClass("row ml-0 mr-0 mb-3 nodec")?.html()?.
                    substringBefore("\">")?.
                    substringAfterLast("/")

                    currentNumber = stringic?.toInt() ?: 0

                }
                Log.d(DEBUG_TAG, "getting current number done: $currentNumber")

        } catch (e: Exception) {
            Log.d(DEBUG_TAG, "An error while getting newest notice number: " + e.message)
        }
    }

     private fun getNewNotices(): ArrayList<Notice> {

        var doc: Document? = null
        var element : Element?
        var notices = ArrayList<Notice>()

        try {

            element = Jsoup.connect(noticeUrl + currentNumber).get().body()

            /* Notice html structure example
            <div class="col-12 col-lg-8">
                    <div class="col-12 mt-4">
                        <p class="m-0">Objavljeno 14.08.2020 u 15:29</p>
                        <h1 class="">Informacija o planiranim radovima na vodovodnoj mreži za dan  15. i 16.08.2020.  </h1>
                        <p class="">U subotu, 15.08.2020.g., u okviru redovnih aktivnosti na održavanju vodovodnog sistema  izvodit će se radovi na popravkama kvarova u ulicama Alifakovac, Veliki Alifakovac i Brezanska.
U nedelju, 16.08.2020. g., radovi na popravkama kvarova vršit će se u ulici Muhameda ef. Pandže.</p>
                    </div></div>
             */
            doc.let {

                var noticeBody = element!!.getElementsByClass("col-12 mt-4")
                var noticeElement = noticeBody.elementAt(0)
                var timeReleased = noticeElement.child(0).text()
                var title = noticeElement.child(1).text()
                var noticeBodyText = noticeElement.child(2).text()

                val dateAndTime = timeReleased
                    .substringAfter("Objavljeno")
                    .removePrefix(" ")

                val date = SimpleDateFormat("dd.MM.yyyy 'u' HH:mm")
                    .parse(dateAndTime.removePrefix(" "))

                var listOfStreets = ArrayList<String>()

                recognizeStreets(noticeBodyText).forEach {
                    listOfStreets.add(it)
                }

                val dates = ArrayList<Date>()

                recognizeDates(noticeBodyText).forEach {
                    dates.add(it)
                }

                val newNotice = Notice(currentNumber, title, date, noticeBodyText, listOfStreets, dates)

                Log.d(DEBUG_TAG, newNotice.toString())

                notices.add(newNotice)
            }

        } catch (e: Exception) {
            Log.d("TAG", "An error happened while getting today notices: ${e.message}")
        }

        return notices
    }

}