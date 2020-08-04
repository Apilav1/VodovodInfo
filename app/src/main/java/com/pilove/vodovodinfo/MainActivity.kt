package com.pilove.vodovodinfo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pilove.vodovodinfo.utils.recognizeStreets
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var currentNumber = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getNewestNumber()
        button.setOnClickListener {
            getObavijestenja()
        }
    }

    fun getNewestNumber() {
        var doc: Document? = null
        var stringic: String?

        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {

                    doc = Jsoup.connect("http://www.viksa.ba/vijesti/str/1").get()
                } catch (e: Exception) {
                    Log.d("MAINACT", "GRESKA NUMBER " + e.message)
                }
                withContext(Dispatchers.Main) {

                    doc.let {

                        var elemt: Element? =
                            it?.getElementsByAttributeValue("id", "obavjestenja")?.first()

                        stringic = elemt?.getElementsByClass("row ml-0 mr-0 mb-3 nodec")?.html()?.
                        substringBefore("\">")?.
                        substringAfterLast("/")

                        currentNumber = stringic?.toInt() ?: 0
                        Log.d("MAINACT", currentNumber.toString())

                        getObavijestenja()
                    }
                }
            }
        }
    }

    fun getObavijestenja() {

        var doc: Document? = null
        var elementic : Element? = null
        uiScope.launch {
            withContext(Dispatchers.IO) {
                try {

                    doc = Jsoup.connect("http://www.viksa.ba/vijesti/$currentNumber").get()
                    elementic = Jsoup.connect("http://www.viksa.ba/vijesti/$currentNumber").get().body()
                } catch (e: Exception) {
                    Log.d("MAINACT", "GRESKA "+e.message)
                }
                withContext(Dispatchers.Main){

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

                        Log.d("MAINACT", stringic)
                    }
                }
            }
        }

    }
}