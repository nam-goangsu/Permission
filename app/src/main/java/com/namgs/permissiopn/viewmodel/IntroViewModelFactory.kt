package com.namgs.permissiopn.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.namgs.permissiopn.data.IntroSlide
import com.namgs.permissiopn.data.PermissionItem

class IntroViewModelFactory(
    private val application: Application,
    private val slides: List<IntroSlide>,
    private val permissions: List<PermissionItem>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        require(
            modelClass.isAssignableFrom(IntroViewModel::class.java)
        ) {
            "Unknown ViewModel class: ${modelClass.name}"
        }

        return IntroViewModel(
            application = application,
            slides = slides,
            permissions = permissions
        ) as T
    }
}