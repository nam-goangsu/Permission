package com.namgs.permissiopn.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.namgs.permissiopn.data.PermissionItem
import kotlin.collections.all
import kotlin.collections.filterNot

class PermissionManager(context: Context) {

    private val appContext = context.applicationContext

    fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun areRequiredPermissionsGranted(
        permissions: List<PermissionItem>
    ): Boolean {
        return permissions
            .filter { it.required }
            .all { isGranted(it.permission) }
    }

    fun getDeniedPermissions(
        permissions: List<PermissionItem>
    ): List<String> {
        return permissions
            .filterNot { isGranted(it.permission) }
            .map { it.permission }
    }
}
