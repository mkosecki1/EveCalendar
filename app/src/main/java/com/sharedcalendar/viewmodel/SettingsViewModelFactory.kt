package com.sharedcalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sharedcalendar.repository.Repository

class SettingsViewModelFactory(private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = SettingsViewModel(repository) as T
}