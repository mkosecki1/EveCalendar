package com.sharedcalendar.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.sharedcalendar.database.CalendarType
import com.sharedcalendar.database.Statics
import com.sharedcalendar.repository.Repository

class AddEventViewModel(private val repository: Repository) : ViewModel() {

    fun pushToDatabase(databaseReference: DatabaseReference, databaseName: String) =
        databaseReference.child(databaseName).push()

    fun setTypeFromFirebase(dataSnapshot: DataSnapshot): List<CalendarType> {
        return dataSnapshot.child(Statics.FIREBASE_TYPE).children.mapNotNull {
            repository.setTypeSnapshot(it)
        }
    }

    fun addToDatabase(databaseReference: DatabaseReference, calendarListener: ValueEventListener) =
        databaseReference.addListenerForSingleValueEvent(calendarListener)
}