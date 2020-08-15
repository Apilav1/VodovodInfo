package com.pilove.vodovodinfo.data

import java.util.Date

data class Notice (
    val id: Int,
    val title: String,
    val date: Date,
    val text: String,
    val streets: ArrayList<String>,
    val dates: ArrayList<Date>
) {
    override fun toString(): String {
        var s = "Notice: \nid: $id \ntitle:$title\n date: $date \n text: $text \n streets: \n"
        streets.forEach {
            s += "$it, "
        }
        s += "\ndates: \n"
        dates.forEach {
            s += "${it.toString()}, "
        }
        s += "\n"

        return s
    }
}
