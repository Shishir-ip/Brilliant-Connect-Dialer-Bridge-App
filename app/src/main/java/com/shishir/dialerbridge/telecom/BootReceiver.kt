package com.shishir.dialerbridge.telecom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shishir.dialerbridge.data.SettingsRepository
import com.shishir.dialerbridge.handoff.HandoffNotificationManager
import com.shishir.dialerbridge.util.BridgeLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-registers the calling account after boot or app update so the bridge is
 * available immediately without opening the app.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        BridgeLogger.i(TAG, "Broadcast received: $action")
        HandoffNotificationManager.createChannels(context)

        // goAsync keeps the process alive while the DataStore read completes.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = SettingsRepository(context).settingsFlow.first()
                if (settings.startOnBoot) {
                    val registered = BridgePhoneAccountManager.registerPhoneAccount(context)
                    BridgeLogger.i(TAG, "PhoneAccount re-registration: $registered")
                } else {
                    BridgeLogger.i(TAG, "Start-on-boot disabled; skipping registration")
                }
            } catch (e: Exception) {
                BridgeLogger.e(TAG, "Error in BootReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
