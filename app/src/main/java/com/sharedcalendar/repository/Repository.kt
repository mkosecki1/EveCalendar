package com.sharedcalendar.repository

import com.google.firebase.database.DataSnapshot
import com.sharedcalendar.database.CalendarDate
import com.sharedcalendar.database.CalendarType

class Repository {
    fun setDataSnapshot(dataSnapshot: DataSnapshot): CalendarDate? {
        return dataSnapshot.getValue<CalendarDate>(CalendarDate::class.java)
    }

    fun setTypeSnapshot(dataSnapshot: DataSnapshot): CalendarType? {
        return dataSnapshot.getValue<CalendarType>(CalendarType::class.java)
    }
}