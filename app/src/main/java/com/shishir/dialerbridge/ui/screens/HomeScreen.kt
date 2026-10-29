package com.shishir.dialerbridge.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shishir.dialerbridge.handoff.HandoffLauncher
import com.shishir.dialerbridge.telecom.BridgePhoneAccountManager
import com.shishir.dialerbridge.ui.BridgeViewModel
import com.shishir.dialerbridge.ui.components.Chevron
import com.shishir.dialerbridge.ui.components.GroupedCard
import com.shishir.dialerbridge.ui.components.PillButton
import com.shishir.dialerbridge.ui.components.SectionHeader
import com.shishir.dialerbridge.ui.components.SettingsRow
import com.shishir.dialerbridge.ui.components.StatusDot
import com.shishir.dialerbridge.ui.components.StepBadge
import com.shishir.dialerbridge.ui.icons.AppIcons
import com.shishir.dialerbridge.util.SetupStatus

private const val GITHUB_URL = "https://github.com/Shishir-ip"

@Composable
fun HomeScreen(
    viewModel: BridgeViewModel,
    onOpenSetup: () -> Unit,
    onOpenDiagnostics: () -> Unit
) {
    val setupStatus by viewModel.setupStatus.collectAsStateWithLifecycle()
    val version by lazy { viewModel.versionName }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(Modifier.statusBarsPadding().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Dialer Bridge",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(ready = setupStatus.allReady)
                Spacer(Modifier.width(7.dp))
                Text(
                    text = if (setupStatus.allReady) {
                        "Connected to Brilliant Connect"
                    } else {
                        "${setupStatus.remainingCount} setup step${if (setupStatus.remainingCount == 1) "" else "s"} remaining"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        HeroCard(setupStatus = setupStatus, onOpenSetup = onOpenSetup)

        SectionHeader("HOW IT WORKS")
        GroupedCard {
            HowItWorksRow(
                step = 1,
                title = "Dial like you always do",
                subtitle = "Use Google Dialer — nothing changes about your routine."
            )
            CardDivider()
            HowItWorksRow(
                step = 2,
                title = "The bridge takes over",
                subtitle = "Your number is verified and sent to Brilliant Connect."
            )
            CardDivider()
            HowItWorksRow(
                step = 3,
                title = "Brilliant-OUT is selected",
                subtitle = "The call type is chosen automatically. You just talk."
            )
        }

        SectionHeader("QUICK ACTIONS")
        GroupedCard {
            SettingsRow(
                title = "Test the bridge",
                subtitle = "Try a call handoff with any number",
                icon = AppIcons.Bolt,
                onClick = onOpenDiagnostics,
                trailing = { Chevron() }
            )
            CardDivider()
            SettingsRow(
                title = "Calling accounts",
                subtitle = "Enable the bridge in system settings",
                icon = AppIcons.Key,
                onClick = { BridgePhoneAccountManager.openCallingAccountsSettings(context) },
                trailing = { Chevron() }
            )
            CardDivider()
            SettingsRow(
                title = "Open Brilliant Connect",
                subtitle = "Launch the calling app directly",
                icon = AppIcons.Phone,
                onClick = {
                    val launch = context.packageManager.getLaunchIntentForPackage(
                        HandoffLauncher.DEFAULT_TARGET_PACKAGE
                    )
                    if (launch != null) {
                        context.startActivity(launch)
                    } else {
                        HandoffLauncher.openPlayStore(context)
                    }
                },
                trailing = { Chevron() }
            )
        }

        Spacer(Modifier.height(28.dp))
        AboutFooter(version = version, developer = "SHISHIR")
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun HeroCard(setupStatus: SetupStatus, onOpenSetup: () -> Unit) {
    val ready = setupStatus.allReady
    GroupedCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AppIcons.LogoMark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(13.dp))
                Column {
                    Text(
                        text = if (ready) "Bridge Active" else "Setup Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = if (ready) {
                            "Calls will hand off to Brilliant Connect automatically."
                        } else {
                            "Finish the steps below to activate automatic call bridging."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            val progress = (8 - setupStatus.remainingCount) / 8f
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(Modifier.height(14.dp))
            PillButton(
                text = if (ready) "Everything is ready" else "Continue Setup",
                onClick = onOpenSetup,
                enabled = !ready
            )
        }
    }
}

@Composable
private fun HowItWorksRow(step: Int, title: String, subtitle: String) {
    Row(
        Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepBadge(step)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CardDivider() {
    Box(
        Modifier
            .padding(start = 14.dp)
            .fillMaxWidth()
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun AboutFooter(version: String, developer: String) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = AppIcons.LogoMark,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Brilliant Dialer Bridge $version",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Developed by $developer",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "github.com/Shishir-ip",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
            }
        )
    }
}
