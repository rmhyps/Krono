package com.krono.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.krono.app.ui.screens.EditorScreen
import com.krono.app.ui.screens.HistoryScreen
import com.krono.app.ui.screens.RulesScreen
import com.krono.app.ui.screens.SettingsScreen
import com.krono.app.ui.screens.SimulationDialog
import com.krono.app.ui.theme.KronoBackground
import com.krono.app.ui.theme.KronoOnPrimaryContainer
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoPrimaryContainer
import com.krono.app.ui.theme.KronoSurface
import com.krono.app.ui.theme.KronoTheme
import com.krono.app.ui.viewmodel.KronoViewModel
import com.krono.core.model.RuleDefinition

enum class ScreenRoute {
    RULES,
    HISTORY,
    EDITOR,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: KronoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            KronoTheme(themeMode = themeMode) {
                KronoApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun KronoApp(viewModel: KronoViewModel) {
    var currentRoute by remember { mutableStateOf(ScreenRoute.RULES) }
    var editingRule by remember { mutableStateOf<RuleDefinition?>(null) }
    var simulatingRule by remember { mutableStateOf<RuleDefinition?>(null) }

    Scaffold(
        bottomBar = {
            if (currentRoute == ScreenRoute.RULES || currentRoute == ScreenRoute.HISTORY) {
                NavigationBar(
                    containerColor = KronoSurface
                ) {
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.RULES,
                        onClick = { currentRoute = ScreenRoute.RULES },
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Aturan") },
                        label = { Text("Aturan", fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KronoOnPrimaryContainer,
                            selectedTextColor = KronoPrimary,
                            indicatorColor = KronoPrimaryContainer,
                            unselectedIconColor = KronoOnSurfaceVariant,
                            unselectedTextColor = KronoOnSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == ScreenRoute.HISTORY,
                        onClick = { currentRoute = ScreenRoute.HISTORY },
                        icon = { Icon(Icons.Default.Refresh, contentDescription = "Riwayat") },
                        label = { Text("Riwayat", fontSize = 12.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KronoOnPrimaryContainer,
                            selectedTextColor = KronoPrimary,
                            indicatorColor = KronoPrimaryContainer,
                            unselectedIconColor = KronoOnSurfaceVariant,
                            unselectedTextColor = KronoOnSurfaceVariant
                        )
                    )
                }
            }
        },
        containerColor = KronoBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentRoute) {
                ScreenRoute.RULES -> {
                    RulesScreen(
                        viewModel = viewModel,
                        onNavigateToEditor = { rule ->
                            editingRule = rule
                            currentRoute = ScreenRoute.EDITOR
                        },
                        onNavigateToSettings = {
                            currentRoute = ScreenRoute.SETTINGS
                        }
                    )
                }
                ScreenRoute.HISTORY -> {
                    HistoryScreen(viewModel = viewModel)
                }
                ScreenRoute.EDITOR -> {
                    EditorScreen(
                        initialRule = editingRule,
                        viewModel = viewModel,
                        onNavigateBack = {
                            editingRule = null
                            currentRoute = ScreenRoute.RULES
                        },
                        onOpenSimulation = { rule ->
                            simulatingRule = rule
                        }
                    )
                }
                ScreenRoute.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = {
                            currentRoute = ScreenRoute.RULES
                        }
                    )
                }
            }

            simulatingRule?.let { rule ->
                SimulationDialog(
                    rule = rule,
                    viewModel = viewModel,
                    onDismiss = { simulatingRule = null }
                )
            }
        }
    }
}
