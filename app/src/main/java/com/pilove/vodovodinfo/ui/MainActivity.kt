package com.pilove.vodovodinfo.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.*
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.networks.ConnectionLiveData
import com.pilove.vodovodinfo.workers.NewNoticeWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var mSnackBar: Snackbar? = null

    private var wasConnected = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { _, destination, _ ->
                when(destination.id){
                    R.id.settingsFragment, R.id.noticesFragment ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE
                }
            }

        ConnectionLiveData(this).observe(this,  { isConnected ->
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