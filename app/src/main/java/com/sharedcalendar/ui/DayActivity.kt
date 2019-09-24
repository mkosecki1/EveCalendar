package com.sharedcalendar.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.muddzdev.styleabletoast.StyleableToast
import com.sharedcalendar.R
import com.sharedcalendar.adapters.RecyclerViewAdapter
import com.sharedcalendar.database.CalendarDate
import com.sharedcalendar.database.Statics
import com.sharedcalendar.utility.hideStatusBar
import com.sharedcalendar.utility.setMonthBackground
import kotlinx.android.synthetic.main.activity_calendar.recycler_view_id
import kotlinx.android.synthetic.main.activity_day.*

class DayActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private lateinit var databaseReference: DatabaseReference
    private var valueEventListener: ValueEventListener? = null
    private val datePick: String by lazy { intent.getStringExtra("value") }
    private val monthPick: Int by lazy { intent.getIntExtra("month", 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day)
        hideStatusBar()
        runFirebase()
        eventDetails(datePick)
        runRecyclerViewAdapter()

        back_button_day_activity_id.setOnClickListener {
            finish()
        }

        button_add_day_activity_id.setOnClickListener {
            val intent = Intent(this, AddEventActivity::class.java)
            if (!datePick.isNullOrEmpty()) {
                intent.putExtra("value", datePick)
                intent.putExtra("month", monthPick)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivityForResult(intent,0)
            finish()
        }
    }

    private fun runFirebase() {
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
    }

    private fun runRecyclerViewAdapter() {
        recyclerViewAdapter = RecyclerViewAdapter()
        recycler_view_id.adapter = recyclerViewAdapter
    }

    private fun eventDetails(calendar: String) {
        val calendarListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val listOfCalendarDate =
                    dataSnapshot.child(Statics.FIREBASE_DATE).children.mapNotNull {
                        it.getValue<CalendarDate>(CalendarDate::class.java)
                    }
                val filteredList = listOfCalendarDate.filter { it.date!! == calendar }
                if (filteredList.isNullOrEmpty()) {
                    day_activity_title_id.text = "Dodaj wydarzenie \nw dniu: ${datePick}"
                } else {
                    day_activity_title_id.text = "Dzień: ${datePick}"
                    day_activity_background_id.setMonthBackground(monthPick, this@DayActivity)

                }

                recyclerViewAdapter.updateItemList(filteredList)
                recyclerViewAdapter.selectedItem = {
                    removeEvent(it.id.toString())
                }
                recyclerViewAdapter.notifyDataSetChanged()
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


    private fun removeEvent(eventId: String) {
        val task = databaseReference.child(Statics.FIREBASE_DATE).child(eventId)
        task.removeValue()
        StyleableToast.makeText(
            applicationContext,
            getString(R.string.remove_event_text),
            Toast.LENGTH_LONG,
            R.style.myToastRemove
        ).show()
        finish()
    }
}
