package com.pilove.vodovodinfo.data

import android.util.Log
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.utils.recognizeStreets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat

class NoticeServer : NoticeServerDao {

    val currentNoticeIdUrl = "http://www.viksa.ba/vijesti/str/1"
    val noticeUrl = "http://www.viksa.ba/vijesti/"

    companion object {
        var TAG = DEBUG_TAG
        var currentNumber = 2000
    }

    override suspend fun getNewestNoticeNumber() {
        var doc: Document? = null
        var stringic: String?

        try {

            doc = Jsoup.connect(currentNoticeIdUrl).get()

        } catch (e: Exception) {
            Log.d(TAG, "Error while getting newest notice number: " + e.message)
        }

        doc.let {

            var elemt: Element? =
                it?.getElementsByAttributeValue("id", "obavjestenja")?.first()

            stringic = elemt?.getElementsByClass("row ml-0 mr-0 mb-3 nodec")?.html()?.
            substringBefore("\">")?.
            substringAfterLast("/")

            currentNumber = stringic?.toInt() ?: 0

        }

        Log.d(TAG, "getting current number done: $currentNumber")
    }

    override suspend fun getTodayNotices() {

        var doc: Document? = null
        var elementic : Element? = null

        try {

            doc = Jsoup.connect(noticeUrl + currentNumber).get()
            elementic = Jsoup.connect(noticeUrl + currentNumber).get().body()
        } catch (e: Exception) {
            Log.d("TAG", "error happened while getting today notices: "+e.message)
        }

        doc.let {

            var obavijest = elementic!!.getElementsByClass("col-12 mt-4")

            var stringic: String? = obavijest.text()
            var notice = obavijest.text()
            val dateAndTime = notice
                .substringAfter("Objavljeno")
                .substringBefore("Obavještenje")
                .removePrefix(" ")

            val date = SimpleDateFormat("dd.MM.yyyy 'u' HH:mm")
                .parse(dateAndTime.removePrefix(" "))

            stringic += "\n datum: ${date.toString()}"

            val noticeText = notice.substringAfter("Obavještenje")
            stringic += "\n text: $noticeText"

            recognizeStreets(noticeText).forEach {
                stringic += "\n $it"
            }

            val firstStreet = recognizeStreets(noticeText)[0]
            stringic += "\n street to be recognized : $firstStreet"

            Log.d(TAG, stringic)
        }

    }

}