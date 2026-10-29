package com.shishir.dialerbridge.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shishir.dialerbridge.autoselect.AutoSelectController
import com.shishir.dialerbridge.ui.BridgeViewModel
import com.shishir.dialerbridge.ui.components.GroupedCard
import com.shishir.dialerbridge.ui.components.PillButton
import com.shishir.dialerbridge.ui.components.SectionHeader
import com.shishir.dialerbridge.ui.components.SettingsRow
import com.shishir.dialerbridge.ui.icons.AppIcons

@Composable
fun DiagnosticsScreen(viewModel: BridgeViewModel, onBack: () -> Unit) {
    val defaultDialer by viewModel.defaultDialerPkg.collectAsStateWithLifecycle()
    val callingAccounts by viewModel.callingAccountsList.collectAsStateWithLifecycle()
    val autoState by viewModel.autoSelectState.collectAsStateWithLifecycle()
    val autoResult by viewModel.autoSelectResult.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val testLogs by viewModel.testLogEntries.collectAsStateWithLifecycle()
    val testRunning by viewModel.testRunning.collectAsStateWithLifecycle()
    val setupStatus by viewModel.setupStatus.collectAsStateWithLifecycle()

    var testNumber by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) listState.animateScrollToItem(logs.lastIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            Modifier.statusBarsPadding().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = AppIcons.ChevronLeft,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(26.dp)
                    .clickable(onClick = onBack)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Diagnostics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        SectionHeader("SYSTEM STATE")
        GroupedCard {
            SettingsRow(
                title = "Accessibility service",
                icon = AppIcons.Person,
                trailing = {
                    Text(
                        text = if (setupStatus.accessibilityServiceEnabled) "Connected" else "Off",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            CardDivider()
            SettingsRow(
                title = "Default dialer",
                icon = AppIcons.Phone,
                trailing = {
                    Text(
                        text = defaultDialer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            CardDivider()
            SettingsRow(
                title = "Auto-select state",
                subtitle = autoResult,
                icon = AppIcons.Bolt,
                trailing = {
                    Text(
                        text = autoState.name.lowercase().replace('_', ' '),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            if (callingAccounts.isNotEmpty()) {
                CardDivider()
                SettingsRow(
                    title = "Calling accounts",
                    subtitle = callingAccounts.joinToString("\n"),
                    icon = AppIcons.Key
                )
            }
        }

        SectionHeader("TEST HANDOFF")
        GroupedCard {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = testNumber,
                    onValueChange = { testNumber = it.filter { ch -> ch.isDigit() || ch == '+' || ch == '#' || ch == '*' } },
                    singleLine = true,
                    placeholder = {
                        Text(
                            "Enter a number to test",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                PillButton(
                    text = if (testRunning) "Testing…" else "Start Handoff Test",
                    enabled = !testRunning && testNumber.isNotBlank(),
                    onClick = { viewModel.runTestHandoff(testNumber) }
                )
                if (testLogs.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        testLogs.forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        SectionHeader("BRIDGE LOGS")
        GroupedCard {
            Row(
                Modifier.padding(start = 14.dp, end = 8.dp, top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent activity (masked)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = AppIcons.Copy,
                    contentDescription = "Copy logs",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { viewModel.copyLogsToClipboard() }
                )
                Spacer(Modifier.width(14.dp))
                Icon(
                    imageVector = AppIcons.Trash,
                    contentDescription = "Clear logs",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { viewModel.clearLogs() }
                )
                Spacer(Modifier.width(6.dp))
            }
            if (logs.isEmpty()) {
                Text(
                    text = "No activity yet. Make a call to see the bridge in action.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(logs) { entry ->
                        Text(
                            text = entry.formatDisplay(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CardDivider() {
    Box(
        Modifier
            .padding(start = 55.dp)
            .fillMaxWidth()
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
