package com.sharedcalendar.ui

import `in`.goodiebag.carouselpicker.CarouselPicker
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muddzdev.styleabletoast.StyleableToast
import com.sharedcalendar.R
import com.sharedcalendar.database.CalendarDate
import com.sharedcalendar.database.CalendarType
import com.sharedcalendar.database.EventsEvidence
import com.sharedcalendar.database.Statics
import com.sharedcalendar.utility.*
import com.sharedcalendar.viewmodel.AddEventViewModel
import com.sharedcalendar.viewmodel.AddEventViewModelFactory
import kotlinx.android.synthetic.main.activity_add_event.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.text.SimpleDateFormat
import java.util.*

class AddEventActivity : AppCompatActivity(), KodeinAware {
    override val kodein: Kodein by kodein()
    private lateinit var addEventViewModel: AddEventViewModel
    private lateinit var imageCarousel: CarouselPicker
    private lateinit var sharedPref: SharedPreference
    private val datePickFromDay: String by lazy { intent.getStringExtra(CALENDAR_DATE) }
    private val monthPick: Int by lazy { intent.getIntExtra(CURRENT_MONTH, 0) }
    private val addEventViewModelFactory: AddEventViewModelFactory by instance()
    private val databaseReference: FirebaseDatabase by instance()
    private var eventsEvidence = EventsEvidence()
    private lateinit var valueEventListener: ValueEventListener
    private var listOfCalendarType: List<CalendarType> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)
        hideStatusBar()
        sharedPref = SharedPreference(this)
        initializeAddEventViewModel()
        enterData(datePickFromDay)

        add_activity_cancel_button_id.setOnClickListener {
            runCalendarActivity()
            finish()
        }

        add_activity_ok_button_id.setOnClickListener {
            isEventsTypesAdded()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        runCalendarActivity()
        finish()
    }

    private fun initializeAddEventViewModel() {
        addEventViewModel =
            ViewModelProviders.of(this, addEventViewModelFactory).get(AddEventViewModel::class.java)
    }

    private fun enterData(calendar: String) {
        add_activity_background_id.setMonthBackground(monthPick, this)
        setMonthNavigationBarsColour(window, monthPick, this)
        add_activity_time_text_from_id.setMonthTimeText(monthPick, this)
        add_activity_time_text_to_id.setMonthTimeText(monthPick, this)
        add_activity_carousel_text_id.setMonthTimeText(monthPick, this)
        imageCarousel = add_activity_carousel_id
        createListOfCarousel(imageCarousel)
//        add_activity_text_data_id.text = calendar
        add_activity_carousel_text_id.text = getString(R.string.enter_dialog_title) + "  " + calendar
        eventsEvidence.date = calendar
        add_activity_time_picker_from_id.setOnTimeChangedListener { _, hour, minute ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            eventsEvidence.timeFrom = SimpleDateFormat(TIME_FORMAT).format(cal.time)
        }

        add_activity_time_picker_to_id.setOnTimeChangedListener { _, hour, minute ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            eventsEvidence.timeTo = SimpleDateFormat(TIME_FORMAT).format(cal.time)
        }

        add_activity_carousel_id.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                eventsEvidence.event = listOfCalendarType[position].type.toString()

            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun createListOfCarousel(imageCarousel: CarouselPicker) {
        val fontSize = sharedPref.getCarouselFontSize(FONT_SIZE, CAROUSEL_FONT_SIZE)
        var textItems = mutableListOf<CarouselPicker.PickerItem>()
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listOfCalendarType = addEventViewModel.setTypeFromFirebase(dataSnapshot)
                listOfCalendarType.forEach { it ->
                    textItems.add(CarouselPicker.TextItem(it.type, fontSize))
                }

                val textAdapter =
                    CarouselPicker.CarouselViewAdapter(applicationContext, textItems, 0)
                textAdapter.textColor = getColor(R.color.white)
                imageCarousel.adapter = textAdapter as PagerAdapter?

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
        addEventViewModel.addToDatabase(databaseReference.reference, valueEventListener)
    }

    private fun isEventsTypesAdded() {
        if (!listOfCalendarType.isNullOrEmpty()) {
            saveDate()


            val intent = Intent(this, DayActivity::class.java)
            intent.putExtra(CALENDAR_DATE, datePickFromDay)
            intent.putExtra(CURRENT_MONTH, monthPick)
            startActivity(intent)


            finish()
        } else {
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.add_event_toast_info),
                Toast.LENGTH_LONG,
                R.style.MyToastAdd
            ).show()
        }
    }

    private fun saveDate() {
        val calendarDate = CalendarDate.create()
        calendarDate.date = eventsEvidence.date
        if (eventsEvidence.timeFrom.isNullOrEmpty()) {
            calendarDate.timeFrom = getString(R.string.time_all_day)
            calendarDate.timeTo = ""
        } else {
            calendarDate.timeFrom = eventsEvidence.timeFrom
            calendarDate.timeTo = eventsEvidence.timeTo
            eventsEvidence.timeFrom = ""
        }

        if (eventsEvidence.event.isNullOrEmpty()) {
            calendarDate.event = listOfCalendarType[0].type.toString()
        } else {
            calendarDate.event = eventsEvidence.event
            eventsEvidence.event = ""
        }

        val newDate =
            addEventViewModel.pushToDatabase(databaseReference.reference, Statics.FIREBASE_DATE)
        calendarDate.id = newDate.key
        newDate.setValue(calendarDate)
        StyleableToast.makeText(
            applicationContext,
            getString(R.string.add_event_text),
            Toast.LENGTH_LONG,
            R.style.MyToastAdd
        ).show()

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }

    private fun runCalendarActivity() {
        val intent = Intent(this, CalendarActivity::class.java)
        intent.putExtra(CALENDAR_DATE_BACK, datePickFromDay)
        startActivity(intent)
    }

    companion object {
        const val CALENDAR_DATE = "value"
        const val CALENDAR_DATE_BACK = "value1"
        const val CURRENT_MONTH = "month"
        const val TIME_FORMAT = "HH:mm"
        const val CAROUSEL_FONT_SIZE = 8
        const val FONT_SIZE = "font"
    }
}