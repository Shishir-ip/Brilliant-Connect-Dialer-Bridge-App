package com.shishir.dialerbridge.handoff

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shishir.dialerbridge.autoselect.AutoSelectController
import com.shishir.dialerbridge.util.BridgeLogger

class HandoffActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            HandoffNotificationManager.ACTION_CONFIRM_CALL -> {
                BridgeLogger.i("HandoffActionReceiver", "Call confirmation received")
                AutoSelectController.userConfirmedCall()
            }
        }
    }
}
