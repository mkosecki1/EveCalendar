package com.sharedcalendar.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.muddzdev.styleabletoast.StyleableToast
import com.sharedcalendar.R
import com.sharedcalendar.adapters.RecyclerViewAdapter
import com.sharedcalendar.database.Statics
import com.sharedcalendar.utility.hideStatusBar
import com.sharedcalendar.utility.setMonthBackground
import com.sharedcalendar.utility.setMonthNavigationBarsColour
import com.sharedcalendar.viewmodel.DayViewModel
import com.sharedcalendar.viewmodel.DayViewModelFactory
import kotlinx.android.synthetic.main.activity_day.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class DayActivity : AppCompatActivity(), KodeinAware {

    override val kodein: Kodein by kodein()
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var valueEventListener: ValueEventListener
    private lateinit var dayViewModel: DayViewModel
    private val databaseReference: FirebaseDatabase by instance()
    private val dayViewModelFactory: DayViewModelFactory by instance()
    private val datePick: String by lazy { intent.getStringExtra(CALENDAR_DATE) }
    private val monthPick: Int by lazy { intent.getIntExtra(CURRENT_MONTH, 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day)
        hideStatusBar()
        initializeDayViewModel()
        eventDetails(datePick)
        runRecyclerViewAdapter()

        day_activity_back_button_id.setOnClickListener {
            runCalendarActivity()
            finish()
        }

        day_activity_add_button_id.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            if (!datePick.isNullOrEmpty()) {
                intent.putExtra(CALENDAR_DATE, datePick)
                intent.putExtra(CURRENT_MONTH, monthPick)
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        runCalendarActivity()
        finish()
    }

    private fun initializeDayViewModel() {
        dayViewModel =
            ViewModelProviders.of(this, dayViewModelFactory).get(DayViewModel::class.java)
    }

    private fun runRecyclerViewAdapter() {
        recyclerViewAdapter = RecyclerViewAdapter()
        day_activity_recycler_view_id.adapter = recyclerViewAdapter
    }

    private fun eventDetails(calendar: String) {
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listOfCalendarDate =
                    dayViewModel.setDataFromFirebase(dataSnapshot)

                val filteredList = listOfCalendarDate.filter { it.date!! == calendar }
                if (filteredList.isNullOrEmpty()) {
//                    startActivity(Intent(applicationContext, CalendarActivity::class.java))
                    finish()
                } else {
                    day_activity_title_id.text = getString(R.string.this_day) + " $datePick"
                    day_activity_background_id.setMonthBackground(monthPick, this@DayActivity)
                    setMonthNavigationBarsColour(window, monthPick, this@DayActivity)
                }

                recyclerViewAdapter.updateTypeList(dayViewModel.setTypeFromFirebase(dataSnapshot))
                recyclerViewAdapter.updateItemList(filteredList)
                recyclerViewAdapter.selectedItem = {
                    removeEvent(it.id.toString())
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
        dayViewModel.addToDatabase(databaseReference.reference, valueEventListener)
    }

    private fun removeEvent(eventId: String) {
        val dbReference = dayViewModel.takeTaskFromDatabase(
            databaseReference.reference,
            Statics.FIREBASE_DATE,
            eventId
        )
        dbReference.removeValue()
        StyleableToast.makeText(
            applicationContext,
            getString(R.string.remove_event_text),
            Toast.LENGTH_LONG,
            R.style.MyToastRemove
        ).show()
    }

    private fun runCalendarActivity() {
        val intent = Intent(this, CalendarActivity::class.java)
        intent.putExtra(CALENDAR_DATE_BACK, datePick)
        startActivity(intent)
    }

    companion object {
        const val CALENDAR_DATE = "value"
        const val CALENDAR_DATE_BACK = "value1"
        const val CURRENT_MONTH = "month"
    }
}