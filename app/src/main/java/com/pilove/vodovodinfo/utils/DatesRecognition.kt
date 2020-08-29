package com.pilove.vodovodinfo.utils

import android.util.Log
import com.pilove.vodovodinfo.other.Constants.DEBUG_TAG
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

fun recognizeDates(noticeText: String): ArrayList<Date> {

    var result = ArrayList<Date>(0)

    try {

        noticeText.split(" ").toTypedArray().forEach { word ->
            if (word[0].isDigit() && word.length > 9) {
                try {
                    val w = if (word.last() == '.') word.removeSuffix(".") else word
                    val date = SimpleDateFormat("dd.MM.yyyy").parse(w)

                    if (!result.contains(date))
                        result.add(date)

                } catch (e: Exception) {
                }
            }
        }
    } catch (e: Exception) {
        Log.d(DEBUG_TAG, "Greska kod datuma")
    }

    return result
}