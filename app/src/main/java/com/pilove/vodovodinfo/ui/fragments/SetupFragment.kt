package com.pilove.vodovodinfo.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
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
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
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

    private var isHostedByFragment = false

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

        if(requireParentFragment() is SettingsFragment) {
            isHostedByFragment = true
            tvSkip.text = getText(R.string.TRYAGAIN)
            tvNext.text = getText(R.string.SETTINGS_SAVE_BUTTON_TEXT)
            tvNext.visibility = View.GONE
            tvSkip.visibility = View.GONE
            tvDiclamer.text = ""
//            pbMapNoticesSetup.visibility = View.VISIBLE
        }

        if(!isFirstAppTime) {
            val navigationOpt = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_noticesFragment,
                savedInstanceState,
                navigationOpt
            )
        } else {
            mapViewSetup.onCreate(savedInstanceState)

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

            if(PermissionsUtil.hasLocationPermissions(requireContext())){
                isPermissionGranted = true
                mapSetup()
            } else {
                requestPermissions()
            }
        }

        tvNext.setOnClickListener {
            if(it.isVisible && !isHostedByFragment) {
                if(tvNext.text == getText(R.string.YES)) {
                    if(writeToSharedPref()) {
                        nextFrag(savedInstanceState)
                    }
                } else if(tvNext.text == getText(R.string.TRYAGAIN)){
                    //retry again
                    geoLocate(true)
                }
            } else if(it.isVisible){
                writeToSharedPref()

                Toast.makeText(requireContext(),
                        getText(R.string.CHANGES_SAVED), Toast.LENGTH_LONG).show()
            }
        }

        tvSkip.setOnClickListener {
            if(it.isVisible && !isHostedByFragment) {

                nextFrag(savedInstanceState)

            } else if(it.isVisible) {
                //retry again
                geoLocate(true)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setFusedLocationListener() {
        fusedLocationProviderClient!!.lastLocation
            .addOnSuccessListener {
                    location ->
                if (location == null || location.accuracy > 100) {
                   val mLocationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            Log.d(DEBUG_TAG, "receved loc 150")


                            if (locationResult != null && locationResult.locations.isNotEmpty()) {
                                lastKnownLocation = locationResult.locations[0]
                                moveCamera()
                                geoLocate()
                            } else {
                                Log.d(DEBUG_TAG, "ERRRORRR 155")
                                showErrorDialog()
                            }
                        }
                    }

                    val currentLocationRequest = LocationRequest()
                    currentLocationRequest.setInterval(500)
                        .setFastestInterval(0)
                        .setMaxWaitTime(0)
                        .setSmallestDisplacement(0F)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    fusedLocationProviderClient!!.requestLocationUpdates(currentLocationRequest,
                        mLocationCallback, null)
                } else {
                    lastKnownLocation = fusedLocationProviderClient.lastLocation.result
                }
            }
            .addOnFailureListener {
                    showErrorDialog()
            }
    }

    private fun nextFrag(savedInstanceState: Bundle?) {
        val navigationOpt = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in)
            .setExitAnim(R.anim.fade_out)
            .build()

        findNavController().navigate(
            R.id.action_setupFragment_to_noticesFragment,
            savedInstanceState,
            navigationOpt
        )
    }

    private fun mapSetup() = CoroutineScope(Dispatchers.Default).launch {
//        pbMapNoticesSetup.visibility = View.VISIBLE
        delay(2000L)
        withContext(Main) {
            mapViewSetup?.getMapAsync { googleMap ->
                map = googleMap
                getDeviceLocation()
            }
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


    @SuppressLint("Missing permissions")
    private fun getDeviceLocation() {

//        pbMapNoticesSetup.visibility = View.VISIBLE
        try {
            if (isPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        if(task.result == null ||
                            task.result?.accuracy!! > 100 ||
                            (task.result?.latitude == defaultLocation.latitude &&
                                    task.result?.longitude == defaultLocation.longitude)) {
                            Log.d(DEBUG_TAG, "setting up fusedlocalist")

                            setFusedLocationListener()
                        } else {
                            Log.d(
                                DEBUG_TAG,
                                "TASK is success ${task.result?.latitude} " +
                                        "${task.result?.longitude}"
                            )
                            lastKnownLocation = task.result

                            if (lastKnownLocation != null || lastKnownLocation?.latitude != null) {
                                moveCamera()
                                geoLocate()
                            } else {
                                showErrorDialog()
                            }
                        }

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

    @SuppressLint("MissingPermission")
    private fun moveCamera() {
        map?.isMyLocationEnabled = true
        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    lastKnownLocation!!.latitude,
                    lastKnownLocation!!.longitude
                ), DEFAULT_ZOOM.toFloat()
            )
        )
    }

    private fun geoLocate(tryAgain: Boolean = false) = GlobalScope.launch(Main) {
        if(lastKnownLocation == null || lastKnownLocation?.latitude == null) return@launch

        val geocoder = Geocoder(requireContext())
        try {
            val result = geocoder.getFromLocation(lastKnownLocation!!.latitude,
                                                    lastKnownLocation!!.longitude, 1)
            if(tryAgain) {
                //radius search
                val lat = lastKnownLocation!!.latitude
                val long = lastKnownLocation!!.longitude
                var boundaries = listOf<LatLng>(
                    LatLng(lat + 0.001, long),
                    LatLng(lat, long + 0.001),
                    LatLng(lat + 0.001, long + 0.001),
                    LatLng(lat + 0.001, long - 0.001),
                    LatLng(lat - 0.001, long + 0.001),
                    LatLng(lat - 0.001, long - 0.001),
                    LatLng(lat - 0.001, long),
                    LatLng(lat, long - 0.001)
                )

                boundaries.forEach {
                    result += geocoder.getFromLocation(it.latitude, it.longitude, 1)
                }
            }

            if(result.isNotEmpty() && result?.size == 1)
            {
                val address : Address = result[0]
                withContext(Main) {
                    tvAddress.text = address.featureName
                    tvNext.visibility = View.VISIBLE
                    tvSkip.visibility = View.VISIBLE
                }
                Toast.makeText(requireContext(),
                    getText(R.string.TOAST_TRY_AGAIN_TEXT), Toast.LENGTH_LONG).show()
                pbMapNoticesSetup.visibility = View.GONE
            }
            else if(result.isNotEmpty() && result?.size!! > 1){
                result.forEach {
                    Log.d(DEBUG_TAG, it.toString())
                }
                showAlertDialog(result)
            }
        } catch (e: Exception) {
            Log.d(DEBUG_TAG, "geoLocate error: ${e.message}")
            withContext(Main) {
                showErrorDialog()
//                pbMapNoticesSetup.visibility = View.VISIBLE
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

    private fun showAlertDialog(list: MutableList<Address>) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.ALERT_DIALOG_MULTIPLE_PLACES_TITLE)

            val arrayAdapter = ArrayAdapter<String>(requireContext(),
                                                    android.R.layout.select_dialog_singlechoice)

            var result = list.distinctBy { it.featureName }
            //eliminate numbers from list
            result = result.filter { !it.featureName.matches("-?\\d+(\\.\\d+)?".toRegex()) }

            result.forEach {
                arrayAdapter.add(it.featureName)
            }

            setNegativeButton(getText(R.string.CANCEL)) { dialog, _ ->
//                pbMapNoticesSetup.visibility = View.GONE
                dialog.dismiss()
            }

            setPositiveButton(getText(R.string.TRYAGAIN)) { dialog, _ ->
                tvNext.visibility = View.GONE
                geoLocate(true)
                pbMapNoticesSetup.visibility = View.GONE
                dialog.dismiss()
            }

            setAdapter(arrayAdapter) { _, which ->
                tvAddress.text = arrayAdapter.getItem(which)
                tvNext.text = getText(R.string.YES)
            }
            show()
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 17
    }
}