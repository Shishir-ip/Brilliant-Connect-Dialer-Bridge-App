package com.shishir.dialerbridge.telecom

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import com.shishir.dialerbridge.data.BridgeSettings
import com.shishir.dialerbridge.data.SettingsRepository
import com.shishir.dialerbridge.handoff.HandoffLauncher
import com.shishir.dialerbridge.handoff.HandoffNotificationManager
import com.shishir.dialerbridge.util.BridgeLogger
import com.shishir.dialerbridge.util.NumberMasker
import com.shishir.dialerbridge.util.NumberNormalizer
import com.shishir.dialerbridge.util.SafetyChecker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class BridgeConnectionService : ConnectionService() {

    companion object {
        private const val TAG = "BridgeConnectionService"
        private const val SETTINGS_READ_TIMEOUT_MS = 1200L
    }

    private val repository by lazy { SettingsRepository(applicationContext) }

    /**
     * Reads user settings so the handoff honours the configured number format,
     * launch strategy and target label. Bounded so Telecom is never left waiting.
     */
    private fun loadSettings(): BridgeSettings = try {
        runBlocking {
            withTimeoutOrNull(SETTINGS_READ_TIMEOUT_MS) { repository.settingsFlow.first() }
        } ?: BridgeSettings()
    } catch (e: Exception) {
        BridgeLogger.w(TAG, "Falling back to default settings: ${e.message}")
        BridgeSettings()
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        BridgeLogger.i(TAG, "Outgoing connection requested by Telecom framework")

        val connection = BridgeConnection()

        if (request == null || request.address == null) {
            BridgeLogger.e(TAG, "Connection request or address is null")
            connection.setDisconnected(DisconnectCause(DisconnectCause.ERROR, "Invalid call address"))
            connection.destroy()
            return connection
        }

        try {
            val rawNumber = request.address.schemeSpecificPart ?: ""
            val masked = NumberMasker.mask(rawNumber)
            BridgeLogger.i(TAG, "Dialed number: $masked")

            val settings = loadSettings()

            when (val safety = SafetyChecker.check(rawNumber)) {
                is SafetyChecker.SafetyResult.Emergency -> {
                    BridgeLogger.w(TAG, "Emergency call blocked: ${safety.reason}")
                    HandoffNotificationManager.showAlertNotification(applicationContext, "Emergency Call Rejected", safety.reason)
                    connection.setDisconnected(DisconnectCause(DisconnectCause.REJECTED, safety.reason))
                    connection.destroy()
                }
                is SafetyChecker.SafetyResult.Ussd -> {
                    BridgeLogger.w(TAG, "USSD code blocked: ${safety.reason}")
                    HandoffNotificationManager.showAlertNotification(applicationContext, "USSD Code Blocked", safety.reason)
                    connection.setDisconnected(DisconnectCause(DisconnectCause.REJECTED, safety.reason))
                    connection.destroy()
                }
                is SafetyChecker.SafetyResult.TooShort -> {
                    BridgeLogger.w(TAG, "Short number blocked: ${safety.reason}")
                    HandoffNotificationManager.showAlertNotification(applicationContext, "Invalid Number", safety.reason)
                    connection.setDisconnected(DisconnectCause(DisconnectCause.REJECTED, safety.reason))
                    connection.destroy()
                }
                is SafetyChecker.SafetyResult.Valid -> {
                    BridgeLogger.i(TAG, "Safety checks passed; handing off to Brilliant Connect")
                    HandoffLauncher.performHandoff(
                        context = applicationContext,
                        rawNumber = rawNumber,
                        targetPackage = settings.targetPackageName,
                        strategy = settings.handoffStrategy,
                        formatOption = NumberNormalizer.formatOptionOf(settings.numberFormat)
                    )
                    connection.setDisconnected(
                        DisconnectCause(DisconnectCause.LOCAL, "Handed off to Brilliant Connect")
                    )
                    connection.destroy()
                }
            }
        } catch (e: Exception) {
            BridgeLogger.e(TAG, "Unexpected error in onCreateOutgoingConnection", e)
            HandoffNotificationManager.showAlertNotification(
                applicationContext,
                "Bridge Connection Error",
                "Failed to route call: ${e.message}"
            )
            connection.setDisconnected(DisconnectCause(DisconnectCause.ERROR, e.message))
            connection.destroy()
        }

        return connection
    }

    override fun onCreateOutgoingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateOutgoingConnectionFailed(connectionManagerPhoneAccount, request)
        BridgeLogger.e(TAG, "Telecom framework could not create the outgoing connection")
        HandoffNotificationManager.showAlertNotification(
            applicationContext,
            "Call Failed",
            "Telecom framework could not create the outgoing connection."
        )
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        BridgeLogger.w(TAG, "Incoming connections are not supported by the bridge")
        return Connection.createFailedConnection(
            DisconnectCause(DisconnectCause.REJECTED, "Incoming calls not supported")
        )
    }
}

/** Minimal connection: exists only to acknowledge the framework, then disconnects. */
class BridgeConnection : Connection()
