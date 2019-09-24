package com.sharedcalendar.adapters

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.sharedcalendar.R
import com.sharedcalendar.database.CalendarDate
import com.sharedcalendar.utility.convertTimestamp
import kotlinx.android.synthetic.main.event_details.view.*

class RecyclerViewAdapter : RecyclerView.Adapter<ViewHolder>() {

    private var items: MutableList<CalendarDate> = mutableListOf()
    private lateinit var myContext: Context
    var selectedItem: ((CalendarDate) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(viewGroup.context)
        val goldRow = layoutInflater.inflate(R.layout.event_details, viewGroup, false)
        myContext = viewGroup.context
        return ViewHolder(goldRow)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun addEventImage(event: String, imageView: ImageView) {
        when (event) {
            myContext.getString(R.string.event_duty) -> imageView.setImageResource(
                R.drawable.ic_hospital
            )
            myContext.getString(R.string.event_clinic_text) -> imageView.setImageResource(
                R.drawable.ic_clinic
            )
            myContext.getString(R.string.event_visit_text) -> imageView.setImageResource(
                R.drawable.ic_visit
            )
            myContext.getString(R.string.event_grafting_text) -> imageView.setImageResource(
                R.drawable.ic_grafting
            )
            myContext.getString(R.string.event_other_text) -> imageView.setImageResource(
                R.drawable.mountain
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.run {
            items[position].event?.let { addEventImage(it, event_details_image_id) }

            if (items[position].time.isNullOrEmpty()) {
                event_details_time_displayed_id.text = myContext.getString(R.string.time_all_day)

            } else {
                event_details_time_displayed_id.text = items[position].time.toString()
            }
            event_details_event_displayed_id.text = items[position].event.toString()
            event_details_week_day_displayed_id.text =
                DateFormat.format("EEEE", items[position].date!!.convertTimestamp()).toString()
        }

        holder.itemView.event_details_delete_button_id.setOnClickListener {
            selectedItem?.invoke(items[position])
        }
    }

    fun updateItemList(list: List<CalendarDate>) {
        items.clear()
        items.addAll(list)
        items.sortWith(compareBy({ it.date }, { it.time }))
        notifyDataSetChanged()
    }
}

class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view)