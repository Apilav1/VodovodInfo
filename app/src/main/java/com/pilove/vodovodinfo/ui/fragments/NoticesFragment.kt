package com.pilove.vodovodinfo.ui.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.adapters.NoticeAdapter
import com.pilove.vodovodinfo.data.Notice
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.other.Constants.KEY_DEFAULT_LOCATION_LAT
import com.pilove.vodovodinfo.other.Constants.KEY_DEFAULT_LOCATION_LNG
import com.pilove.vodovodinfo.other.Constants.KEY_DEFAULT_LOCATION_STREET_NAME
import com.pilove.vodovodinfo.ui.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notices.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject

const val ERROR_DIALOG_TAG = "ErrorDialog"

@AndroidEntryPoint
class NoticesFragment : Fragment(R.layout.fragment_notices) {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var noticeAdapter: NoticeAdapter

    private var notices: ArrayList<Notice>? = null

    private var map: GoogleMap? = null

    private val bounds = LatLngBounds.builder()

    private var isSetOneOrMore = false

    private var isMapSet = false

    private var isGPRFailed: Boolean = false

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var currentLocationLatLng : LatLng

    private lateinit var mapJob: Job

    private var isThisResumed: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(savedInstanceState != null) {
            val errorDialog = parentFragmentManager.findFragmentByTag(
                ERROR_DIALOG_TAG
            ) as ErrorDialog?
            errorDialog?.setYesListener {
                mapSetup()
            }
        }

        setupRecycleView()

        requireActivity().bottomNavigationView?.visibility = View.VISIBLE

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        viewModel.notices.observe(viewLifecycleOwner, Observer {
            it?.let {

                val maxIdOldNotices = noticeAdapter.items.maxByOrNull { n -> n.id }
                val maxIdNewNotice = it.maxByOrNull { n -> n.id }

                if(maxIdOldNotices?.id != null && maxIdNewNotice?.id != null &&
                    maxIdOldNotices.id >= maxIdNewNotice.id && !isThisResumed) return@Observer

                isThisResumed = false
                Log.d(DEBUG_TAG, "DOING ADAPTER")
                if(isThisResumed)
                    setupRecycleView()

                notices = it as ArrayList<Notice>
                noticeAdapter.submitList(notices!!)
                noticeAdapter.notifyDataSetChanged()
                viewModel.insertNotices(it)
                pbNotices.visibility = View.GONE

                if(viewModel.isConnected && !isMapSet && mapView.isVisible) {
                    startMap()
                }
            }
        })

        mapView.onCreate(savedInstanceState)

        btnResizeMapUp.setOnClickListener {
            toggleMap()
        }
    }

    private fun zoomToDefaultLocation() {
        currentLocationLatLng = LatLng(sharedPreferences
            .getString(KEY_DEFAULT_LOCATION_LAT, "0.0")!!.toDouble(),
            sharedPreferences.getString(KEY_DEFAULT_LOCATION_LNG, "0.0")!!.toDouble()
        )

        if(currentLocationLatLng.latitude != 0.0) {
            map?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(currentLocationLatLng.latitude, currentLocationLatLng.longitude),
                    DEFAULT_ZOOM.toFloat()
                )
            )
        } else {
            Log.d(DEBUG_TAG, "PRAZAN LOCATION")
        }
    }

    private fun startMap() {
        pbMapNotices.visibility = View.VISIBLE
        mapView.getMapAsync { googleMap ->
            map = googleMap
            zoomToDefaultLocation()
            mapSetup()
        }
    }

    private fun mapSetup() {

        if(currentLocationLatLng.latitude != 0.0) {
            map?.apply {
                val defaultLocation = LatLng(
                    currentLocationLatLng.latitude,
                    currentLocationLatLng.longitude
                )
                val defaultStreet =
                    sharedPreferences.getString(KEY_DEFAULT_LOCATION_STREET_NAME, "")
                addMarker(
                    MarkerOptions()
                        .position(defaultLocation)
                        .title(defaultStreet)
                )
            }
        }

        mapJob = GlobalScope.launch {
            notices?.forEach { notice ->
                notice.streets.forEach { street ->
                    if(!isGPRFailed) {
                        val job = geoLocate(street)
                        if (job.isCancelled) {
                            showErrorDialog()
                        }
                    }
                }
            }
            if (isSetOneOrMore)
                zoomToSeeWholeTrack()

            isMapSet = true
        }
    }

    private suspend fun geoLocate(street: String) = GlobalScope.launch(Dispatchers.IO) {

        if(!isActive) return@launch

        Log.d(DEBUG_TAG, "GEOLOCATING $street")
        val geocoder = Geocoder(requireContext())
        try {
            val result = geocoder.getFromLocationName("Sarajevo $street", 1)
            val address : Address = result[0]
            if (result.isNotEmpty()) {
                val latLng = LatLng(address.latitude, address.longitude)
                bounds.include(latLng)
                isSetOneOrMore = true
                withContext(Main) {
                    drawCircle(latLng)
                    zoomToSeeWholeTrack()
                }
              }
        } catch (e: Exception) {
            Log.d(TAG, "geoLocate error: ${e.message}")

            this.cancel("Error", e)

            if(e.message.equals("grpc failed")) {
                isGPRFailed = true
            }
        }
    }

    private fun showErrorDialog() {
        ErrorDialog().apply {
            setYesListener {
                mapSetup()
            }
        }.show(parentFragmentManager, ERROR_DIALOG_TAG)
    }

    private fun drawCircle(point: LatLng) {

        map!!.addCircle(
            CircleOptions()
                .center(point)
                .radius(500.0)
                .strokeColor(Color.RED)
                .fillColor(0x30ff0000)
                .strokeWidth(2F)
             )
    }

    private fun zoomToSeeWholeTrack() {

        pbMapNotices.visibility = View.GONE

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height*0.15).toInt()
            )
        )
    }

    private fun toggleMap() {
        TransitionManager.beginDelayedTransition(mapView, AutoTransition())
        if(mapView.visibility == View.VISIBLE) {
            mapView.visibility = View.GONE
            blackLineAboveTheMap.visibility = View.GONE
            btnResizeMapUp.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_up_24)
            pbMapNotices.visibility = View.GONE
            if(this::mapJob.isInitialized && mapJob.isActive) {
                mapJob.cancel("map shut down")
            }
        } else {
            mapView.visibility = View.VISIBLE
            blackLineAboveTheMap.visibility = View.VISIBLE
            btnResizeMapUp.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_down_24)

            if(!isMapSet) startMap()
        }
    }



    private fun setupRecycleView() = rvNotices.apply {
        noticeAdapter = NoticeAdapter()
        adapter = noticeAdapter
        layoutManager = LinearLayoutManager(requireContext())
        this.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        isThisResumed = true
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isMapSet", true)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        isMapSet = savedInstanceState?.getBoolean("isMapSet", false) ?: false
        Log.d(DEBUG_TAG, "VIEW RESTORED isMapSet is $isMapSet")
    }

    companion object {
        private const val DEFAULT_ZOOM = 13
        private const val TAG = "MAINACT"
    }
}