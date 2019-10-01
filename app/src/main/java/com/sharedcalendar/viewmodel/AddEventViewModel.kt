package com.sharedcalendar.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference

class AddEventViewModel : ViewModel() {

    fun pushToDatabase(databaseReference: DatabaseReference, databaseName: String) =
        databaseReference.child(databaseName).push()
}