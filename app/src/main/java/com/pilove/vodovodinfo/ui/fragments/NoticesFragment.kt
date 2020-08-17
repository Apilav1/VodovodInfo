package com.pilove.vodovodinfo.ui.fragments

import android.annotation.SuppressLint
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.adapters.NoticeAdapter
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import com.pilove.vodovodinfo.ui.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notices.*
import kotlinx.android.synthetic.main.item_notice.*

@AndroidEntryPoint
class NoticesFragment : Fragment(R.layout.fragment_notices) {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var noticeAdapter: NoticeAdapter

    private var map: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView.onCreate(savedInstanceState)

        //TODO: fix progress bar
        progress_bar.visibility = View.GONE
        setupRecycleView()

        viewModel.getNotices()

        viewModel.notices.observe(viewLifecycleOwner, Observer {
             noticeAdapter.submitList(it)
             progress_bar.visibility = View.GONE
        })

        mapView.getMapAsync {
            map = it
        }

        btnResizeMapDown.setOnClickListener {
            toggleMap()
        }
        btnResizeMapUp.setOnClickListener {
            toggleMap()
        }
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
}