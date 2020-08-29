package com.pilove.vodovodinfo.utils

import android.annotation.SuppressLint
import android.util.Log
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("SimpleDateFormat")
fun recognizeDates(noticeText: String): ArrayList<Date> {

    val result = ArrayList<Date>(0)

    try {

        noticeText.split(" ").toTypedArray().forEach { word ->
            if (word[0].isDigit() && word.length > 9) {
                try {
                    val w = if (word.last() == '.') word.removeSuffix(".") else word
                    val date = SimpleDateFormat("dd.MM.yyyy").parse(w)

                    if (!result.contains(date!!))
                        result.add(date)

                } catch (e: Exception) {
                    return result
                }
            }
        }
    } catch (e: Exception) {
        Log.d(DEBUG_TAG, "error to be ignored date: ${e.message}")
        return result
    }

    return result
}