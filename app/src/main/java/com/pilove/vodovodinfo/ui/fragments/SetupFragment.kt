package com.pilove.vodovodinfo.ui.fragments

import android.Manifest
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.other.Constants
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.other.Constants.KEY_DEFAULT_LOCATION_LAT
import com.pilove.vodovodinfo.other.Constants.KEY_DEFAULT_LOCATION_LNG
import com.pilove.vodovodinfo.other.Constants.KEY_DEFAULT_LOCATION_STREET_NAME
import com.pilove.vodovodinfo.other.Constants.KEY_IS_FIRST_TIME
import com.pilove.vodovodinfo.utils.PermissionsUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_location_setup.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_location_setup),
                            EasyPermissions.PermissionCallbacks {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject
    var isFirstAppTime = true

    private var defaultLocationStreetName = ""

    private var isPermissionGranted: Boolean = false

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var lastKnownLocation: Location? = null

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    private var map: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(PermissionsUtil.hasLocationPermissions(requireContext())){
            isPermissionGranted = true
            mapSetup()
        } else {
            requestPermissions()
        }

        mapViewSetup.onCreate(savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())


        if(!isFirstAppTime) {
            val navigationOpt = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_noticesFragment,
                savedInstanceState,
                navigationOpt
            )
        }

        mapSetup()

        tvNext.setOnClickListener {
            if(it.isVisible) {
                if(writeToSharedPref()) {
                    findNavController().navigate(R.id.action_setupFragment_to_noticesFragment)
                }
            }
        }

        btnRetrySetup.setOnClickListener {
            if(it.isVisible) {
                geoLocate()
            }
        }

        tvSkip.setOnClickListener {
            if(it.isVisible) {
                if(writeToSharedPref()) {
                    findNavController().navigate(R.id.action_setupFragment_to_noticesFragment)
                }
            }
        }
    }

    private fun mapSetup() {
        mapViewSetup?.getMapAsync { googleMap ->
            map = googleMap
            getDeviceLocation()
        }
    }

    private fun writeToSharedPref(): Boolean {
        if(lastKnownLocation == null) return false

            sharedPreferences.edit()
                .putString(KEY_DEFAULT_LOCATION_STREET_NAME, defaultLocationStreetName)
                .putString(KEY_DEFAULT_LOCATION_LAT, lastKnownLocation?.latitude.toString())
                .putString(KEY_DEFAULT_LOCATION_LNG, lastKnownLocation?.longitude.toString())
                .putBoolean(KEY_IS_FIRST_TIME, false)
                .apply()

        return true
    }

    override fun onResume() {
        super.onResume()
        mapViewSetup?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapViewSetup?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapViewSetup?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapViewSetup?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapViewSetup?.onSaveInstanceState(outState)
    }


    private fun requestPermissions() {
        if(PermissionsUtil.hasLocationPermissions(requireContext())) return
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.PERMISSION_REQUEST_TEXT),
                Constants.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.PERMISSION_REQUEST_TEXT),
                Constants.REQUEST_CODE_LOCATION_PERMISSION,
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
        mapSetup()
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
            if (isPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(
                            DEBUG_TAG,
                            "TASK is success ${task.result?.latitude} ${task.result?.longitude}")
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.isMyLocationEnabled = true
                        }
                        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                        geoLocate()

                    } else {
                        Log.d(DEBUG_TAG, "Current location is null. Using defaults.")
                        Log.e(DEBUG_TAG, "Exception: %s", task.exception)
                        map?.moveCamera(
                            CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun geoLocate() = GlobalScope.launch(Dispatchers.IO) {
        if(lastKnownLocation == null || lastKnownLocation?.latitude == null) return@launch

        val geocoder = Geocoder(requireContext())
        try {
            val result = geocoder.getFromLocation(lastKnownLocation!!.latitude,
                                                    lastKnownLocation!!.longitude, 1)
            val address : Address = result[0]
            if (result.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    tvAddress.text = address.featureName
                    tvNext.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            Log.d(DEBUG_TAG, "geoLocate error: ${e.message}")
            if(e.message.equals("grpc failed")) {
                Toast.makeText(requireContext(), getText(R.string.ERROR_TEXT),
                    Toast.LENGTH_LONG).show()
                btnRetrySetup.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 16
    }
}