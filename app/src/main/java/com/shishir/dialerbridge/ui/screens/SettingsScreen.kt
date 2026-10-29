package com.shishir.dialerbridge.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shishir.dialerbridge.ui.BridgeViewModel
import com.shishir.dialerbridge.ui.components.Chevron
import com.shishir.dialerbridge.ui.components.GroupedCard
import com.shishir.dialerbridge.ui.components.IosSwitch
import com.shishir.dialerbridge.ui.components.SectionHeader
import com.shishir.dialerbridge.ui.components.SegmentedControl
import com.shishir.dialerbridge.ui.components.SettingsRow
import com.shishir.dialerbridge.ui.icons.AppIcons

private const val DEVELOPER_URL = "https://github.com/Shishir-ip"

@Composable
fun SettingsScreen(viewModel: BridgeViewModel, onOpenDiagnostics: () -> Unit) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val redirectionHeld by viewModel.redirectionRoleHeld.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var labelDraft by remember(settings.targetOptionLabel) { mutableStateOf(settings.targetOptionLabel) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(Modifier.statusBarsPadding().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        SectionHeader("AUTOMATION")
        GroupedCard {
            SettingsRow(
                title = "Auto-select Brilliant-OUT",
                subtitle = "Pick the Brilliant-OUT call type automatically",
                icon = AppIcons.Bolt,
                divider = true,
                trailing = {
                    IosSwitch(settings.autoSelectBrilliantOut) { viewModel.updateAutoSelect(it) }
                }
            )
            SettingsRow(
                title = "Auto-press call button",
                subtitle = "Press the call button in Brilliant Connect for you",
                icon = AppIcons.Phone,
                divider = true,
                trailing = {
                    IosSwitch(settings.autoPressCallButton) { viewModel.updateAutoPressCallButton(it) }
                }
            )
            SettingsRow(
                title = "Confirm before calling",
                subtitle = "Ask before every Brilliant-OUT call",
                icon = AppIcons.Shield,
                divider = true,
                trailing = {
                    IosSwitch(settings.confirmBeforeCalling) { viewModel.updateConfirmBeforeCall(it) }
                }
            )
            SettingsRow(
                title = "Route all outgoing calls",
                subtitle = "Redirect every dialer call — even without a calling account",
                icon = AppIcons.Handoff,
                divider = true,
                trailing = {
                    IosSwitch(settings.routeAllOutgoingCalls) { viewModel.updateRouteAllCalls(it) }
                }
            )
            SettingsRow(
                title = "Default redirection app",
                subtitle = if (redirectionHeld) {
                    "Brilliant Dialer Bridge is active"
                } else {
                    "Select this bridge to route all calls"
                },
                icon = AppIcons.Key,
                divider = true,
                trailing = {
                    if (!redirectionHeld && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        FixLink("Set") {
                            try {
                                val rm = context.getSystemService(android.app.role.RoleManager::class.java)
                                if (rm != null && rm.isRoleAvailable(android.app.role.RoleManager.ROLE_CALL_REDIRECTION)) {
                                    context.startActivity(
                                        rm.createRequestRoleIntent(
                                            android.app.role.RoleManager.ROLE_CALL_REDIRECTION
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                            }
                        }
                    } else {
                        Text(
                            text = if (redirectionHeld) "Active" else "—",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            SettingsRow(
                title = "Start on boot",
                subtitle = "Re-register the calling account after restart",
                icon = AppIcons.Battery,
                trailing = {
                    IosSwitch(settings.startOnBoot) { viewModel.updateStartOnBoot(it) }
                }
            )
        }

        SectionHeader("CALL FORMATTING")
        GroupedCard {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    text = "Number format",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(9.dp))
                SegmentedControl(
                    options = listOf("As dialed", "+E.164", "Local"),
                    selectedIndex = when (settings.numberFormat) {
                        "e164" -> 1
                        "local" -> 2
                        else -> 0
                    },
                    onSelect = { index ->
                        viewModel.updateNumberFormat(
                            when (index) {
                                1 -> "e164"
                                2 -> "local"
                                else -> "as_dialed"
                            }
                        )
                    }
                )
            }
            CardDividerSettings()
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    text = "Launch method",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(9.dp))
                SegmentedControl(
                    options = listOf("Direct", "View", "Clipboard"),
                    selectedIndex = when (settings.handoffStrategy) {
                        "view_intent" -> 1
                        "launcher_copy" -> 2
                        else -> 0
                    },
                    onSelect = { index ->
                        viewModel.updateHandoffStrategy(
                            when (index) {
                                1 -> "view_intent"
                                2 -> "launcher_copy"
                                else -> "dial_intent"
                            }
                        )
                    }
                )
            }
            CardDividerSettings()
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    text = "Call type label",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "The row name in the Brilliant Connect call-type sheet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(9.dp))
                OutlinedTextField(
                    value = labelDraft,
                    onValueChange = {
                        labelDraft = it
                        viewModel.updateTargetLabel(it)
                    },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        SectionHeader("ADVANCED")
        GroupedCard {
            SettingsRow(
                title = "Diagnostics & logs",
                subtitle = "Test the bridge, view system state and logs",
                icon = AppIcons.DocLogs,
                onClick = onOpenDiagnostics,
                trailing = { Chevron() }
            )
        }

        SectionHeader("ABOUT")
        GroupedCard {
            SettingsRow(
                title = "Version",
                icon = AppIcons.Info,
                trailing = {
                    Text(
                        text = viewModel.versionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            CardDividerSettings()
            SettingsRow(
                title = "Developer",
                icon = AppIcons.Person,
                trailing = {
                    Text(
                        text = "SHISHIR",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            CardDividerSettings()
            SettingsRow(
                title = "GitHub",
                subtitle = "github.com/Shishir-ip",
                icon = AppIcons.Link,
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DEVELOPER_URL)))
                },
                trailing = { Chevron() }
            )
            CardDividerSettings()
            SettingsRow(
                title = "License",
                icon = AppIcons.DocLogs,
                trailing = {
                    Text(
                        text = "MIT",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CardDividerSettings() {
    Box(
        Modifier
            .padding(start = 55.dp)
            .fillMaxWidth()
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun FixLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
    )
}
