package com.sharedcalendar.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sharedcalendar.R
import com.sharedcalendar.utility.hideStatusBar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        hideStatusBar()

        GlobalScope.launch {
            delay(1500)
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }
    }
}