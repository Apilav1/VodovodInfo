package com.pilove.vodovodinfo.ui.fragments

import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.adapters.NoticeAdapter
import com.pilove.vodovodinfo.ui.viewModels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_notices.*
import kotlinx.android.synthetic.main.item_notice.*

@AndroidEntryPoint
class NoticesFragment : Fragment(R.layout.fragment_notices) {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var noticeAdapter: NoticeAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: fix progress bar
        progress_bar.visibility = View.GONE
        setupRecycleView()

        viewModel.getNotices()

        viewModel.notices.observe(viewLifecycleOwner, Observer {
             noticeAdapter.submitList(it)
             progress_bar.visibility = View.GONE
        })

    }

    private fun setupRecycleView() = rvNotices.apply {
        noticeAdapter = NoticeAdapter()
        adapter = noticeAdapter
        layoutManager = LinearLayoutManager(requireContext())
        this.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
    }
}