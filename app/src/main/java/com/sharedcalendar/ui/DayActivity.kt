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
    private val datePick: String by lazy { intent.getStringExtra("value") }
    private val monthPick: Int by lazy { intent.getIntExtra("month", 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day)
        hideStatusBar()
        initializeDayViewModel()
        eventDetails(datePick)
        runRecyclerViewAdapter()

        day_activity_back_button_id.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
            finish()
        }

        day_activity_add_button_id.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            if (!datePick.isNullOrEmpty()) {
                intent.putExtra("value", datePick)
                intent.putExtra("month", monthPick)
            }
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, CalendarActivity::class.java))
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
                    finish()
                } else {
                    day_activity_title_id.text = "Dzień: ${datePick}"
                    day_activity_background_id.setMonthBackground(monthPick, this@DayActivity)
                }

                recyclerViewAdapter.updateItemList(filteredList)
                recyclerViewAdapter.selectedItem = {
                    removeEvent(it.id.toString())
                }
//                recyclerViewAdapter.notifyDataSetChanged()
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
            R.style.myToastRemove
        ).show()
//        finish()
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
}