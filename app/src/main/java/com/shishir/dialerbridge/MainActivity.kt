package com.shishir.dialerbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shishir.dialerbridge.ui.BridgeViewModel
import com.shishir.dialerbridge.ui.components.PillButton
import com.shishir.dialerbridge.ui.icons.AppIcons
import com.shishir.dialerbridge.ui.screens.DiagnosticsScreen
import com.shishir.dialerbridge.ui.screens.HomeScreen
import com.shishir.dialerbridge.ui.screens.SettingsScreen
import com.shishir.dialerbridge.ui.screens.SetupScreen
import com.shishir.dialerbridge.ui.theme.BridgeTheme

object Routes {
    const val HOME = "home"
    const val SETUP = "setup"
    const val SETTINGS = "settings"
    const val DIAGNOSTICS = "diagnostics"
}

class MainActivity : ComponentActivity() {

    private val viewModel: BridgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BridgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BridgeApp(viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshStatus()
    }
}

@Composable
fun BridgeApp(viewModel: BridgeViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showTabBar = currentRoute == Routes.HOME ||
        currentRoute == Routes.SETUP ||
        currentRoute == Routes.SETTINGS

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        viewModel = viewModel,
                        onOpenSetup = { navController.navigate(Routes.SETUP) },
                        onOpenDiagnostics = { navController.navigate(Routes.DIAGNOSTICS) }
                    )
                }
                composable(Routes.SETUP) {
                    SetupScreen(viewModel = viewModel)
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onOpenDiagnostics = { navController.navigate(Routes.DIAGNOSTICS) }
                    )
                }
                composable(Routes.DIAGNOSTICS) {
                    DiagnosticsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
            }
        }

        AnimatedVisibility(
            visible = showTabBar,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            BridgeTabBar(
                currentRoute = currentRoute ?: Routes.HOME,
                onSelect = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }

    PaidCallWarningDialog(viewModel)
}

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun BridgeTabBar(currentRoute: String, onSelect: (String) -> Unit) {
    val tabs = listOf(
        TabItem(Routes.HOME, "Home", AppIcons.Home),
        TabItem(Routes.SETUP, "Setup", AppIcons.Setup),
        TabItem(Routes.SETTINGS, "Settings", AppIcons.Sliders)
    )
    val barColor = MaterialTheme.colorScheme.background

    Surface(color = barColor) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(48.dp)
            ) {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable { onSelect(tab.route) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(7.dp))
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaidCallWarningDialog(viewModel: BridgeViewModel) {
    val show by viewModel.showPaidWarningDialog.collectAsStateWithLifecycle()
    if (!show) return

    AlertDialog(
        onDismissRequest = { /* must acknowledge */ },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        title = {
            Text(
                text = "Paid Calling Service",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Brilliant-OUT is a paid service by Brilliant Connect for calls to regular " +
                    "phone numbers.\n\nThis bridge automatically opens Brilliant Connect, enters the " +
                    "dialed number, presses call, and selects Brilliant-OUT for you.\n\n" +
                    "Standard charges from your Brilliant Connect balance apply.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            PillButton(
                text = "I Understand",
                onClick = { viewModel.dismissPaidWarning() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    )
}
