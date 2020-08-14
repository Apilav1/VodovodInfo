package com.pilove.vodovodinfo.data

import java.sql.Date

data class Notice (
    val id: String,
    val date: Date,
    val text: String,
    val idOfPreviousNotice: String
)