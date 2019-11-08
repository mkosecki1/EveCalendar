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
import kotlinx.android.synthetic.main.bubble_dialog_helper.*
import kotlinx.android.synthetic.main.options_menu.*
import kotlinx.android.synthetic.main.options_menu_background.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*

class CalendarActivity : AppCompatActivity(), EventsCalendar.Callback, KodeinAware {

    override val kodein: Kodein by kodein()
    private lateinit var calendarListener: ValueEventListener
    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var sharedPref: SharedPreference
    private lateinit var checkTodayDate: Date
    private val databaseReference: FirebaseDatabase by instance()
    private val calendarViewModelFactory: CalendarViewModelFactory by instance()
    private val firebaseAuth: FirebaseAuth by instance()
    private val datePickFromDay: String by lazy { intent.getStringExtra(CALENDAR_DATE_BACK) }
    private var listOfCalendarDate: List<CalendarDate> = mutableListOf()
    private var isSettingsClicked = false
    private var isBubbleClicked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        hideStatusBar()
        sharedPref = SharedPreference(this)
        hideBubbleDialog()
        initializeCalendarViewModel()
        getCurrentUser()

        calendar_activity_options_id.setOnClickListener {
            hideKeyboard(this)
            openSettingsMenu()
        }

        options_text_logout_id.setOnClickListener {
            logoutUser()
            finish()
        }

        options_text_exit_id.setOnClickListener {
            finishAffinity()
        }

        options_text_help_app_id.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
            openSettingsMenu()
        }

        bubble_dialog_helper_check_box_id.setOnClickListener {
            checkBubbleDialog()
        }

        options_text_settings_activity_id.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            openSettingsMenu()
        }

        options_menu_background_id.setOnClickListener { }
    }

    override fun onStart() {
        super.onStart()
        calendar_activity_progressbar_id.visibility = View.VISIBLE
        runCalendar()
        if (this.checkInternetConnection()) {
            loadEventsDate()
            addToDatabaseReference()
        }
        calendar_activity_progressbar_id.visibility = View.GONE
    }

    override fun onBackPressed() {
        val animToLeft = AnimationUtils.loadAnimation(applicationContext, R.anim.to_left)
        val animRotateAntiClockwise =
            AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_anti_clockwise)
        if (isSettingsClicked) {
            calendar_activity_options_layout_background_id.toggleVisibility()
            calendar_activity_options_layout_id.toggleVisibility()
            calendar_activity_options_layout_id.startAnimation(animToLeft)
            calendar_activity_options_id.startAnimation(animRotateAntiClockwise)
            isSettingsClicked = false
        }
    }

    override fun onDayLongPressed(selectedDate: Calendar?) {
        val pickedDate = selectedDate!!.time.convertToday()
        val currentMonth = selectedDate.time.month
        startVibration(VIBRATION_DURATION)
        openEventDetails(pickedDate, currentMonth)
    }

    override fun onMonthChanged(monthStartDate: Calendar?) {
        calendar_activity_image_id.setMonthImage(monthStartDate!!.time.month)
        calendar_activity_background_id.setMonthBackground(monthStartDate.time.month, this)
        setMonthNavigationBarsColour(window, monthStartDate.time.month, this)
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        val selectedMonth = selectedDate!!.time.month
        changeCalendarBackground(selectedMonth)
    }

    private fun hideBubbleDialog() {
        isBubbleClicked = sharedPref.getBubbleIsChecked(IS_BUBBLE_CHECKED_KEY, false)
        if (isBubbleClicked) {
            calendar_activity_bubble_dialog_id.visibility = View.GONE
        } else {
            animationBubble()
        }
    }

    private fun checkBubbleDialog() {
        if (bubble_dialog_helper_check_box_id.isChecked) {
            isBubbleClicked = true
            sharedPref.saveBubbleIsChecked(IS_BUBBLE_CHECKED_KEY, isBubbleClicked)
            val noAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.no_animation)
            calendar_activity_bubble_dialog_id.startAnimation(noAnim)
            calendar_activity_bubble_dialog_id.visibility = View.GONE
        }
    }

    private fun animationBubble() {
        val animZoomIn = AnimationUtils.loadAnimation(applicationContext, R.anim.zoom_in)
        val animZoomOut = AnimationUtils.loadAnimation(applicationContext, R.anim.zoom_out)
        calendar_activity_bubble_dialog_id.startAnimation(animZoomIn)
        calendar_activity_bubble_dialog_id.startAnimation(animZoomOut)

    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun initializeCalendarViewModel() {
        calendarViewModel =
            ViewModelProviders.of(this, calendarViewModelFactory).get(CalendarViewModel::class.java)
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
                    intent.putExtra(CALENDAR_DATE, calendar)
                    intent.putExtra(CURRENT_MONTH, currentMonth)
                    startActivity(intent)
                    overridePendingTransition(R.anim.appear, R.anim.no_animation)
//                    finish()
                } else {
                    val intent = Intent(applicationContext, AddEventActivity::class.java)
                    intent.putExtra(CALENDAR_DATE, calendar)
                    intent.putExtra(CURRENT_MONTH, currentMonth)
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
        if (!datePickFromDay.isNullOrEmpty()) {
            checkTodayDate = datePickFromDay.convertTimestamp()
            today.time = checkTodayDate
        }
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
        events_calendar_id.setToday(Calendar.getInstance())
    }

    private fun changeCalendarBackground(selectedMonth: Int) {
        val currentMonth = today.time.month
        if (selectedMonth == currentMonth) {
            calendar_activity_image_id.setMonthImage(currentMonth)
            calendar_activity_background_id.setMonthBackground(currentMonth, this)
            setMonthNavigationBarsColour(window, currentMonth, this)
        } else {
            calendar_activity_image_id.setMonthImage(selectedMonth)
            calendar_activity_background_id.setMonthBackground(selectedMonth, this)
            setMonthNavigationBarsColour(window, selectedMonth, this)
        }
    }

    private fun getCurrentUser() {
        settings_activity_text_clear_id.text = firebaseAuth.currentUser!!.email
    }

    private fun openSettingsMenu() {
        val animFromLeft = AnimationUtils.loadAnimation(applicationContext, R.anim.from_left)
        val animToLeft = AnimationUtils.loadAnimation(applicationContext, R.anim.to_left)
        val animRotateClockwise =
            AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_clockwise)
        val animRotateAntiClockwise =
            AnimationUtils.loadAnimation(applicationContext, R.anim.rotate_anti_clockwise)
        isSettingsClicked = when {
            isSettingsClicked -> {
                calendar_activity_options_layout_background_id.toggleVisibility()
                calendar_activity_options_layout_id.toggleVisibility()
                calendar_activity_options_layout_id.startAnimation(animToLeft)
                calendar_activity_options_id.startAnimation(animRotateAntiClockwise)
                false
            }
            else -> {
                calendar_activity_options_layout_background_id.toggleVisibility()
                calendar_activity_options_layout_id.toggleVisibility()
                calendar_activity_options_layout_id.startAnimation(animFromLeft)
                calendar_activity_options_id.startAnimation(animRotateClockwise)
                events_calendar_id.visibility = View.GONE
                events_calendar_id.visibility = View.VISIBLE
                true
            }
        }
    }

    companion object {
        const val IS_BUBBLE_CHECKED_KEY = "bubbleCalendar"
        const val CALENDAR_DATE = "value"
        const val CALENDAR_DATE_BACK = "value1"
        const val CURRENT_MONTH = "month"
        const val VIBRATION_DURATION = 90L
    }
}