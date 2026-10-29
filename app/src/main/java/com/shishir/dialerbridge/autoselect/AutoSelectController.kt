package com.shishir.dialerbridge.autoselect

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.shishir.dialerbridge.handoff.HandoffNotificationManager
import com.shishir.dialerbridge.util.BridgeLogger
import com.shishir.dialerbridge.util.NumberMasker
import com.shishir.dialerbridge.util.NumberNormalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Watches the Brilliant Connect window once a handoff is armed: verifies the
 * keypad contains the exact dialed number, presses the call button, and picks
 * the configured call-type row from the bottom sheet.
 */
object AutoSelectController {

    private const val TAG = "AutoSelectController"
    private const val OVERALL_TIMEOUT_MS = 12000L
    private const val MIN_ARM_INTERVAL_MS = 3000L

    const val ID_DIALER_EDIT_TEXT = "com.brilliant.connect.com.bd:id/screen_tab_dialer_editText_number"
    const val ID_DIALER_BUTTON = "com.brilliant.connect.com.bd:id/dialler_button"
    const val ID_CALL_TYPE_LIST = "com.brilliant.connect.com.bd:id/call_type_list"
    const val ID_SHEET_ITEM_TEXT = "com.brilliant.connect.com.bd:id/sheet_item_text"
    const val DEFAULT_TARGET_LABEL = "Brilliant-OUT"
    const val DEFAULT_TARGET_PACKAGE = "com.brilliant.connect.com.bd"

    enum class State {
        IDLE,
        WAIT_KEYPAD,
        WAIT_CONFIRMATION,
        WAIT_SHEET,
        DONE,
        TIMEOUT,
        ERROR
    }

    interface AccessibilityBridge {
        fun getRootNode(): AccessibilityNodeInfo?
        fun dispatchTapGesture(x: Float, y: Float, onComplete: (Boolean) -> Unit)
        fun isServiceBound(): Boolean
    }

    private var activeBridge: AccessibilityBridge? = null
    private var appContext: Context? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentState = State.IDLE
    private var armedNumber = ""
    private var armedDigits = ""
    private var lastArmedTimestamp = 0L
    private var callButtonPressed = false
    private var targetLabel = DEFAULT_TARGET_LABEL
    private var autoPressCall = true
    private var autoSelectSheet = true
    private var confirmBeforeCall = false
    private var targetPackage = DEFAULT_TARGET_PACKAGE

    private val _stateFlow = MutableStateFlow(State.IDLE)
    val stateFlow: StateFlow<State> = _stateFlow.asStateFlow()

    private val _lastResultFlow = MutableStateFlow("Idle")
    val lastResultFlow: StateFlow<String> = _lastResultFlow.asStateFlow()

    private val timeoutRunnable = Runnable {
        if (currentState != State.IDLE && currentState != State.DONE) {
            val stopState = currentState
            BridgeLogger.w(TAG, "Timeout expired in state $stopState")
            _lastResultFlow.value = "Timed out in $stopState"
            setState(State.TIMEOUT)
            appContext?.let {
                HandoffNotificationManager.showAutoSelectFailedNotification(
                    it,
                    "Couldn't auto-select $targetLabel — please select it manually."
                )
            }
            disarm()
        }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun registerBridge(bridge: AccessibilityBridge) {
        activeBridge = bridge
        BridgeLogger.i(TAG, "Accessibility bridge registered")
    }

    fun unregisterBridge() {
        activeBridge = null
        BridgeLogger.i(TAG, "Accessibility bridge unregistered")
    }

    fun isAccessibilityServiceConnected(): Boolean = activeBridge?.isServiceBound() == true

    fun updateConfig(
        targetPkg: String = DEFAULT_TARGET_PACKAGE,
        label: String = DEFAULT_TARGET_LABEL,
        autoPress: Boolean = true,
        autoSelect: Boolean = true,
        confirmFirst: Boolean = false
    ) {
        targetPackage = targetPkg
        targetLabel = label.trim()
        autoPressCall = autoPress
        autoSelectSheet = autoSelect
        confirmBeforeCall = confirmFirst
    }

    @Synchronized
    fun arm(number: String): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastArmedTimestamp < MIN_ARM_INTERVAL_MS) {
            BridgeLogger.w(TAG, "arm() called too soon (${now - lastArmedTimestamp}ms), ignoring")
            return false
        }

        lastArmedTimestamp = now
        armedNumber = number
        armedDigits = NumberNormalizer.digitsOnly(number)
        callButtonPressed = false

        if (armedDigits.isEmpty()) {
            BridgeLogger.w(TAG, "arm() called with empty number digits")
            return false
        }

        mainHandler.removeCallbacks(timeoutRunnable)
        mainHandler.postDelayed(timeoutRunnable, OVERALL_TIMEOUT_MS)

        setState(State.WAIT_KEYPAD)
        val masked = NumberMasker.mask(number)
        BridgeLogger.i(TAG, "Armed for $masked (12s timeout)")
        _lastResultFlow.value = "Armed ($masked)"
        return true
    }

    @Synchronized
    fun disarm() {
        mainHandler.removeCallbacks(timeoutRunnable)
        armedNumber = ""
        armedDigits = ""
        callButtonPressed = false
        if (currentState != State.TIMEOUT && currentState != State.ERROR && currentState != State.DONE) {
            setState(State.IDLE)
        }
    }

    @Synchronized
    fun resetForTesting() {
        mainHandler.removeCallbacks(timeoutRunnable)
        lastArmedTimestamp = 0L
        armedNumber = ""
        armedDigits = ""
        callButtonPressed = false
        setState(State.IDLE)
    }

    private fun setState(newState: State) {
        currentState = newState
        _stateFlow.value = newState
    }

    fun userConfirmedCall() {
        if (currentState == State.WAIT_CONFIRMATION) {
            BridgeLogger.i(TAG, "User confirmed call via notification action")
            setState(State.WAIT_KEYPAD)
            pressCallButtonIfReady()
        }
    }

    fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventPkg = event.packageName?.toString() ?: ""

        if (eventPkg.isNotEmpty() && eventPkg != targetPackage) {
            if (currentState == State.WAIT_KEYPAD || currentState == State.WAIT_SHEET) {
                BridgeLogger.w(TAG, "Window switched to $eventPkg while armed; disarming for safety")
                _lastResultFlow.value = "Left target app"
                disarm()
            }
            return
        }

        if (currentState == State.IDLE || currentState == State.DONE ||
            currentState == State.TIMEOUT || currentState == State.ERROR
        ) {
            return
        }

        val bridge = activeBridge ?: return
        val rootNode = bridge.getRootNode() ?: return

        try {
            when (currentState) {
                State.WAIT_KEYPAD -> processWaitKeypad(rootNode)
                State.WAIT_SHEET -> processWaitSheet(rootNode, bridge)
                else -> { }
            }
        } catch (e: Exception) {
            BridgeLogger.e(TAG, "Error processing accessibility event", e)
            _lastResultFlow.value = "Error: ${e.message}"
            setState(State.ERROR)
            disarm()
        }
    }

    private fun processWaitKeypad(rootNode: AccessibilityNodeInfo) {
        val numberNodes = rootNode.findAccessibilityNodeInfosByViewId(ID_DIALER_EDIT_TEXT)
        if (numberNodes.isNullOrEmpty()) return

        val keypadText = numberNodes[0].text?.toString() ?: ""
        val keypadDigits = NumberNormalizer.digitsOnly(keypadText)

        if (!NumberNormalizer.numbersMatch(armedDigits, keypadDigits)) {
            BridgeLogger.w(
                TAG,
                "Keypad (${NumberMasker.mask(keypadText)}) does not match dialed number " +
                    "(${NumberMasker.mask(armedNumber)}). Aborting to prevent a wrong call."
            )
            _lastResultFlow.value = "Number mismatch — aborted"
            return
        }

        BridgeLogger.i(TAG, "Keypad number verified")
        _lastResultFlow.value = "Keypad verified"

        if (!autoPressCall) {
            BridgeLogger.i(TAG, "Auto-press call button is OFF; stopped at keypad")
            _lastResultFlow.value = "Stopped at keypad (setting off)"
            setState(State.DONE)
            disarm()
            return
        }

        if (confirmBeforeCall) {
            BridgeLogger.i(TAG, "Confirm-before-call is ON; awaiting user confirmation")
            setState(State.WAIT_CONFIRMATION)
            appContext?.let {
                HandoffNotificationManager.showConfirmationNotification(it, armedNumber)
            }
            return
        }

        pressCallButtonIfReady(rootNode)
    }

    private fun pressCallButtonIfReady(root: AccessibilityNodeInfo? = null) {
        if (callButtonPressed) return

        val bridge = activeBridge ?: return
        val rootNode = root ?: bridge.getRootNode() ?: return

        val callButtons = rootNode.findAccessibilityNodeInfosByViewId(ID_DIALER_BUTTON)
        if (callButtons.isNullOrEmpty()) {
            BridgeLogger.w(TAG, "Dialer call button not found yet")
            return
        }

        val clicked = performClickOnNodeOrAncestor(callButtons[0])
        if (clicked) {
            callButtonPressed = true
            BridgeLogger.i(TAG, "Call button pressed; waiting for call-type sheet")
            _lastResultFlow.value = "Call button pressed"
            setState(State.WAIT_SHEET)
        } else {
            BridgeLogger.w(TAG, "Failed to click dialer button")
        }
    }

    private fun processWaitSheet(rootNode: AccessibilityNodeInfo, bridge: AccessibilityBridge) {
        if (!autoSelectSheet) {
            BridgeLogger.i(TAG, "Auto-select sheet is OFF; stopped after call button")
            _lastResultFlow.value = "Call placed manually"
            setState(State.DONE)
            disarm()
            return
        }

        val listNodes = rootNode.findAccessibilityNodeInfosByViewId(ID_CALL_TYPE_LIST)
        if (listNodes.isNullOrEmpty()) return

        val itemTextNodes = rootNode.findAccessibilityNodeInfosByViewId(ID_SHEET_ITEM_TEXT)
        if (itemTextNodes.isNullOrEmpty()) return

        var matchingNode: AccessibilityNodeInfo? = null
        for (node in itemTextNodes) {
            val text = node.text?.toString()?.trim() ?: ""
            if (text.equals(targetLabel, ignoreCase = true)) {
                matchingNode = node
                break
            }
        }
        if (matchingNode == null) return

        BridgeLogger.i(TAG, "Found target row '$targetLabel'")

        val parentRow = matchingNode.parent
        var success = false

        if (parentRow != null) {
            success = parentRow.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (success) BridgeLogger.i(TAG, "Clicked parent row")
        }

        if (!success) {
            success = performClickOnNodeOrAncestor(matchingNode)
            if (success) BridgeLogger.i(TAG, "Clicked ancestor node")
        }

        if (!success) {
            val bounds = Rect()
            if (parentRow != null) parentRow.getBoundsInScreen(bounds) else matchingNode.getBoundsInScreen(bounds)

            if (bounds.width() > 0 && bounds.height() > 0) {
                val centerX = bounds.centerX().toFloat()
                val centerY = bounds.centerY().toFloat()
                BridgeLogger.i(TAG, "Falling back to gesture tap at ($centerX, $centerY)")

                bridge.dispatchTapGesture(centerX, centerY) { gestureSucceeded ->
                    _lastResultFlow.value = if (gestureSucceeded) {
                        "Call placed ($targetLabel)"
                    } else {
                        "Gesture tap failed"
                    }
                    setState(State.DONE)
                    disarm()
                }
                return
            } else {
                BridgeLogger.w(TAG, "Target row bounds are empty; cannot gesture tap")
            }
        } else {
            _lastResultFlow.value = "Call placed ($targetLabel)"
            setState(State.DONE)
            disarm()
        }
    }

    private fun performClickOnNodeOrAncestor(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) {
                if (current.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            }
            current = current.parent
        }
        return false
    }
}
