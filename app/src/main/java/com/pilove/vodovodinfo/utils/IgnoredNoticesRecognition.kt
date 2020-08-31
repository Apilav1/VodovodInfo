package com.pilove.vodovodinfo.utils


fun isNoticeToBeIgnored(noticeTitle: Array<String>): Boolean {
    return noticeTitle.first() == "Služba"
}