package com.sharedcalendar.database

object Statics {
    @JvmStatic var FIREBASE_DATE: String = ""
    @JvmStatic var FIREBASE_TYPE: String = ""

    fun changeDate(dateName: String, typeName: String) {
        FIREBASE_DATE = dateName
        FIREBASE_TYPE = typeName
    }
}