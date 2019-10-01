package com.sharedcalendar

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sharedcalendar.repository.Repository
import com.sharedcalendar.viewmodel.*
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

class MyApplication : Application(), KodeinAware {

    override val kodein by Kodein.lazy {
        bind() from singleton { Repository() }
        bind() from singleton { FirebaseAuth.getInstance() }
        bind() from singleton { FirebaseDatabase.getInstance() }
        bind() from singleton { CalendarViewModel(instance()) }
        bind() from singleton { CalendarViewModelFactory(instance()) }
        bind() from singleton { DayViewModel(instance()) }
        bind() from singleton { DayViewModelFactory(instance()) }
        bind() from singleton { AddEventViewModel() }
        bind() from singleton { AddEventViewModelFactory() }
    }

}