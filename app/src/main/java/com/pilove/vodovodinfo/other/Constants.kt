package com.pilove.vodovodinfo.other

object Constants {

    const val NOTICE_DATABASE_NAME = "notice_db"

    const val DEBUG_TAG = "MAINACT"

    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    const val NOTIFICATION_CHANNEL_ID = "notification_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Notifying"
    const val NOTIFICATION_ID = 1

    const val DEFAULT_VALUE_FOR_NOTICE_TITLE = "Default"

    const val SHARED_PREFERENCES_NAME = "sharedPref"
    //for when the app is first time open to show setup fragment
    const val KEY_IS_FIRST_TIME = "KEY_IS_FIRST_TIME"
    const val KEY_DEFAULT_LOCATION_STREET_NAME = "KEY_DEFAULT_LOCATION_STREET_NAME"
    const val KEY_DEFAULT_LOCATION_LAT = "KEY_DEFAULT_LOCATION_LAT"
    const val KEY_DEFAULT_LOCATION_LNG = "KEY_DEFAULT_LOCATION_LNG"

    const val KEY_NOTIFICATIONS_MODE = "NOTIFICATIONS_MODE"
    const val NOTIFICATIONS_ONLY_MY_STREET = 1
    const val NOTIFICATIONS_ALL = 2
    const val NO_NOTIFICATIONS_MODE = 3

    const val KEY_NOTIFICATIONS_TEXT_SIZE = "KEY_NOTIFICATIONS_TEXT_SIZE"

    const val KEY_LATEST_NOTICE_ID = "KEY_LATEST_NOTICE_ID"
}