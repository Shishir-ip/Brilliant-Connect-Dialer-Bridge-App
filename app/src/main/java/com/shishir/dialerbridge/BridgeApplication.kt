package com.shishir.dialerbridge

import android.app.Application
import com.shishir.dialerbridge.autoselect.AutoSelectController
import com.shishir.dialerbridge.data.SettingsRepository
import com.shishir.dialerbridge.handoff.HandoffNotificationManager
import com.shishir.dialerbridge.telecom.BridgePhoneAccountManager
import com.shishir.dialerbridge.util.BridgeLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BridgeApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        BridgeLogger.i("BridgeApplication", "Starting Brilliant Dialer Bridge")

        HandoffNotificationManager.createChannels(this)
        AutoSelectController.init(this)

        // Telecom registration is a binder call — keep it off the main thread
        // so cold start stays fast.
        appScope.launch {
            val registered = BridgePhoneAccountManager.registerPhoneAccount(this@BridgeApplication)
            BridgeLogger.i("BridgeApplication", "PhoneAccount registration: $registered")
        }

        // Keep the accessibility controller config in sync with user settings.
        appScope.launch {
            SettingsRepository(this@BridgeApplication).settingsFlow.collect { settings ->
                AutoSelectController.updateConfig(
                    targetPkg = settings.targetPackageName,
                    label = settings.targetOptionLabel,
                    autoPress = settings.autoPressCallButton,
                    autoSelect = settings.autoSelectBrilliantOut,
                    confirmFirst = settings.confirmBeforeCalling
                )
            }
        }
    }
}
