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
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), View.OnClickListener, KodeinAware {
    override val kodein: Kodein by kodein()
    private val firebaseAuth: FirebaseAuth by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideStatusBar()
        isUserLogged()

        main_activity_sign_in_id.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        main_activity_button_login_id.setOnClickListener {
            startSound(R.raw.button)
            if (checkInternetConnection()) {
                main_activity_progressbar.visibility = View.VISIBLE
                loginUser()
            }
        }
    }

    private fun isUserLogged() {
        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, CalendarActivity::class.java))
            finish()
        }
    }

    override fun onClick(view: View?) {
        if (view == main_activity_sign_in_id) {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = main_activity_text_email_edit_id.text.toString().trim()
        val password = main_activity_text_password_edit_id.text.toString().trim()
        if (TextUtils.isEmpty(email)) {
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.enter_email_text),
                Toast.LENGTH_LONG,
                R.style.myToastMail
            ).show()
            main_activity_progressbar.visibility = View.GONE
            startVibration(100)
            return
        }
        if (TextUtils.isEmpty(password)) {
            showMessage(applicationContext, getString(R.string.enter_password_text))
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.enter_password_text),
                Toast.LENGTH_LONG,
                R.style.myToastPassword
            ).show()
            main_activity_progressbar.visibility = View.GONE
            startVibration(100)
            return
        }

        hideKeyboard(this)
        firebaseAuthIsLoged(email, password)
    }

    private fun firebaseAuthIsLoged(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    main_activity_progressbar.visibility = View.GONE
                    finish()
                } else {
                    StyleableToast.makeText(
                        applicationContext,
                        getString(R.string.invalid_password_text),
                        Toast.LENGTH_LONG,
                        R.style.myToastInvalid
                    ).show()
                    startVibration(100)
                    main_activity_progressbar.visibility = View.GONE
                }
            }
    }
}