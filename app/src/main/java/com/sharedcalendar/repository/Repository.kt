package com.sharedcalendar.repository

import com.google.firebase.database.DataSnapshot
import com.sharedcalendar.database.CalendarDate

class Repository {
    fun setDataSnapshot(dataSnapshot: DataSnapshot): CalendarDate? {
        return dataSnapshot.getValue<CalendarDate>(CalendarDate::class.java)
    }
}