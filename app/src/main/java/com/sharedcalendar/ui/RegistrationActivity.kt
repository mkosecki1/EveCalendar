package com.sharedcalendar.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.muddzdev.styleabletoast.StyleableToast
import com.sharedcalendar.R
import com.sharedcalendar.database.Statics
import com.sharedcalendar.utility.*
import kotlinx.android.synthetic.main.activity_registration.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class RegistrationActivity : AppCompatActivity(), KodeinAware {
    override val kodein: Kodein by kodein()
    private val firebaseAuth: FirebaseAuth by instance()
    private var staticsDate = Statics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        hideStatusBar()
        setNavigationBarColour(window, this)

        register_activity_button_registration_id.setOnClickListener {
            if (checkInternetConnection()) {
                registerUser()
            }
        }

        register_activity_back_button_id.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val email = register_activity_text_email_edit_id.text.toString().trim()
        val password = register_activity_text_password_edit_id.text.toString().trim()
        val firebaseDate  = email.replace("[^a-zA-Z0-9@_]".toRegex(),"")+"Events"
        val firebaseType  = firebaseDate+"Type"
        staticsDate.changeDate(firebaseDate, firebaseType)
        val validation = RegexValidation()
        if (TextUtils.isEmpty(email) ) {
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.enter_email_text),
                Toast.LENGTH_LONG,
                R.style.MyToastMail
            ).show()
            register_activity_progressbar.visibility = View.GONE
            return
        }
        if (TextUtils.isEmpty(password)) {
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.enter_password_text),
                Toast.LENGTH_LONG,
                R.style.MyToastPassword
            ).show()
            register_activity_progressbar.visibility = View.GONE
            return
        }

        if (validation.emailRegexValidation(email) && validation.passwordRegexValidation(password)) {
            hideKeyboard(this)
            register_activity_progressbar.toggleVisibility()
            firebaseAuthIsRegistered(email, password)
        } else {
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.password_must_contain),
                Toast.LENGTH_LONG,
                R.style.MyToastPassword
            ).show()
            register_activity_progressbar.visibility = View.GONE
            return
        }
    }

    private fun firebaseAuthIsRegistered(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    StyleableToast.makeText(
                        applicationContext,
                        getString(R.string.registered_text),
                        Toast.LENGTH_LONG,
                        R.style.MyToastRegistered
                    ).show()
                    register_activity_progressbar.visibility = View.GONE
                    startActivity(Intent(this, CalendarActivity::class.java))
                    finish()
                } else {
                    StyleableToast.makeText(
                        applicationContext,
                        getString(R.string.not_registered_text),
                        Toast.LENGTH_LONG,
                        R.style.MyToastRegistered
                    ).show()
                }
                register_activity_progressbar.toggleVisibility()
            }
    }
}