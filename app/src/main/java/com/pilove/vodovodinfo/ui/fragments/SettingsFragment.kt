package com.pilove.vodovodinfo.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.other.Constants.NOTIFICATIONS_ALL
import com.pilove.vodovodinfo.other.Constants.KEY_NOTIFICATIONS_MODE
import com.pilove.vodovodinfo.other.Constants.KEY_NOTIFICATIONS_TEXT_SIZE
import com.pilove.vodovodinfo.other.Constants.NOTIFICATIONS_ONLY_MY_STREET
import com.pilove.vodovodinfo.other.Constants.NO_NOTIFICATIONS_MODE
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var notificationMode = 0

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

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            btnSave.visibility = View.VISIBLE
            when(checkedId) {
                R.id.firstRB -> {
                    notificationMode = NOTIFICATIONS_ONLY_MY_STREET
                }
                R.id.secondRB -> {
                    notificationMode = NOTIFICATIONS_ALL
                }
                R.id.thirdRB -> {
                    notificationMode = NO_NOTIFICATIONS_MODE
                }
            }
        }

    }

    private fun writeToSharedPref(): Boolean {

        sharedPreferences.edit()
            .putInt(KEY_NOTIFICATIONS_MODE, notificationMode)
            .putFloat(KEY_NOTIFICATIONS_TEXT_SIZE, tvExampleText.textSize)
            .apply()

        return true
    }

}