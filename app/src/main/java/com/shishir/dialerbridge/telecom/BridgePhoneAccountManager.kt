package com.shishir.dialerbridge.telecom

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.provider.Settings
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat
import com.shishir.dialerbridge.R
import com.shishir.dialerbridge.util.BridgeLogger

object BridgePhoneAccountManager {

    private const val TAG = "PhoneAccountManager"
    const val ACCOUNT_ID = "brilliant_bridge_account"

    fun getPhoneAccountHandle(context: Context): PhoneAccountHandle =
        PhoneAccountHandle(ComponentName(context, BridgeConnectionService::class.java), ACCOUNT_ID)

    fun registerPhoneAccount(context: Context): Boolean {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        if (telecomManager == null) {
            BridgeLogger.e(TAG, "TelecomManager not available on this device")
            return false
        }

        val handle = getPhoneAccountHandle(context)
        val phoneAccount = PhoneAccount.builder(handle, context.getString(R.string.phone_account_label))
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .setIcon(Icon.createWithResource(context, R.drawable.ic_bridge_account))
            .setShortDescription("Call via Brilliant Connect")
            .addSupportedUriScheme(PhoneAccount.SCHEME_TEL)
            .build()

        return try {
            telecomManager.registerPhoneAccount(phoneAccount)
            BridgeLogger.i(TAG, "PhoneAccount registered ($ACCOUNT_ID)")
            true
        } catch (e: Exception) {
            BridgeLogger.e(TAG, "Failed to register PhoneAccount", e)
            false
        }
    }

    fun isAccountRegistered(context: Context): Boolean {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager ?: return false
        return try {
            telecomManager.getPhoneAccount(getPhoneAccountHandle(context)) != null
        } catch (e: Exception) {
            BridgeLogger.w(TAG, "Error checking registration: ${e.message}")
            false
        }
    }

    fun isAccountEnabled(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            BridgeLogger.w(TAG, "READ_PHONE_STATE not granted; cannot query call-capable accounts")
            return false
        }

        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager ?: return false

        return try {
            telecomManager.callCapablePhoneAccounts.any { it == getPhoneAccountHandle(context) }
        } catch (e: SecurityException) {
            BridgeLogger.w(TAG, "SecurityException querying callCapablePhoneAccounts: ${e.message}")
            false
        } catch (e: Exception) {
            BridgeLogger.e(TAG, "Error checking if account is enabled", e)
            false
        }
    }

    fun openCallingAccountsSettings(context: Context) {
        val changeAccountsIntent = Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            if (context.packageManager.resolveActivity(changeAccountsIntent, 0) != null) {
                context.startActivity(changeAccountsIntent)
                return
            }
        } catch (e: Exception) {
            BridgeLogger.w(TAG, "ACTION_CHANGE_PHONE_ACCOUNTS failed: ${e.message}")
        }

        try {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        } catch (e: Exception) {
            BridgeLogger.e(TAG, "Cannot open system settings", e)
        }
    }
}
