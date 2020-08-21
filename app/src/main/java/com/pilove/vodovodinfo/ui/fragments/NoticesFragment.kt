package com.pilove.vodovodinfo.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.util.Util
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.adapters.NoticeAdapter
import com.pilove.vodovodinfo.data.Notice
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.other.Constants.FASTEST_LOCATION_INTERVAL
import com.pilove.vodovodinfo.other.Constants.LOCATION_UPDATE_INTERVAL
import com.pilove.vodovodinfo.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.pilove.vodovodinfo.ui.viewModels.MainViewModel
import com.pilove.vodovodinfo.utils.PermissionsUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_notices.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class NoticesFragment : Fragment(R.layout.fragment_notices),
                    EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var noticeAdapter: NoticeAdapter

    private var map: GoogleMap? = null

    private var isConnected: Boolean = false

    private var locationPermissionGranted = true

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var lastKnownLocation: Location? = null

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()

        mapView.onCreate(savedInstanceState)


        viewModel.getConnectionStatus(requireContext())

        setupRecycleView()

        viewModel.getNotices()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        viewModel.notices.observe(viewLifecycleOwner, Observer {
            if(it == null || it.isEmpty()) return@Observer
             showProgressBar()
             noticeAdapter.submitList(it as ArrayList<Notice>)
             noticeAdapter.notifyDataSetChanged()
        })


        viewModel.connectionLiveData.observe(viewLifecycleOwner, Observer { connected ->
             isConnected = connected
             if(connected) {
                 showProgressBar()
                 viewModel.getNotices()

                 if(map == null)
                 mapView.getMapAsync {
                     map = it
                     CoroutineScope(Dispatchers.Default).launch {
                         delay(3_000)
                         getDeviceLocation()
                     }
//                     map?.setOnMapLoadedCallback {
//                         delay(1_000)
//                         getDeviceLocation()
//                     }
                 }
             }
        })

        textView.setOnClickListener {
            Log.d(TAG, "GETTING location")
                getDeviceLocation()
        }

        btnResizeMapDown.setOnClickListener {
            toggleMap()
        }
        btnResizeMapUp.setOnClickListener {
            toggleMap()
        }
    }

    private fun showProgressBar() = GlobalScope.launch(Main) {
        progress_bar.visibility = View.VISIBLE
        delay(1000L)
        progress_bar.visibility = View.GONE
    }

    private fun toggleMap() {
        if(mapView.visibility == View.VISIBLE) {
            mapView.visibility = View.GONE
            blackLineAboveTheMap.visibility = View.GONE
            btnResizeMapDown.visibility = View.GONE
            btnResizeMapUp.visibility = View.VISIBLE
            bottomTab.visibility = View.VISIBLE
            googleMapsLogo.visibility = View.VISIBLE
        } else {
            mapView.visibility = View.VISIBLE
            blackLineAboveTheMap.visibility = View.VISIBLE
            btnResizeMapDown.visibility = View.VISIBLE
            btnResizeMapUp.visibility = View.GONE
            bottomTab.visibility = View.GONE
            googleMapsLogo.visibility = View.GONE
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
        mapView?.onSaveInstanceState(outState)
    }

    private fun requestPermissions() {
        if(PermissionsUtil.hasLocationPermissions(requireContext())) return
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun getDeviceLocation() {

//        val request = LocationRequest().apply {
//            interval = LOCATION_UPDATE_INTERVAL
//            fastestInterval = FASTEST_LOCATION_INTERVAL
//            priority = PRIORITY_HIGH_ACCURACY
//        }
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//
//            return
//        }
//        fusedLocationProviderClient.requestLocationUpdates(
//            request,
//            locationCallback,
//            Looper.getMainLooper()
//        )
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            locationPermissionGranted = PermissionsUtil.hasLocationPermissions(requireContext())
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "TASK is success ${task.result?.latitude} ${task.result?.longitude}")
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                            map?.isMyLocationEnabled = true
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

//    val locationCallback = object : LocationCallback() {
//        override fun onLocationResult(result: LocationResult?) {
//            super.onLocationResult(result)
//                result?.locations?.let { locations ->
//                    Log.d(TAG, "locaije: "+locations.toString())
//                }
//        }
//    }

    companion object {
        private const val DEFAULT_ZOOM = 15
        private const val TAG = "DEBUGIICCMAPIIIC"
    }
}