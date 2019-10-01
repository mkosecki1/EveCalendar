package com.sharedcalendar.viewmodel

import androidx.lifecycle.ViewModel
import com.events.calendar.views.EventsCalendar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.sharedcalendar.database.CalendarDate
import com.sharedcalendar.database.Statics
import com.sharedcalendar.repository.Repository
import java.util.*

class CalendarViewModel(private val repository: Repository) : ViewModel() {

    fun setDataFromFirebase(dataSnapshot: DataSnapshot): List<CalendarDate> {
        return dataSnapshot.child(Statics.FIREBASE_DATE).children.mapNotNull {
            repository.setDataSnapshot(it)
        }
    }

    fun addEventDotsOnCalendar(
        eventsCalendar: EventsCalendar,
        listOfCalendarDate: List<CalendarDate>
    ) {
        val cal = Calendar.getInstance()
        listOfCalendarDate.forEach {
            cal.set(
                it.date?.split("-")!![0].toInt(),
                it.date?.split("-")!![1].toInt() - 1,
                it.date?.split("-")!![2].toInt()
            )
            eventsCalendar.addEvent(cal)
        }
    }

    fun addToDatabase(databaseReference: DatabaseReference, calendarListener: ValueEventListener) =
        databaseReference.addValueEventListener(calendarListener)
}