package com.pilove.vodovodinfo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
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

        // Initialize the SDK
        Places.initialize(applicationContext, R.string.google_maps_api_key.toString())
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

                        val firstStreet = recognizeStreets(noticeText)[0]
                        stringic += "\n street to be recognized : $firstStreet"

                        Log.d("MAINACT", stringic)
                    }
                }
            }
        }

    }

    fun findPlaces(query: String) {
        val placesClient = Places.createClient(this)

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        val token = AutocompleteSessionToken.newInstance()
        Log.d("MAINACT", "token: "+ token.toString())

        // Create a RectangularBounds object.
        val bounds = RectangularBounds.newInstance(
            LatLng(-33.880490, 151.184363),
            LatLng(-33.858754, 151.229596)
        )
        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request =
            FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                //.setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                //.setOrigin(LatLng(-33.8749937, 151.2041382))
                //.setCountries("BA")
               // .setTypeFilter(TypeFilter.ADDRESS)
                .setSessionToken(token)
                .setQuery(query)
                .build()

        val TAG = "MAINACT"
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                for (prediction in response.autocompletePredictions) {
                    Log.i(TAG, prediction.placeId)
                    Log.i(TAG, prediction.getPrimaryText(null).toString())
                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: " + exception.statusCode)
                }
            }
    }
}