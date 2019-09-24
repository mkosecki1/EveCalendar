package com.sharedcalendar.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.muddzdev.styleabletoast.StyleableToast
import com.sharedcalendar.R
import com.sharedcalendar.utility.RegexValidation
import com.sharedcalendar.utility.checkInternetConnection
import com.sharedcalendar.utility.hideStatusBar
import com.sharedcalendar.utility.toggleVisibility
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
//        startActionBar()
        hideStatusBar()
        auth = FirebaseAuth.getInstance()

        button_registration_id.setOnClickListener {
            if (checkInternetConnection()) {
                registerUser()
            }
        }

        back_button_register_activity_id.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val email = edit_text_registration_email_id.text.toString().trim()
        val password = edit_text_registration_password_id.text.toString().trim()
        val validation = RegexValidation()
        if (TextUtils.isEmpty(email) && validation.emailRegexValidation(email)) {
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.enter_email_text),
                Toast.LENGTH_LONG,
                R.style.myToastMail
            ).show()
            progressbar_registration_activity.visibility = View.GONE
            return
        }
        if (TextUtils.isEmpty(password)) {
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.enter_password_text),
                Toast.LENGTH_LONG,
                R.style.myToastPassword
            ).show()
            progressbar_registration_activity.visibility = View.GONE
            return
        }

        hideKeyboard()
        progressbar_registration_activity.toggleVisibility()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    StyleableToast.makeText(
                        applicationContext,
                        getString(R.string.registered_text),
                        Toast.LENGTH_LONG,
                        R.style.myToastRegistered
                    ).show()
                    progressbar_registration_activity.visibility = View.GONE
                    startActivity(Intent(this, CalendarActivity::class.java))
                    finish()
                } else {
                    StyleableToast.makeText(
                        applicationContext,
                        getString(R.string.not_registered_text),
                        Toast.LENGTH_LONG,
                        R.style.myToastRegistered
                    ).show()
                }
                progressbar_registration_activity.toggleVisibility()
            }
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }
}