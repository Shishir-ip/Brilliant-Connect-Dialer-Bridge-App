package com.shishir.dialerbridge.ui

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telecom.TelecomManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shishir.dialerbridge.autoselect.AutoSelectController
import com.shishir.dialerbridge.data.BridgeSettings
import com.shishir.dialerbridge.data.SettingsRepository
import com.shishir.dialerbridge.handoff.HandoffLauncher
import com.shishir.dialerbridge.util.BridgeLogger
import com.shishir.dialerbridge.util.LogEntry
import com.shishir.dialerbridge.util.NumberMasker
import com.shishir.dialerbridge.util.NumberNormalizer
import com.shishir.dialerbridge.util.PermissionHelper
import com.shishir.dialerbridge.util.SafetyChecker
import com.shishir.dialerbridge.util.SetupStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BridgeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)
    private val context: Context get() = getApplication<Application>().applicationContext

    val settings: StateFlow<BridgeSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BridgeSettings()
    )

    private val _setupStatus = MutableStateFlow(
        PermissionHelper.getFullSetupStatus(context, HandoffLauncher.DEFAULT_TARGET_PACKAGE)
    )
    val setupStatus: StateFlow<SetupStatus> = _setupStatus.asStateFlow()

    val autoSelectState: StateFlow<AutoSelectController.State> = AutoSelectController.stateFlow
    val autoSelectResult: StateFlow<String> = AutoSelectController.lastResultFlow
    val logs: StateFlow<List<LogEntry>> = BridgeLogger.logsFlow

    private val _defaultDialerPkg = MutableStateFlow("Unknown")
    val defaultDialerPkg: StateFlow<String> = _defaultDialerPkg.asStateFlow()

    private val _callingAccountsList = MutableStateFlow<List<String>>(emptyList())
    val callingAccountsList: StateFlow<List<String>> = _callingAccountsList.asStateFlow()

    private val _redirectionRoleHeld = MutableStateFlow(false)
    val redirectionRoleHeld: StateFlow<Boolean> = _redirectionRoleHeld.asStateFlow()

    private val _showPaidWarningDialog = MutableStateFlow(false)
    val showPaidWarningDialog: StateFlow<Boolean> = _showPaidWarningDialog.asStateFlow()

    private val _testLogEntries = MutableStateFlow<List<String>>(emptyList())
    val testLogEntries: StateFlow<List<String>> = _testLogEntries.asStateFlow()

    private val _testRunning = MutableStateFlow(false)
    val testRunning: StateFlow<Boolean> = _testRunning.asStateFlow()

    val versionName: String = try {
        @Suppress("DEPRECATION")
        application.packageManager.getPackageInfo(application.packageName, 0).versionName ?: "1.0.0"
    } catch (e: Exception) {
        "1.0.0"
    }

    init {
        refreshStatus()
        viewModelScope.launch {
            val initial = repository.settingsFlow.first()
            if (!initial.hasSeenPaidCallWarning) {
                _showPaidWarningDialog.value = true
            }
        }
        // Any settings change can flip the connection path (e.g. enabling
        // "Route all outgoing calls" satisfies setup without a calling account).
        viewModelScope.launch {
            repository.settingsFlow.collect { latest ->
                recomputeSetupStatus(latest)
            }
        }
    }

    /** Permission/device-derived status combined with the given settings snapshot. */
    private fun recomputeSetupStatus(currentSettings: BridgeSettings) {
        _setupStatus.value = PermissionHelper.getFullSetupStatus(
            context = context,
            targetPackage = currentSettings.targetPackageName,
            routeAllOutgoingCalls = currentSettings.routeAllOutgoingCalls,
            redirectionRoleHeld = _redirectionRoleHeld.value
        )
    }

    fun refreshStatus() {
        _redirectionRoleHeld.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val rm = context.getSystemService(android.app.role.RoleManager::class.java)
                rm?.isRoleHeld(android.app.role.RoleManager.ROLE_CALL_REDIRECTION) ?: false
            } catch (e: Exception) {
                false
            }
        } else {
            // CallRedirectionService only exists on Android 10+; never satisfy
            // the redirection path on older APIs.
            false
        }

        recomputeSetupStatus(settings.value)

        val tm = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager ?: return
        try {
            _defaultDialerPkg.value = tm.defaultDialerPackage ?: "System default"
        } catch (e: Exception) {
            _defaultDialerPkg.value = "Unavailable"
        }

        _callingAccountsList.value = if (PermissionHelper.hasPhonePermissions(context)) {
            try {
                tm.callCapablePhoneAccounts.map { "${it.componentName.packageName} (${it.id})" }
            } catch (e: Exception) {
                listOf("Error: ${e.message}")
            }
        } else {
            emptyList()
        }
    }

    fun updateAutoSelect(enabled: Boolean) = viewModelScope.launch { repository.setAutoSelect(enabled) }
    fun updateTargetLabel(label: String) = viewModelScope.launch { repository.setTargetOptionLabel(label) }
    fun updateAutoPressCallButton(enabled: Boolean) = viewModelScope.launch { repository.setAutoPressCallButton(enabled) }
    fun updateConfirmBeforeCall(enabled: Boolean) = viewModelScope.launch { repository.setConfirmBeforeCalling(enabled) }
    fun updateNumberFormat(format: String) = viewModelScope.launch { repository.setNumberFormat(format) }
    fun updateHandoffStrategy(strategy: String) = viewModelScope.launch { repository.setHandoffStrategy(strategy) }
    fun updateRouteAllCalls(enabled: Boolean) = viewModelScope.launch { repository.setRouteAllOutgoingCalls(enabled) }
    fun updateStartOnBoot(enabled: Boolean) = viewModelScope.launch { repository.setStartOnBoot(enabled) }

    /** One-tap switch to the redirection path used on OEMs that hide calling accounts. */
    fun enableRedirectionRoute() = updateRouteAllCalls(true)

    val deviceManufacturer: String =
        Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }

    fun dismissPaidWarning() = viewModelScope.launch {
        repository.setHasSeenPaidCallWarning(true)
        _showPaidWarningDialog.value = false
    }

    fun runTestHandoff(number: String) {
        val trimmed = number.trim()
        if (trimmed.isEmpty() || _testRunning.value) return

        _testRunning.value = true
        val lines = mutableListOf<String>()
        fun log(line: String) {
            lines += NumberMasker.maskInText(line)
            _testLogEntries.value = lines.toList()
        }

        viewModelScope.launch {
            try {
                val currentSettings = repository.settingsFlow.first()
                val masked = NumberMasker.mask(trimmed)
                log("Testing handoff for $masked…")

                val safety = SafetyChecker.check(trimmed)
                if (safety !is SafetyChecker.SafetyResult.Valid) {
                    val (kind, reason) = when (safety) {
                        is SafetyChecker.SafetyResult.Emergency -> "Emergency number" to safety.reason
                        is SafetyChecker.SafetyResult.Ussd -> "USSD code" to safety.reason
                        is SafetyChecker.SafetyResult.TooShort -> "Invalid number" to safety.reason
                        else -> "Blocked" to ""
                    }
                    log("✕ Blocked: $kind")
                    log(reason)
                    return@launch
                }
                log("✓ Safety checks passed")

                val installed = HandoffLauncher.isTargetInstalled(context, currentSettings.targetPackageName)
                log(if (installed) "✓ Brilliant Connect installed" else "✕ Brilliant Connect not installed")

                val formatOption = NumberNormalizer.formatOptionOf(currentSettings.numberFormat)
                log("Number format: ${formatOption.name.lowercase().replace('_', '-')}")

                AutoSelectController.updateConfig(
                    targetPkg = currentSettings.targetPackageName,
                    label = currentSettings.targetOptionLabel,
                    autoPress = currentSettings.autoPressCallButton,
                    autoSelect = currentSettings.autoSelectBrilliantOut,
                    confirmFirst = currentSettings.confirmBeforeCalling
                )

                val success = HandoffLauncher.performHandoff(
                    context = context,
                    rawNumber = trimmed,
                    targetPackage = currentSettings.targetPackageName,
                    strategy = currentSettings.handoffStrategy,
                    formatOption = formatOption
                )

                if (success) {
                    log("✓ Handoff sent — Brilliant Connect is opening")
                    if (currentSettings.autoPressCallButton && currentSettings.autoSelectBrilliantOut) {
                        log("Auto-select armed. Watch the call complete in Brilliant Connect.")
                    }
                } else {
                    log("✕ Handoff failed — check the logs below")
                }
            } catch (e: Exception) {
                BridgeLogger.e("TestHandoff", "Test failed", e)
                log("✕ Error: ${e.message}")
            } finally {
                _testRunning.value = false
            }
        }
    }

    fun copyLogsToClipboard(onCopied: () -> Unit = {}) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
        clipboard?.setPrimaryClip(
            android.content.ClipData.newPlainText("Dialer Bridge Logs", BridgeLogger.getAllLogsFormatted())
        )
        onCopied()
    }

    fun clearLogs() = BridgeLogger.clear()
}
