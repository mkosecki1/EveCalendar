package com.sharedcalendar.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sharedcalendar.R
import com.sharedcalendar.utility.hideStatusBar
import kotlinx.android.synthetic.main.activity_help.*

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        hideStatusBar()

        help_activity_back_button_id.setOnClickListener {
            finish()
        }
    }
}
