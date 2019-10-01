package com.sharedcalendar.ui

import `in`.goodiebag.carouselpicker.CarouselPicker
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.database.FirebaseDatabase
import com.muddzdev.styleabletoast.StyleableToast
import com.sharedcalendar.R
import com.sharedcalendar.database.CalendarDate
import com.sharedcalendar.database.EventsEvidence
import com.sharedcalendar.database.Statics
import com.sharedcalendar.utility.hideStatusBar
import com.sharedcalendar.utility.setMonthBackground
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
    private val datePickFromDay: String by lazy { intent.getStringExtra("value") }
    private val monthPick: Int by lazy { intent.getIntExtra("month", 0) }
    private val addEventViewModelFactory: AddEventViewModelFactory by instance()
    private val databaseReference: FirebaseDatabase by instance()
    private var eventsEvidence = EventsEvidence()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_event)
        hideStatusBar()
        enterData(datePickFromDay)
        initializeAddEventViewModel()

        add_activity_cancel_button_id.setOnClickListener {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun initializeAddEventViewModel() {
        addEventViewModel =
            ViewModelProviders.of(this, addEventViewModelFactory).get(AddEventViewModel::class.java)
    }

    private fun enterData(calendar: String) {
        add_activity_background_id.setMonthBackground(monthPick, this)
        imageCarousel = add_activity_carousel_id
        createListOfCarousel(imageCarousel)
        add_activity_text_data_id.text = calendar
        eventsEvidence.date = calendar
        add_activity_time_picker_id.setOnTimeChangedListener { _, hour, minute ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            eventsEvidence.time = SimpleDateFormat("HH:mm").format(cal.time)
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
                when (position) {
                    0 -> eventsEvidence.event = getString(R.string.add_event_text)
                    1 -> eventsEvidence.event = getString(R.string.event_clinic_text)
                    2 -> eventsEvidence.event = getString(R.string.event_visit_text)
                    3 -> eventsEvidence.event = getString(R.string.event_grafting_text)
                    4 -> eventsEvidence.event = getString(R.string.event_other_text)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        add_activity_ok_button_id.setOnClickListener {
            saveDate()
            finish()
        }
    }

    private fun createListOfCarousel(imageCarousel: CarouselPicker) {
        val imageItems = ArrayList<CarouselPicker.PickerItem>()
        imageItems.add(CarouselPicker.DrawableItem(R.drawable.duty))
        imageItems.add(CarouselPicker.DrawableItem(R.drawable.clinic))
        imageItems.add(CarouselPicker.DrawableItem(R.drawable.visit))
        imageItems.add(CarouselPicker.DrawableItem(R.drawable.grafting))
        imageItems.add(CarouselPicker.DrawableItem(R.drawable.other))

        val imageAdapter = CarouselPicker.CarouselViewAdapter(this, imageItems, 0)
        imageAdapter.textColor = getColor(R.color.white)
        imageCarousel.adapter = imageAdapter as PagerAdapter?
    }

    private fun saveDate() {
        val calendarDate = CalendarDate.create()
        calendarDate.date = eventsEvidence.date
        if (eventsEvidence.time.isNullOrEmpty()) {
            calendarDate.time = getString(R.string.time_all_day)
        } else {
            calendarDate.time = eventsEvidence.time
            eventsEvidence.time = ""
        }

        if (eventsEvidence.event.isNullOrEmpty()) {
            calendarDate.event = getString(R.string.event_duty)
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
            R.style.myToastAdd
        ).show()

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
}