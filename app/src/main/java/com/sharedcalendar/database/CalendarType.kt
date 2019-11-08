package com.sharedcalendar.database

import com.sharedcalendar.R

class CalendarType {
    companion object Factory {
        fun create(): CalendarType = CalendarType()
    }

    var id: String? = null
    var type: String? = null

    var eventsTypesList = mutableListOf<String>()
    var eventsTypesImagesList = mutableListOf(
        R.drawable.img_event_red_1,
        R.drawable.img_event_red_2,
        R.drawable.img_event_red_3,
        R.drawable.img_event_red_4,
        R.drawable.img_event_red_5,
        R.drawable.img_event_blue_1,
        R.drawable.img_event_blue_2,
        R.drawable.img_event_blue_3,
        R.drawable.img_event_blue_4,
        R.drawable.img_event_blue_5,
        R.drawable.img_event_green_1,
        R.drawable.img_event_green_2,
        R.drawable.img_event_green_3,
        R.drawable.img_event_green_4,
        R.drawable.img_event_green_5
    )

    fun addToEventsTypesList(eventToAdd: String) {
        eventsTypesList.add(eventToAdd)
    }

    fun getFromEventsTypesImagesList(): MutableList<Int> {
        return eventsTypesImagesList
    }
}