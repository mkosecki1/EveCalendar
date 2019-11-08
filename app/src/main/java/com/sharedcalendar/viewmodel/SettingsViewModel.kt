package com.sharedcalendar.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.sharedcalendar.repository.Repository

class SettingsViewModel(private val repository: Repository) : ViewModel() {

    fun pushDataToDatabase(databaseReference: DatabaseReference, databaseName: String) =
        databaseReference.child(databaseName).push()

    fun takeTaskFromDatabase(databaseReference: DatabaseReference, databaseName: String) =
        databaseReference.child(databaseName)
}