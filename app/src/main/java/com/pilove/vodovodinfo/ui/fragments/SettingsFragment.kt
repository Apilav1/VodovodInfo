package com.pilove.vodovodinfo.ui.fragments

import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RadioButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.pilove.vodovodinfo.R
import kotlinx.android.synthetic.main.fragment_location_setup.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnOptionOneSettingExpand.setOnClickListener {
            if(expandableFirstOptionLayout.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(cvChangeDefLocation, AutoTransition())
                expandableFirstOptionLayout.visibility = View.VISIBLE
                it.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_up_24)

                childFragmentManager.beginTransaction().apply {
                    this.replace(R.id.frameLayout, SetupFragment())
                    commit()
                }
            } else {
                TransitionManager.beginDelayedTransition(cvChangeDefLocation, AutoTransition())
                expandableFirstOptionLayout.visibility = View.GONE
                it.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_down_24)
            }
        }

        btnSettingSecondOptionExpand.setOnClickListener {
            if(expandableSecondOptionLayout.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(cvNotificationSettings, AutoTransition())
                expandableSecondOptionLayout.visibility = View.VISIBLE
                it.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_up_24)
            } else {
                TransitionManager.beginDelayedTransition(cvNotificationSettings, AutoTransition())
                expandableSecondOptionLayout.visibility = View.GONE
                it.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_down_24)
            }
        }

        npSettings.minValue = 16
        npSettings.maxValue = 40

        npSettings.setOnValueChangedListener { picker, oldVal, newVal ->
            btnSave.visibility = View.VISIBLE
            tvExampleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, newVal.toFloat())
        }

        btnSave.setOnClickListener {
            if(it.isVisible) {
            }
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            btnSave.visibility = View.VISIBLE
            when(checkedId) {
                R.id.firstRB -> {

                }
            }
        }

    }

}