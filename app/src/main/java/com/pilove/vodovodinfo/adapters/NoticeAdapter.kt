package com.pilove.vodovodinfo.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.data.Notice
import kotlinx.android.synthetic.main.item_notice.view.*
import kotlin.collections.ArrayList

class NoticeAdapter : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {
        return NoticeViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_notice,
                parent,
                false
            )
        )
    }

    var items = ArrayList<Notice>()

    fun submitList(list: ArrayList<Notice>) {
        items = list
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: NoticeViewHolder, position: Int) {
        val notice = items[position]

        holder.itemView.apply {
            tvTitle.text = notice.title
            tvNoticeBody.text = notice.text
        }
    }
}