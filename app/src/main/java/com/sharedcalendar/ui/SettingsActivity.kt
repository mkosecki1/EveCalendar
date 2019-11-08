package com.sharedcalendar.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.muddzdev.styleabletoast.StyleableToast
import com.sharedcalendar.R
import com.sharedcalendar.database.CalendarType
import com.sharedcalendar.database.Statics
import com.sharedcalendar.utility.SharedPreference
import com.sharedcalendar.utility.openDialog
import com.sharedcalendar.utility.hideKeyboard
import com.sharedcalendar.utility.hideStatusBar
import com.sharedcalendar.viewmodel.SettingsViewModel
import com.sharedcalendar.viewmodel.SettingsViewModelFactory
import kotlinx.android.synthetic.main.activity_settings.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class SettingsActivity : AppCompatActivity(), KodeinAware {

    override val kodein: Kodein by kodein()
    private lateinit var sharedPref: SharedPreference
    private lateinit var settingsViewModel: SettingsViewModel
    private val settingsViewModelFactory: SettingsViewModelFactory by instance()
    private val databaseReference: FirebaseDatabase by instance()
    private val firebaseAuth: FirebaseAuth by instance()
    private var eventsTypeList = CalendarType.create()
    private var carouselFontSize = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        hideStatusBar()
        sharedPref = SharedPreference(this)
        initializeSettingsViewModel()

        settings_activity_back_button_id.setOnClickListener {
            finish()
        }

        settings_activity_icon_add_event_id.setOnClickListener {
            addEventsTypes()
            hideKeyboard(this)
        }

        settings_activity_icon_clear_id.setOnClickListener {
            openDialog(
                this,
                getString(R.string.delete_events_type_dialog_info), null,
                ::removeEventsType
            )
        }

        settings_activity_icon_delete_user_app_id.setOnClickListener {
            openDialog(
                this,
                getString(R.string.delete_user_dialog_info), null,
                ::removeUser
            )
        }

        changeCarouselFontSize()
    }

    private fun initializeSettingsViewModel() {
        settingsViewModel =
            ViewModelProviders.of(this, settingsViewModelFactory).get(SettingsViewModel::class.java)
    }

    private fun addEventsTypes() {
        val calendarType = CalendarType.create()
        calendarType.type = settings_activity_add_event_edit_id.text.toString().trim()
        if (!settings_activity_add_event_edit_id.text.isNullOrEmpty()) {
            val newType =
                settingsViewModel.pushDataToDatabase(
                    databaseReference.reference,
                    Statics.FIREBASE_TYPE
                )
            calendarType.id = newType.key
            newType.setValue(calendarType)
            eventsTypeList.addToEventsTypesList(calendarType.type!!)
            settings_activity_add_event_edit_id.text!!.clear()
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.add_events_type),
                Toast.LENGTH_LONG,
                R.style.MyToastAdd
            ).show()
        }
    }

    private fun removeEventsType() {
        val dbReference = settingsViewModel.takeTaskFromDatabase(
            databaseReference.reference,
            Statics.FIREBASE_TYPE
        )
        dbReference.removeValue()
        StyleableToast.makeText(
            applicationContext,
            getString(R.string.remove_event_type),
            Toast.LENGTH_LONG,
            R.style.MyToastRemove
        ).show()
    }

    private fun removeUser() {
        val user = firebaseAuth.currentUser
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))
                    StyleableToast.makeText(
                        applicationContext,
                        getString(R.string.delete_user_text),
                        Toast.LENGTH_LONG,
                        R.style.MyToastAdd
                    ).show()
                }
            }
    }

    private fun changeCarouselFontSize() {
        carouselFontSize = sharedPref.getCarouselFontSize(FONT_SIZE, 8)
        settings_activity_font_info_id.text = getString(R.string.events_font_size)+" $carouselFontSize"
        settings_activity_icon_plus_font_size_id.setOnClickListener {
            carouselFontSize += 1
            sharedPref.saveCarouselFontSize(FONT_SIZE, carouselFontSize)
            settings_activity_font_info_id.text = getString(R.string.events_font_size)+" $carouselFontSize"
        }

        settings_activity_icon_minus_font_size_id.setOnClickListener {
            carouselFontSize -= 1
            sharedPref.saveCarouselFontSize(FONT_SIZE, carouselFontSize)
            settings_activity_font_info_id.text = getString(R.string.events_font_size)+" $carouselFontSize"
        }
    }

    companion object {
        const val FONT_SIZE = "font"
    }
}
