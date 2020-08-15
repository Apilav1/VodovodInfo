package com.pilove.vodovodinfo.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.utils.recognizeStreets
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.sql.Date
import java.text.SimpleDateFormat
import javax.inject.Inject

class NoticeServer @Inject constructor() {

    val currentNoticeIdUrl = "http://www.viksa.ba/vijesti/str/1"
    val noticeUrl = "http://www.viksa.ba/vijesti/"

    private var job: Job = Job()

    companion object {
        var TAG = DEBUG_TAG
        var currentNumber = 2000
    }

    fun getTodayNotices() {
        GlobalScope.launch {
            getNewestNoticeNumber()
            getNewNotices()
        }
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
                Log.d(TAG, "getting current number done: $currentNumber")

        } catch (e: Exception) {
            Log.d(TAG, "An error while getting newest notice number: " + e.message)
        }
    }

     private fun getNewNotices(): ArrayList<Notice> {

        var doc: Document? = null
        var element : Element?
        var notices: LiveData<ArrayList<Notice>>

        try {

            element = Jsoup.connect(noticeUrl + currentNumber).get().body()

            doc.let {

                var obavijest = element!!.getElementsByClass("col-12 mt-4")

                var stringic: String? = obavijest.text()
                var noticeStr = obavijest.text()

                val dateAndTime = noticeStr
                    .substringAfter("Objavljeno")
                    .substringBefore("Obavještenje")
                    .removePrefix(" ")

                val date = SimpleDateFormat("dd.MM.yyyy 'u' HH:mm")
                    .parse(dateAndTime.removePrefix(" ")) as Date


                stringic += "\n datum: ${date.toString()}"

                val noticeText = noticeStr.substringAfter("Obavještenje")
                stringic += "\n text: $noticeText"

                var listOfStreets = ArrayList<String>()

                recognizeStreets(noticeText).forEach {
                    stringic += "\n $it"
                    listOfStreets.add(it)
                }

                val firstStreet = recognizeStreets(noticeText)[0]
                stringic += "\n street to be recognized : $firstStreet"

                Log.d(TAG, stringic)

                 Notice(
                    currentNoticeIdUrl,
                    date,
                    noticeText,
                    listOfStreets
                )
            }

        } catch (e: Exception) {
            Log.d("TAG", "An    error happened while getting today notices: "+e.message)
        }
        return ArrayList<Notice>()
    }

}