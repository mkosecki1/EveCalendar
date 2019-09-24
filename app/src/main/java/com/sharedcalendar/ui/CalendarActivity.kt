package com.sharedcalendar.ui

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.events.calendar.views.EventsCalendar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sharedcalendar.R
import com.sharedcalendar.database.CalendarDate
import com.sharedcalendar.database.Statics
import com.sharedcalendar.utility.*
import kotlinx.android.synthetic.main.activity_calendar.*
import java.util.*

class CalendarActivity : AppCompatActivity(), EventsCalendar.Callback {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var valueEventListener: ValueEventListener? = null
    private lateinit var loggedUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        hideStatusBar()
        runFirebase()
        checkLoggedUser()

        calendar_settings_id.setOnClickListener {
            logoutUser()
            finish()
        }
    }

    override fun onDayLongPressed(selectedDate: Calendar?) {
        val pickedDate = selectedDate!!.time.convertToday()
        val currentMonth = selectedDate.time.month
        recycler_view_id.visibility = View.VISIBLE
        startVibration()
        openEventDetails(pickedDate, currentMonth)
    }

    override fun onMonthChanged(monthStartDate: Calendar?) {
        recycler_view_id.visibility = View.GONE
        calendar_image_id.setMonthImage(monthStartDate!!.time.month)
        calendar_activity_background_id.setMonthBackground(monthStartDate!!.time.month, this)
    }

    override fun onDaySelected(selectedDate: Calendar?) {}

    public override fun onStart() {
        progressbar_calendar_activity.visibility = View.VISIBLE
        super.onStart()
        if (checkInternetConnection()) {
            val calendarListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val listOfCalendarDate =
                        dataSnapshot.child(Statics.FIREBASE_DATE).children.mapNotNull {
                            it.getValue<CalendarDate>(CalendarDate::class.java)
                        }
                    runCalendar()

                    val cal = Calendar.getInstance()

                    listOfCalendarDate.forEach {
                        cal.set(
                            it.date?.split("-")!![0].toInt(),
                            it.date?.split("-")!![1].toInt() - 1,
                            it.date?.split("-")!![2].toInt()
                        )
                        events_calendar_id.addEvent(cal)
                    }
                    progressbar_calendar_activity.visibility = View.GONE
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        baseContext, getString(R.string.on_cancelled_toast_text),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            databaseReference.addValueEventListener(calendarListener)
            this.valueEventListener = calendarListener
        }
    }

    private fun runCalendar() {
        val today = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 2)
        events_calendar_id.setSelectionMode(events_calendar_id.MULTIPLE_SELECTION)
            .setToday(today)
            .setMonthRange(today, end)
            .setWeekStartDay(Calendar.MONDAY, true)
            .setCurrentSelectedDate(today)
            .setIsBoldTextOnSelectionEnabled(true)
            .setWeekHeaderTypeface(Typeface.DEFAULT_BOLD)
            .setCallback(this)
            .build()

//        calendar_image_id.setMonthImage(today.time.month)
//        calendar_activity_background_id.setSeasonBackground(today.time.month, this)
        calendar_image_id.setMonthImage(today.time.month)
        calendar_activity_background_id.setMonthBackground(today.time.month, this)
        calendar_image_id.visibility = View.VISIBLE
    }

    private fun runFirebase() {
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
    }

    private fun logoutUser() {
        auth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun checkLoggedUser() {
        if (auth.currentUser != null) {
            loggedUser = " ${auth.currentUser?.email}"
        }
    }

    private fun openEventDetails(calendar: String, currentMonth: Int) {
        val calendarListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listOfCalendarDate =
                    dataSnapshot.child(Statics.FIREBASE_DATE).children.mapNotNull {
                        it.getValue<CalendarDate>(CalendarDate::class.java)
                    }
                val filteredList = listOfCalendarDate.filter { it.date!! == calendar }

                if (!filteredList.isNullOrEmpty() && calendar == filteredList[0].date) {
                    val intent = Intent(applicationContext, DayActivity::class.java)
                    intent.putExtra("value", calendar)
                    intent.putExtra("month", currentMonth)
                    startActivity(intent)
                } else {
                    val intent = Intent(applicationContext, AddEventActivity::class.java)
                    intent.putExtra("value", calendar)
                    intent.putExtra("month", currentMonth)
                    startActivity(intent)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    baseContext, getString(R.string.on_cancelled_toast_text),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        databaseReference.addValueEventListener(calendarListener)
        this.valueEventListener = calendarListener
    }

    private fun startVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    300,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }
}