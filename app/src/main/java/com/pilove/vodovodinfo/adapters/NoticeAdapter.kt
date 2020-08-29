package com.pilove.vodovodinfo.adapters

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.data.Notice
import com.pilove.vodovodinfo.other.Constants.KEY_NOTIFICATIONS_TEXT_SIZE
import com.pilove.vodovodinfo.other.Constants.SHARED_PREFERENCES_NAME
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.item_notice.view.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class NoticeAdapter : RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {

        sharedPreferences = parent.context
                                .getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

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

            val textSize = sharedPreferences.getFloat(KEY_NOTIFICATIONS_TEXT_SIZE, 16F)
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
            tvNoticeBody.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
        }
    }
}