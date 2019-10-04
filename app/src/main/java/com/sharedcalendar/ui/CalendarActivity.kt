package com.sharedcalendar.ui

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.events.calendar.utils.EventsCalendarUtil.today
import com.events.calendar.views.EventsCalendar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sharedcalendar.R
import com.sharedcalendar.database.CalendarDate
import com.sharedcalendar.utility.*
import com.sharedcalendar.viewmodel.CalendarViewModel
import com.sharedcalendar.viewmodel.CalendarViewModelFactory
import kotlinx.android.synthetic.main.activity_calendar.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*

class CalendarActivity : AppCompatActivity(), EventsCalendar.Callback, KodeinAware {

    override val kodein: Kodein by kodein()
    private lateinit var calendarListener: ValueEventListener
    private lateinit var calendarViewModel: CalendarViewModel
    private val databaseReference: FirebaseDatabase by instance()
    private val calendarViewModelFactory: CalendarViewModelFactory by instance()
    private val firebaseAuth: FirebaseAuth by instance()
    private var listOfCalendarDate: List<CalendarDate> = mutableListOf()
    private var isSettingsClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        initializeCalendarViewModel()
        hideStatusBar()

        calendar_activity_logout_id.setOnClickListener {
            logoutUser()
            finish()
        }

        calendar_activity_exit_id.setOnClickListener {
            finishAffinity()
        }

        calendar_activity_settings_id.setOnClickListener {
            openSettings()
        }
    }

    override fun onBackPressed() {
        //do nothing
    }

    override fun onDayLongPressed(selectedDate: Calendar?) {
        val pickedDate = selectedDate!!.time.convertToday()
        val currentMonth = selectedDate.time.month
        startVibration(100)
        openEventDetails(pickedDate, currentMonth)
    }

    override fun onMonthChanged(monthStartDate: Calendar?) {
        calendar_activity_image_id.setMonthImage(monthStartDate!!.time.month)
        calendar_activity_background_id.setMonthBackground(monthStartDate.time.month, this)
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        val selectedMonth = selectedDate!!.time.month
        changeCalendarBackground(selectedMonth)
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun initializeCalendarViewModel() {
        calendarViewModel =
            ViewModelProviders.of(this, calendarViewModelFactory).get(CalendarViewModel::class.java)
    }

    public override fun onStart() {
        calendar_activity_progressbar.visibility = View.VISIBLE
        super.onStart()
        runCalendar()
        if (this.checkInternetConnection()) {
            loadEventsDate()
            addToDatabaseReference()
        }
        calendar_activity_progressbar.visibility = View.GONE
    }

    private fun loadEventsDate(): ValueEventListener {
        calendarListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listOfCalendarDate = calendarViewModel.setDataFromFirebase(dataSnapshot)
                runCalendar()
                calendarViewModel.addEventDotsOnCalendar(events_calendar_id, listOfCalendarDate)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        return calendarListener
    }

    private fun openEventDetails(calendar: String, currentMonth: Int) {
        calendarListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listOfCalendarDate = calendarViewModel.setDataFromFirebase(dataSnapshot)
                val filteredList = listOfCalendarDate.filter { it.date!! == calendar }

                if (!filteredList.isNullOrEmpty() && calendar == filteredList[0].date) {
                    val intent = Intent(applicationContext, DayActivity::class.java)
                    intent.putExtra("value", calendar)
                    intent.putExtra("month", currentMonth)
                    startActivity(intent)
                    overridePendingTransition(R.anim.appear, R.anim.no_animation)
                    finish()
                } else {
                    val intent = Intent(applicationContext, AddEventActivity::class.java)
                    intent.putExtra("value", calendar)
                    intent.putExtra("month", currentMonth)
                    startActivity(intent)
                    overridePendingTransition(R.anim.appear, R.anim.no_animation)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    baseContext, getString(R.string.on_cancelled_toast_text),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        addToDatabaseReference()
    }

    private fun addToDatabaseReference() {
        calendarViewModel.addToDatabase(databaseReference.reference, calendarListener)
    }

    private fun runCalendar() {
        val today = Calendar.getInstance()
        val minMonth = Calendar.getInstance()
        minMonth.add(Calendar.MONTH, -12)
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 2)
        events_calendar_id.setSelectionMode(events_calendar_id.SINGLE_SELECTION)
            .setToday(today)
            .setMonthRange(minMonth, end)
            .setWeekStartDay(Calendar.MONDAY, true)
            .setCurrentSelectedDate(today)
            .setIsBoldTextOnSelectionEnabled(true)
            .setWeekHeaderTypeface(Typeface.DEFAULT_BOLD)
            .setCallback(this)
            .build()

        changeCalendarBackground(events_calendar_id.getCurrentSelectedDate()!!.time.month)
        calendar_activity_image_id.visibility = View.VISIBLE
    }

    private fun changeCalendarBackground(selectedMonth: Int) {
        val currentMonth = today.time.month
        if (selectedMonth == currentMonth) {
            calendar_activity_image_id.setMonthImage(currentMonth)
            calendar_activity_background_id.setMonthBackground(currentMonth, this)
        } else {
            calendar_activity_image_id.setMonthImage(selectedMonth)
            calendar_activity_background_id.setMonthBackground(selectedMonth, this)
        }
    }

    private fun openSettings() {
        val animZoomIn = AnimationUtils.loadAnimation(applicationContext, R.anim.zoom_in)
        val animZoomOut = AnimationUtils.loadAnimation(applicationContext, R.anim.zoom_out)
        when {
            isSettingsClicked -> {
                calendar_activity_logout_id.toggleVisibility()
                calendar_activity_exit_id.toggleVisibility()
                calendar_activity_logout_id.startAnimation(animZoomOut)
                calendar_activity_exit_id.startAnimation(animZoomOut)
                isSettingsClicked = false
            }
            else -> {
                calendar_activity_logout_id.toggleVisibility()
                calendar_activity_exit_id.toggleVisibility()
                calendar_activity_logout_id.startAnimation(animZoomIn)
                calendar_activity_exit_id.startAnimation(animZoomIn)
                isSettingsClicked = true
            }
        }
    }
}