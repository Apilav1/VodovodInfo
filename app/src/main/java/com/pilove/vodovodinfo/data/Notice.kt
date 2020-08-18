package com.pilove.vodovodinfo.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date
import java.text.SimpleDateFormat

@Entity(tableName = "Notices")
data class Notice (
    var date: Date = Date(0L),
    var title: String = "Default",
    var text: String = "Default"
) {
    @PrimaryKey(autoGenerate = false)
    var id: Int = 2500

    constructor(id: Int,
                title: String,
                date: Date,
                dateForComparison: Date,
                text: String,
                streets: ArrayList<String>,
                dates: ArrayList<Date>) : this(date, title, text) {
        this.id = id
        this.dateForComparison = dateForComparison
        this.streets = streets
        this.dates = dates
    }

    @Ignore
    var dateForComparison = Date(0L)

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