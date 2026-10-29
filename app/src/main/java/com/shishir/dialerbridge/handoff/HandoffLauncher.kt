package com.shishir.dialerbridge.handoff

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.shishir.dialerbridge.autoselect.AutoSelectController
import com.shishir.dialerbridge.util.BridgeLogger
import com.shishir.dialerbridge.util.NumberMasker
import com.shishir.dialerbridge.util.NumberNormalizer

/** Routes a dialed number into the Brilliant Connect app. */
object HandoffLauncher {

    private const val TAG = "HandoffLauncher"
    const val DEFAULT_TARGET_PACKAGE = "com.brilliant.connect.com.bd"
    const val DEFAULT_STRATEGY = "dial_intent"

    interface IntentResolver {
        fun resolveActivity(intent: Intent): Boolean
    }

    class DefaultIntentResolver(private val context: Context) : IntentResolver {
        override fun resolveActivity(intent: Intent): Boolean = try {
            context.packageManager.resolveActivity(intent, 0) != null
        } catch (e: Exception) {
            false
        }
    }

    fun isTargetInstalled(context: Context, targetPackage: String = DEFAULT_TARGET_PACKAGE): Boolean =
        try {
            context.packageManager.getPackageInfo(targetPackage, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

    fun openPlayStore(context: Context, targetPackage: String = DEFAULT_TARGET_PACKAGE) {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$targetPackage")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        try {
            context.startActivity(marketIntent)
        } catch (e: ActivityNotFoundException) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$targetPackage")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            try {
                context.startActivity(webIntent)
            } catch (e2: Exception) {
                BridgeLogger.e(TAG, "Cannot open browser for Play Store link", e2)
            }
        }
    }

    fun copyToClipboard(context: Context, number: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("Dialed Number", number))
        Toast.makeText(context, "Number copied — open Brilliant Connect and paste", Toast.LENGTH_LONG).show()
    }

    /**
     * Opens Brilliant Connect pre-filled with [rawNumber]. When the number was
     * launched by the telecom layer, arms the accessibility auto-select controller.
     */
    fun performHandoff(
        context: Context,
        rawNumber: String,
        targetPackage: String = DEFAULT_TARGET_PACKAGE,
        strategy: String = DEFAULT_STRATEGY,
        formatOption: NumberNormalizer.FormatOption = NumberNormalizer.FormatOption.AS_DIALED,
        resolver: IntentResolver = DefaultIntentResolver(context)
    ): Boolean {
        val normalizedNumber = NumberNormalizer.format(rawNumber, formatOption)
        val masked = NumberMasker.mask(normalizedNumber)
        BridgeLogger.i(TAG, "Handoff $masked → $targetPackage (strategy: $strategy)")

        if (!isTargetInstalled(context, targetPackage)) {
            BridgeLogger.w(TAG, "Target app $targetPackage is not installed")
            HandoffNotificationManager.showAlertNotification(
                context,
                "Brilliant Connect Not Installed",
                "Install Brilliant Connect to place calls through the bridge."
            )
            openPlayStore(context, targetPackage)
            return false
        }

        var startedSuccessfully = false
        val canDrawOverlays = Settings.canDrawOverlays(context)

        val intentsToTry = mutableListOf<Pair<String, Intent>>()

        when (strategy) {
            "view_intent" -> {
                intentsToTry += "ACTION_VIEW" to Intent(Intent.ACTION_VIEW, Uri.parse("tel:$normalizedNumber")).apply {
                    setPackage(targetPackage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                intentsToTry += "ACTION_DIAL" to Intent(Intent.ACTION_DIAL, Uri.parse("tel:$normalizedNumber")).apply {
                    setPackage(targetPackage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
            "launcher_copy" -> {
                copyToClipboard(context, normalizedNumber)
                context.packageManager.getLaunchIntentForPackage(targetPackage)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }?.let { intentsToTry += "LAUNCHER" to it }
            }
            else -> {
                intentsToTry += "ACTION_DIAL" to Intent(Intent.ACTION_DIAL, Uri.parse("tel:$normalizedNumber")).apply {
                    setPackage(targetPackage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                intentsToTry += "ACTION_VIEW" to Intent(Intent.ACTION_VIEW, Uri.parse("tel:$normalizedNumber")).apply {
                    setPackage(targetPackage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
            }
        }

        if (canDrawOverlays) {
            for ((name, intent) in intentsToTry) {
                if (resolver.resolveActivity(intent)) {
                    try {
                        context.startActivity(intent)
                        BridgeLogger.i(TAG, "Launched via $name")
                        startedSuccessfully = true
                        break
                    } catch (e: Exception) {
                        BridgeLogger.w(TAG, "Direct launch with $name failed: ${e.message}")
                    }
                }
            }
        }

        if (!startedSuccessfully) {
            BridgeLogger.i(TAG, "Overlay missing or direct launch failed; posting handoff notification")
            try {
                HandoffNotificationManager.showHandoffNotification(context, normalizedNumber, targetPackage)
                startedSuccessfully = true
            } catch (e: Exception) {
                BridgeLogger.e(TAG, "Failed to post handoff notification", e)
            }
        }

        if (!startedSuccessfully) {
            BridgeLogger.w(TAG, "All launch methods failed; falling back to clipboard")
            copyToClipboard(context, normalizedNumber)
            return false
        }

        AutoSelectController.arm(normalizedNumber)
        return true
    }
}
