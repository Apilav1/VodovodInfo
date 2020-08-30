package com.pilove.vodovodinfo.adapters

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.pilove.vodovodinfo.R
import com.pilove.vodovodinfo.data.Notice
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
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

    private var textSize = 16F

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeViewHolder {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(parent.context)

        notifyAdapterTextSizeChanged()

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
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
            tvNoticeBody.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
        }
    }

    fun notifyAdapterTextSizeChanged() {

        textSize = sharedPreferences.getFloat(KEY_NOTIFICATIONS_TEXT_SIZE, 16F)

        if(textSize == 16F) {
            sharedPreferences.edit()
                .putFloat(KEY_NOTIFICATIONS_TEXT_SIZE, 16F)
                .apply()
        }
    }
}