package com.shishir.dialerbridge.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shishir.dialerbridge.handoff.HandoffLauncher
import com.shishir.dialerbridge.telecom.BridgePhoneAccountManager
import com.shishir.dialerbridge.ui.BridgeViewModel
import com.shishir.dialerbridge.ui.components.GroupedCard
import com.shishir.dialerbridge.ui.components.SectionHeader
import com.shishir.dialerbridge.ui.icons.AppIcons

private data class SetupRow(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val done: Boolean
)

@Composable
fun SetupScreen(viewModel: BridgeViewModel) {
    val setupStatus by viewModel.setupStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val phonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { viewModel.refreshStatus() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshStatus() }

    fun launchOverlaySettings() {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun launchFullScreenIntentSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Intent(
                Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                Uri.parse("package:${context.packageName}")
            )
        } else {
            Intent("android.settings.NOTIFICATION_SETTINGS")
        }
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun launchBatterySettings() {
        try {
            @Suppress("BatteryLife")
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun launchAccessibilitySettings() {
        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    val rows = listOf(
        SetupRow(
            icon = AppIcons.Shield,
            title = "Phone permissions",
            subtitle = "Read phone state and place calls",
            done = setupStatus.phonePermissionsGranted
        ),
        SetupRow(
            icon = AppIcons.Bell,
            title = "Notifications",
            subtitle = "Show handoff and confirmation alerts",
            done = setupStatus.notificationPermissionGranted
        ),
        SetupRow(
            icon = AppIcons.Overlay,
            title = "Display over other apps",
            subtitle = "Needed to auto-select Brilliant-OUT",
            done = setupStatus.overlayPermissionGranted
        ),
        SetupRow(
            icon = AppIcons.Bolt,
            title = "Full-screen notifications",
            subtitle = "Show handoff instantly, even on lock screen",
            done = setupStatus.fullScreenIntentGranted
        ),
        SetupRow(
            icon = AppIcons.Battery,
            title = "Battery optimization",
            subtitle = "Let the bridge run without restrictions",
            done = setupStatus.batteryOptimizationIgnored
        ),
        SetupRow(
            icon = AppIcons.AppGrid,
            title = "Brilliant Connect installed",
            subtitle = "The calling app that receives your calls",
            done = setupStatus.targetAppInstalled
        ),
        SetupRow(
            icon = AppIcons.Person,
            title = "Accessibility service",
            subtitle = "Auto-selects Brilliant-OUT in Brilliant Connect",
            done = setupStatus.accessibilityServiceEnabled
        ),
        SetupRow(
            icon = AppIcons.Key,
            title = "Calling account enabled",
            subtitle = "Turn on “Brilliant Connect” under calling accounts",
            done = setupStatus.callingAccountEnabled
        )
    )

    val doneCount = rows.count { it.done }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(Modifier.statusBarsPadding().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Setup",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$doneCount of ${rows.size} complete",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionHeader("REQUIRED STEPS")
        GroupedCard {
            rows.forEachIndexed { index, row ->
                SetupItemRow(row = row) {
                    when (index) {
                        0 -> phonePermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.CALL_PHONE
                            )
                        )
                        1 -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.refreshStatus()
                            }
                        }
                        2 -> launchOverlaySettings()
                        3 -> launchFullScreenIntentSettings()
                        4 -> launchBatterySettings()
                        5 -> HandoffLauncher.openPlayStore(context)
                        6 -> launchAccessibilitySettings()
                        7 -> BridgePhoneAccountManager.openCallingAccountsSettings(context)
                    }
                }
                if (index < rows.lastIndex) {
                    Box(
                        Modifier
                            .padding(start = 55.dp)
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }

        SectionHeader("NOTE")
        GroupedCard {
            Text(
                text = "Emergency numbers (999, 112, 911 …) and USSD codes always use your SIM " +
                    "carrier directly — the bridge never intercepts them.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SetupItemRow(row: SetupRow, onFix: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onFix)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = row.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(Modifier.size(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = row.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = row.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.size(10.dp))
        if (row.done) {
            Icon(
                imageVector = AppIcons.CheckCircle,
                contentDescription = "Done",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        } else {
            FixBadge(onClick = onFix)
        }
    }
}

@Composable
private fun FixBadge(onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = "Fix",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}
