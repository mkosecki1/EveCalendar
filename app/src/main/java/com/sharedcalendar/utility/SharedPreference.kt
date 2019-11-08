package com.sharedcalendar.utility

import android.content.Context
import android.content.SharedPreferences

class SharedPreference(val context: Context) {
    private val PREFS_NAME = "calendarBubble"
    val sharedPref: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveBubbleIsChecked(keyName: String, isChecked: Boolean) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putBoolean(keyName, isChecked)
        editor.apply()
    }

    fun getBubbleIsChecked(keyName: String, isChecked: Boolean) = sharedPref.getBoolean(keyName, isChecked)

    fun saveCarouselFontSize(keyName: String, fontSize: Int) {
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putInt(keyName, fontSize)
        editor.apply()
    }

    fun getCarouselFontSize(keyName: String, fontSize: Int) = sharedPref.getInt(keyName, fontSize)
}