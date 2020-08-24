package com.pilove.vodovodinfo.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.pilove.vodovodinfo.other.Constants.DEFAULT_VALUE_FOR_NOTICE_TITLE
import java.util.Date
import java.text.SimpleDateFormat

@Entity(tableName = "Notices")
data class Notice (
    var date: Date = Date(0L),
    var dateForComparison: Date = Date(0L),
    var title: String = DEFAULT_VALUE_FOR_NOTICE_TITLE,
    var text: String = DEFAULT_VALUE_FOR_NOTICE_TITLE
) {
    @PrimaryKey(autoGenerate = false)
    var id: Int = 2500

    constructor(id: Int,
                title: String,
                date: Date,
                dateForComparison: Date,
                text: String,
                streets: ArrayList<String>,
                dates: ArrayList<Date>) : this(date, dateForComparison, title, text) {
        this.id = id
        this.streets = streets
        this.dates = dates
    }

    @Ignore
    var streets: ArrayList<String> = ArrayList()

    @Ignore
    var dates: ArrayList<Date> = ArrayList()

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