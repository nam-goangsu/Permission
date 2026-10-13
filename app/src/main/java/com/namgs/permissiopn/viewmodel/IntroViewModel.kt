package com.namgs.permissiopn.viewmodel

import android.Manifest
import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.namgs.permissiopn.R
import com.namgs.permissiopn.data.IntroSlide
import com.namgs.permissiopn.data.IntroUiState
import com.namgs.permissiopn.data.PermissionItem
import com.namgs.permissiopn.permission.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class IntroViewModel(
    application: Application,
    val slides: List<IntroSlide>,
    val permissions: List<PermissionItem>
) : AndroidViewModel(application) {

    private val permissionManager =
        PermissionManager(application.applicationContext)

    private val _uiState = MutableStateFlow(IntroUiState())
    val uiState: StateFlow<IntroUiState> = _uiState.asStateFlow()

    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    fun showPermissionScreen() {
        _uiState.update { it.copy(showPermissions = true) }
        refreshPermissions()
    }

    fun refreshPermissions() {
        val allGranted =
            permissionManager.areRequiredPermissionsGranted(permissions)

        _uiState.update { state ->
            state.copy(
                permissionRefresh = state.permissionRefresh + 1,
                allRequiredGranted = allGranted,
                navigateToMain = state.navigateToMain || (
                        state.showPermissions && allGranted
                        )
            )
        }
    }

    fun onPermissionResult() {
        refreshPermissions()
    }

    fun isPermissionGranted(permission: String): Boolean {
        return permissionManager.isGranted(permission)
    }

    fun requestPermissionNames(): List<String> {
        return permissionManager.getDeniedPermissions(permissions)
    }
}