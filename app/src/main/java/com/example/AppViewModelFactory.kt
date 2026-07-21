package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppRepository
import com.example.data.UserPreferencesRepository
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.MainViewModel

class AppViewModelFactory(
    private val repository: AppRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, userPrefs) as T
        }
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, userPrefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
