package com.sharedcalendar.database

class CalendarDate {
    companion object Factory {
        fun create(): CalendarDate = CalendarDate()
    }

    var id: String? = null
    var date: String? = null
    var timeFrom: String? = null
    var timeTo: String? = null
    var event: String? = null
}