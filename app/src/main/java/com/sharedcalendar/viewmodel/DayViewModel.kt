package com.sharedcalendar.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.sharedcalendar.database.CalendarDate
import com.sharedcalendar.database.CalendarType
import com.sharedcalendar.database.Statics
import com.sharedcalendar.repository.Repository

class DayViewModel(private val repository: Repository) : ViewModel() {

    fun setDataFromFirebase(dataSnapshot: DataSnapshot): List<CalendarDate> {
        return dataSnapshot.child(Statics.FIREBASE_DATE).children.mapNotNull {
            repository.setDataSnapshot(it)
        }
    }

    fun setTypeFromFirebase(dataSnapshot: DataSnapshot): List<CalendarType> {
        return dataSnapshot.child(Statics.FIREBASE_TYPE).children.mapNotNull {
            repository.setTypeSnapshot(it)
        }
    }

    fun addToDatabase(databaseReference: DatabaseReference, valueEventListener: ValueEventListener) =
        databaseReference.addValueEventListener(valueEventListener)

    fun takeTaskFromDatabase(databaseReference: DatabaseReference, databaseName: String, eventId: String) =
        databaseReference.child(databaseName).child(eventId)
}