package com.namgs.permissiopn.data

data class IntroSlide(
    val imageRes: Int,
    val title: String,
    val description: String,
    val durationMillis: Long = 4000L
)

data class PermissionItem(
    val permission: String,
    val title: String,
    val description: String,
    val required: Boolean = true
)



data class IntroUiState(
    val currentPage: Int = 0,
    val showPermissions: Boolean = false,
    val permissionRefresh: Int = 0,
    val allRequiredGranted: Boolean = false,
    val navigateToMain: Boolean = false
)