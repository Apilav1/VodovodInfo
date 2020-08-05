package com.pilove.vodovodinfo.model

import java.text.SimpleDateFormat

data class Notice (
    val id: Int,
    val date: SimpleDateFormat,
    val text: String,
    val previousNoticeId: Int = -1
)