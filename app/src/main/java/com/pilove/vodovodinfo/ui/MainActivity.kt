package com.pilove.vodovodinfo.ui

import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.networks.ConnectionLiveData
import com.pilove.vodovodinfo.repositories.MainRepository
import com.pilove.vodovodinfo.ui.viewModels.MainViewModel
import com.pilove.vodovodinfo.utils.recognizeStreets
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notices.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var mSnackBar: Snackbar? = null

    private var wasConnected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ConnectionLiveData(this).observe(this, Observer { isConnected ->
            if(!isConnected) {
                val message = "Please check your internet connection"
                mSnackBar = Snackbar.make(findViewById(android.R.id.content),
                    message, Snackbar.LENGTH_LONG)
                mSnackBar?.apply {
                    duration = BaseTransientBottomBar.LENGTH_INDEFINITE
                    show()
                }
                wasConnected = false
            } else {
                mSnackBar?.dismiss()
                val message = "Connected"

                if(!wasConnected)
                    mSnackBar = Snackbar.make(findViewById(android.R.id.content),
                        message, Snackbar.LENGTH_LONG)
                    mSnackBar?.apply {
                        duration = BaseTransientBottomBar.LENGTH_LONG
                        setTextColor(Color.WHITE)
                        setBackgroundTint(resources.getColor(R.color.snackBarGreen))
                        show()
                    }
                wasConnected = true
            }
        })

        // Initialize the SDK
        //Places.initialize(applicationContext, R.string.google_maps_api_key.toString())

    }

    //    fun findPlaces(query: String) {
//        val placesClient = Places.createClient(this)
//
//        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
//        // and once again when the user makes a selection (for example when calling fetchPlace()).
//        val token = AutocompleteSessionToken.newInstance()
//        Log.d("MAINACT", "token: "+ token.toString())
//
//        // Create a RectangularBounds object.
//        val bounds = RectangularBounds.newInstance(
//            LatLng(-33.880490, 151.184363),
//            LatLng(-33.858754, 151.229596)
//        )
//        // Use the builder to create a FindAutocompletePredictionsRequest.
//        val request =
//            FindAutocompletePredictionsRequest.builder()
//                // Call either setLocationBias() OR setLocationRestriction().
//                //.setLocationBias(bounds)
//                //.setLocationRestriction(bounds)
//                //.setOrigin(LatLng(-33.8749937, 151.2041382))
//                //.setCountries("BA")
//               // .setTypeFilter(TypeFilter.ADDRESS)
//                .setSessionToken(token)
//                .setQuery(query)
//                .build()
//
//        val TAG = "MAINACT"
//        placesClient.findAutocompletePredictions(request)
//            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
//                for (prediction in response.autocompletePredictions) {
//                    Log.i(TAG, prediction.placeId)
//                    Log.i(TAG, prediction.getPrimaryText(null).toString())
//                }
//            }.addOnFailureListener { exception: Exception? ->
//                if (exception is ApiException) {
//                    Log.e(TAG, "Place not found: " + exception.statusCode)
//                }
//            }
//    }
}