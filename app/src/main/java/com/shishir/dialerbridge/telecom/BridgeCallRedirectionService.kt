package com.shishir.dialerbridge.telecom

import android.net.Uri
import android.telecom.CallRedirectionService
import android.telecom.PhoneAccountHandle
import com.shishir.dialerbridge.data.BridgeSettings
import com.shishir.dialerbridge.data.SettingsRepository
import com.shishir.dialerbridge.handoff.HandoffLauncher
import com.shishir.dialerbridge.util.BridgeLogger
import com.shishir.dialerbridge.util.NumberMasker
import com.shishir.dialerbridge.util.NumberNormalizer
import com.shishir.dialerbridge.util.SafetyChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class BridgeCallRedirectionService : CallRedirectionService() {

    companion object {
        private const val TAG = "CallRedirection"
        private const val SETTINGS_READ_TIMEOUT_MS = 1200L
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate)

    override fun onPlaceCall(
        handle: Uri,
        initialPhoneAccount: PhoneAccountHandle,
        allowInteractiveResponse: Boolean
    ) {
        val rawNumber = handle.schemeSpecificPart ?: ""
        BridgeLogger.i(TAG, "Intercepted call to ${NumberMasker.mask(rawNumber)}")

        serviceScope.launch {
            try {
                val settings = withTimeoutOrNull(SETTINGS_READ_TIMEOUT_MS) {
                    SettingsRepository(applicationContext).settingsFlow.first()
                } ?: BridgeSettings()

                if (!settings.routeAllOutgoingCalls) {
                    BridgeLogger.i(TAG, "Routing disabled in settings; placing unmodified")
                    placeCallUnmodified()
                    return@launch
                }

                when (val safety = SafetyChecker.check(rawNumber)) {
                    is SafetyChecker.SafetyResult.Valid -> {
                        BridgeLogger.i(TAG, "Cancelling cellular call; redirecting to Brilliant Connect")
                        cancelCall()
                        HandoffLauncher.performHandoff(
                            context = applicationContext,
                            rawNumber = rawNumber,
                            targetPackage = settings.targetPackageName,
                            strategy = settings.handoffStrategy,
                            formatOption = NumberNormalizer.formatOptionOf(settings.numberFormat)
                        )
                    }
                    is SafetyChecker.SafetyResult.Emergency -> {
                        BridgeLogger.w(TAG, "Emergency number; placing unmodified")
                        placeCallUnmodified()
                    }
                    is SafetyChecker.SafetyResult.Ussd -> {
                        BridgeLogger.w(TAG, "USSD code; placing unmodified")
                        placeCallUnmodified()
                    }
                    is SafetyChecker.SafetyResult.TooShort -> {
                        BridgeLogger.w(TAG, "Short number; placing unmodified")
                        placeCallUnmodified()
                    }
                }
            } catch (e: Exception) {
                BridgeLogger.e(TAG, "Error during call redirection", e)
                placeCallUnmodified()
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
