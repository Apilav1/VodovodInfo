package com.pilove.vodovodinfo.utils

import android.content.res.TypedArray

fun isNoticeToBeIgnored(noticeTitle: Array<String>): Boolean {
    return noticeTitle.first() == "Služba"
}