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
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
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
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.repositories.MainRepository
import com.pilove.vodovodinfo.ui.viewModels.MainViewModel
import com.pilove.vodovodinfo.utils.recognizeStreets
import com.pilove.vodovodinfo.workers.NewNoticeWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notices.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var mSnackBar: Snackbar? = null

    private var wasConnected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

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

        WorkManager.getInstance(this).cancelAllWorkByTag(NewNoticeWorker.myName)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val getNotified = PeriodicWorkRequestBuilder<NewNoticeWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
            )
            .addTag(NewNoticeWorker.myName)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(NewNoticeWorker.myName,
                                        ExistingPeriodicWorkPolicy.REPLACE, getNotified)

    }
}