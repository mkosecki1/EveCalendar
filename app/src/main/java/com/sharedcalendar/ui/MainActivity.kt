package com.sharedcalendar.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.muddzdev.styleabletoast.StyleableToast
import com.sharedcalendar.R
import com.sharedcalendar.database.Statics
import com.sharedcalendar.utility.*
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class MainActivity : AppCompatActivity(), View.OnClickListener, KodeinAware {
    override val kodein: Kodein by kodein()
    private lateinit var resetEmailField: EditText
    private val firebaseAuth: FirebaseAuth by instance()
    private var staticsDate = Statics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideStatusBar()

        setNavigationBarColour(window, this)
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

        main_activity_reset_password_id.setOnClickListener {
            val dialogLayout = layoutInflater.inflate(R.layout.dialog_reset_password, null)
            resetEmailField = dialogLayout.findViewById(R.id.reset_password_edit_text_id)
            openDialog(
                this,
                getString(R.string.reset_password_info),
                dialogLayout,
                ::resetUsersPassword
            )
        }
    }


    private fun isUserLogged() {
        if (firebaseAuth.currentUser != null) {
            val firebaseDate =
                firebaseAuth.currentUser?.email!!.replace("[^a-zA-Z0-9@_]".toRegex(), "") + "Events"
            val firebaseType = firebaseDate + "Type"
            staticsDate.changeDate(firebaseDate, firebaseType)
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
        val firebaseDate = email.replace("[^a-zA-Z0-9@_]".toRegex(), "") + "Events"
        val firebaseType = firebaseDate + "Type"
        staticsDate.changeDate(firebaseDate, firebaseType)
        val password = main_activity_text_password_edit_id.text.toString().trim()
        if (TextUtils.isEmpty(email)) {
            StyleableToast.makeText(
                applicationContext,
                getString(R.string.enter_email_text),
                Toast.LENGTH_LONG,
                R.style.MyToastMail
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
                R.style.MyToastPassword
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
                        R.style.MyToastInvalid
                    ).show()
                    startVibration(VIBRATION_DURATION)
                    main_activity_progressbar.visibility = View.GONE
                }
            }
    }

    private fun resetUsersPassword() {
        firebaseAuth.useAppLanguage()
        if (!resetEmailField.text.isNullOrEmpty()) {
            firebaseAuth.sendPasswordResetEmail(resetEmailField.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        StyleableToast.makeText(
                            applicationContext,
                            getString(R.string.reset_password_correct_toast),
                            Toast.LENGTH_LONG,
                            R.style.MyToastMail
                        ).show()
                    } else {
                        StyleableToast.makeText(
                            applicationContext,
                            getString(R.string.reset_password_incorrect_toast),
                            Toast.LENGTH_LONG,
                            R.style.MyToastMail
                        ).show()
                    }
                }
        }
    }

    companion object {
        const val VIBRATION_DURATION = 90L
    }
}