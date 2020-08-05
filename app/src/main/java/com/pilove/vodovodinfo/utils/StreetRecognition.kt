package com.pilove.vodovodinfo.utils

import android.util.Log

fun recognizeStreets(noticeText: String): ArrayList<String> {
    var tracking = false
    var streets = ArrayList<String>()
    var street = ""

    for (word in noticeText.split(" ").toTypedArray()) {
        if(tracking) {
            var w = if(word.contains(","))
                word.removeSuffix(",")
            else if(word.contains("."))
                word.removeSuffix(".")
            else word

            if(street.contains("ulic") || street.equals("na")) {
                street = ""
            } else if(w.equals("i") || w.equals("u")
                || w.equals("a")) {

                if(street.isNotEmpty()) streets.add(street)
                street = ""

            } else if(word.contains(",") || word.contains(".")) {
                street += w
                streets.add(street)
                street = ""
            } else {
                street += w + " "
            }
        }
        if(word.contains("ulic")) tracking = true
        if(word.contains(".")) tracking = false
    }

    return streets
}