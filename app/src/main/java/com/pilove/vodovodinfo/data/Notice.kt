package com.pilove.vodovodinfo.data

import java.text.SimpleDateFormat
import java.util.Date

data class Notice (
    val id: Int = 2500,
    val title: String = "default",
    val date: Date = Date(),
    var dateForComparison: Date = SimpleDateFormat("dd.MM.yyyy").parse("01.01.1970"),
    val text: String? = "default",
    val streets: ArrayList<String> = ArrayList(),
    val dates: ArrayList<Date> = ArrayList()
) {

    override fun toString(): String {
        var s = "\n----------------------------------------\n" +
                "Notice: \nid: $id \ntitle:$title\n date: $date \n text: $text \n streets: \n"
        streets.forEach {
            s += "$it, "
        }
        s += "\ndates: \n"
        dates.forEach {
            s += "${it.toString()}, "
        }
        s += "\n----------------------------------------\n"

        return s
    }
}
