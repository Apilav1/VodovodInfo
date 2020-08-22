package com.pilove.vodovodinfo.ui.fragments

import android.Manifest
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.adapters.NoticeAdapter
import com.pilove.vodovodinfo.data.Notice
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

    private var notices: ArrayList<Notice>? = null

    private var map: GoogleMap? = null

    private var isConnected: Boolean = false

    private var locationPermissionGranted = true

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var lastKnownLocation: Location? = null

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    private val bounds = LatLngBounds.builder()

    private var isSetOneOrMore = false

    private var isMapSet = false

    private var isPermissionGranted: Boolean = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(PermissionsUtil.hasLocationPermissions(requireContext())){
            isPermissionGranted = true
        } else {
            requestPermissions()
        }

        setupRecycleView()

        viewModel.notices.observe(viewLifecycleOwner, Observer {
            it?.let {
                notices = it as ArrayList<Notice>
                noticeAdapter.submitList(notices!!)
                noticeAdapter.notifyDataSetChanged()
                viewModel.insertNotices(it)
                progress_bar.visibility = View.GONE

                if(viewModel.isConnected) {
                    mapView.getMapAsync { googleMap ->
                        map = googleMap
                    }
                    getDeviceLocation()
                }
            }
        })

        mapView.onCreate(savedInstanceState)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        btnResizeMapDown.setOnClickListener {
            toggleMap()
        }
        btnResizeMapUp.setOnClickListener {
            toggleMap()
        }
    }

    private fun mapSetUp() = CoroutineScope(Dispatchers.Default).launch {

        notices?.forEach { notice ->
            notice.streets.forEach { street ->
                geoLocate(street)
            }
        }

        delay(2000L)
        withContext(Main) {
            if (isSetOneOrMore)
                zoomToSeeWholeTrack()
        }
        isMapSet = true
    }

    private suspend fun geoLocate(street: String) = GlobalScope.launch(Dispatchers.Default)     {
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
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "geoLocate error: ${e.message}")
        }
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

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height*0.10).toInt()
            )
        )
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

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
         isPermissionGranted = true
    }

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
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.isMyLocationEnabled = true
                        }
                        mapSetUp()
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
        private const val DEFAULT_ZOOM = 13
        private const val TAG = "MAINACT"
    }
}