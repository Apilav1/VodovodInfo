package com.pilove.vodovodinfo.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.other.Constants.KEY_FROM_SETTING
import com.pilove.vodovodinfo.other.Constants.KEY_DEFAULT_LOCATION_STREET_NAME
import com.pilove.vodovodinfo.other.Constants.NOTIFICATIONS_ALL
import com.pilove.vodovodinfo.other.Constants.KEY_NOTIFICATIONS_MODE
import com.pilove.vodovodinfo.other.Constants.KEY_NOTIFICATIONS_TEXT_SIZE
import com.pilove.vodovodinfo.other.Constants.NOTIFICATIONS_ONLY_MY_STREET
import com.pilove.vodovodinfo.other.Constants.NO_NOTIFICATIONS_MODE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var notificationMode = NOTIFICATIONS_ALL

    private var isLocationSetUp = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnOptionOneSettingExpand.setOnClickListener {
//            if(expandableFirstOptionLayout.visibility == View.GONE) {
//                TransitionManager.beginDelayedTransition(cvChangeDefLocation, AutoTransition())
//                expandableFirstOptionLayout.visibility = View.VISIBLE
//                it.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_up_24)
//
//                childFragmentManager.beginTransaction().apply {
//                    this.replace(R.id.frameLayout, SetupFragment())
//                    commit()
//                }
//            } else {
//                TransitionManager.beginDelayedTransition(cvChangeDefLocation, AutoTransition())
//                expandableFirstOptionLayout.visibility = View.GONE
//                it.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_down_24)
//            }

            sharedPreferences.edit()
                .putBoolean(KEY_FROM_SETTING, true)
                .apply()

            val navigationOpt = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in)
                .setExitAnim(R.anim.fade_out)
                .build()


            findNavController().navigate(
                R.id.action_settingsFragment_to_setupFragment,
                savedInstanceState,
                navigationOpt
            )
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

        setUpNumberPicker()

        btnSave.setOnClickListener {
            if(it.isVisible) {
                if(writeToSharedPref()) {
                    val navigationOpt = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in)
                        .setExitAnim(R.anim.fade_out)
                        .build()

                    findNavController().navigate(
                        R.id.action_settingsFragment_to_noticesFragment,
                        savedInstanceState,
                        navigationOpt
                    )
                }
            }
        }

        when(sharedPreferences.getInt(KEY_NOTIFICATIONS_MODE, 2)) {
            1 -> radioGroup.check(R.id.streetNotificationsRB)
            2 -> radioGroup.check(R.id.allNotificationsRB)
            3 -> radioGroup.check(R.id.noNotificationsRB)
        }

        radioGroup.setOnCheckedChangeListener { _ , checkedId ->
            btnSave.visibility = View.VISIBLE
            when(checkedId) {
                R.id.streetNotificationsRB -> {
                    isLocationSetUp = sharedPreferences
                            .getString(KEY_DEFAULT_LOCATION_STREET_NAME, "") != ""

                    if(isLocationSetUp)
                     notificationMode = NOTIFICATIONS_ONLY_MY_STREET
                    else {
                           Toast.makeText(requireContext(),
                              getText(R.string.NO_LOCATION_IS_SET_UP), Toast.LENGTH_LONG).show()
                           radioGroup.check(R.id.allNotificationsRB)
                        }
                }
                R.id.allNotificationsRB -> {
                    notificationMode = NOTIFICATIONS_ALL
                }
                R.id.noNotificationsRB -> {
                    notificationMode = NO_NOTIFICATIONS_MODE
                }
            }
        }

    }

    private fun setUpNumberPicker() {
        npSettings.value = sharedPreferences
            .getFloat(KEY_NOTIFICATIONS_TEXT_SIZE, 16F).toInt()
        npSettings.minValue = 16
        npSettings.maxValue = 40

        npSettings.setOnValueChangedListener { _, _, newVal ->
            btnSave.visibility = View.VISIBLE
            tvExampleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, newVal.toFloat())
        }
    }

    private fun writeToSharedPref(): Boolean {

        sharedPreferences.edit()
            .putInt(KEY_NOTIFICATIONS_MODE, notificationMode)
            .putFloat(KEY_NOTIFICATIONS_TEXT_SIZE, npSettings.value.toFloat())
            .apply()

        return true
    }

}