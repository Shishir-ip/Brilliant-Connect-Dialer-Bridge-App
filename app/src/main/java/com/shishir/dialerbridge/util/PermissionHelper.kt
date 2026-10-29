package com.shishir.dialerbridge.util

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.shishir.dialerbridge.autoselect.BrilliantOutAccessibilityService
import com.shishir.dialerbridge.handoff.HandoffLauncher
import com.shishir.dialerbridge.telecom.BridgePhoneAccountManager

data class SetupStatus(
    val phonePermissionsGranted: Boolean,
    val notificationPermissionGranted: Boolean,
    val overlayPermissionGranted: Boolean,
    val fullScreenIntentGranted: Boolean,
    val batteryOptimizationIgnored: Boolean,
    val targetAppInstalled: Boolean,
    val accessibilityServiceEnabled: Boolean,
    val callingAccountEnabled: Boolean,
    val routeAllOutgoingCalls: Boolean = false,
    val redirectionRoleHeld: Boolean = false
) {
    /**
     * Many OEM dialers (Oppo/Realme/OnePlus ColorOS, some Xiaomi builds) hide
     * third-party calling accounts entirely, making the account toggle
     * impossible. The call-redirection path ("Route all outgoing calls" +
     * default redirection app) works without a calling account, so the setup
     * passes when either path is available.
     */
    val redirectionPathActive: Boolean
        get() = routeAllOutgoingCalls && redirectionRoleHeld

    val connectionStepDone: Boolean
        get() = callingAccountEnabled || redirectionPathActive

    val allReady: Boolean
        get() = phonePermissionsGranted &&
            notificationPermissionGranted &&
            overlayPermissionGranted &&
            fullScreenIntentGranted &&
            batteryOptimizationIgnored &&
            targetAppInstalled &&
            accessibilityServiceEnabled &&
            connectionStepDone

    val remainingCount: Int
        get() = listOf(
            phonePermissionsGranted,
            notificationPermissionGranted,
            overlayPermissionGranted,
            fullScreenIntentGranted,
            batteryOptimizationIgnored,
            targetAppInstalled,
            accessibilityServiceEnabled,
            connectionStepDone
        ).count { !it }
}

object PermissionHelper {

    fun hasPhonePermissions(context: Context): Boolean {
        val readPhone = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        val callPhone = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
        return readPhone && callPhone
    }

    fun hasNotificationPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    fun hasOverlayPermission(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun hasFullScreenIntentPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)
                ?.canUseFullScreenIntent() ?: false
        } else {
            true
        }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        if (BrilliantOutAccessibilityService.isConnected()) return true

        val component = ComponentName(context, BrilliantOutAccessibilityService::class.java)
        val expectedComponent = component.flattenToString()
        val expectedShort = component.flattenToShortString()

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""

        val accessibilityEnabled = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            0
        ) == 1

        return accessibilityEnabled &&
            (enabledServices.contains(expectedComponent) || enabledServices.contains(expectedShort))
    }

    fun getFullSetupStatus(
        context: Context,
        targetPackage: String,
        routeAllOutgoingCalls: Boolean = false,
        redirectionRoleHeld: Boolean = false
    ): SetupStatus = SetupStatus(
        phonePermissionsGranted = hasPhonePermissions(context),
        notificationPermissionGranted = hasNotificationPermission(context),
        overlayPermissionGranted = hasOverlayPermission(context),
        fullScreenIntentGranted = hasFullScreenIntentPermission(context),
        batteryOptimizationIgnored = isIgnoringBatteryOptimizations(context),
        targetAppInstalled = HandoffLauncher.isTargetInstalled(context, targetPackage),
        accessibilityServiceEnabled = isAccessibilityServiceEnabled(context),
        callingAccountEnabled = BridgePhoneAccountManager.isAccountEnabled(context),
        routeAllOutgoingCalls = routeAllOutgoingCalls,
        redirectionRoleHeld = redirectionRoleHeld
    )
}
