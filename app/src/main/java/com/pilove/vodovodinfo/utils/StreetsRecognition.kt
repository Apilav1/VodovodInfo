package com.pilove.vodovodinfo.utils

fun recognizeStreets(noticeText: String): ArrayList<String> {
    var tracking = false
    var streets = ArrayList<String>()
    var street = ""

    for (word in noticeText.split(" ").toTypedArray()) {
        if(tracking) {
            val w = if(word.contains(","))
                word.removeSuffix(",")
            else if(word.contains("."))
                word.removeSuffix(".")
            else word

            if(street.contains("ulic") || street == "na" ||
                    street.contains("došlo")) {
                street = ""
            } else if(w == "i" || w == "u"
                || w == "a"
            ) {

                if(street.isNotEmpty()) streets.add(street)
                street = ""

            } else if((word.contains(",") || word.contains(".")) &&
                            !word.contains("ef")) {
                    street += w
                    streets.add(street)
                    street = ""
            } else {
                street += "$w "
            }
        }
        if(word.contains("ulic")) tracking = true
        if(word.contains("došlo")) tracking = false
        if(word.contains(".") && !word.contains("ef")) tracking = false
    }

    streets = streets.filter { !it.matches("-?\\d+(\\.\\d+)?".toRegex())  } as ArrayList<String>

    return streets
}