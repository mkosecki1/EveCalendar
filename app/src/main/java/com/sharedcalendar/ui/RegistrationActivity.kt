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
import com.sharedcalendar.utility.*
import kotlinx.android.synthetic.main.activity_registration.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class RegistrationActivity : AppCompatActivity(), KodeinAware {
    override val kodein: Kodein by kodein()
    private val firebaseAuth: FirebaseAuth by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        hideStatusBar()

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
        val validation = RegexValidation()
        if (TextUtils.isEmpty(email) && validation.emailRegexValidation(email)) {
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.enter_email_text),
                Toast.LENGTH_LONG,
                R.style.myToastMail
            ).show()
            registration_activity_progressbar.visibility = View.GONE
            return
        }
        if (TextUtils.isEmpty(password)) {
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.enter_password_text),
                Toast.LENGTH_LONG,
                R.style.myToastPassword
            ).show()
            registration_activity_progressbar.visibility = View.GONE
            return
        }

        hideKeyboard(this)
        registration_activity_progressbar.toggleVisibility()
        firebaseAuthIsRegistered(email, password)
    }

    private fun firebaseAuthIsRegistered(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    StyleableToast.makeText(
                        applicationContext,
                        getString(R.string.registered_text),
                        Toast.LENGTH_LONG,
                        R.style.myToastRegistered
                    ).show()
                    registration_activity_progressbar.visibility = View.GONE
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
                registration_activity_progressbar.toggleVisibility()
            }
    }
}