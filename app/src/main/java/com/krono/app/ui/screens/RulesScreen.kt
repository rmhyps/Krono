package com.krono.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.krono.app.ui.components.RuleCard
import com.krono.app.ui.theme.KronoBackground
import com.krono.app.ui.theme.KronoError
import com.krono.app.ui.theme.KronoOnPrimary
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoOutline
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSurface
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoWarning
import com.krono.app.ui.viewmodel.KronoViewModel
import com.krono.app.util.BatteryOptimizationHelper
import com.krono.core.model.RuleDefinition
import com.krono.core.model.TriggerFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    viewModel: KronoViewModel,
    onNavigateToEditor: (RuleDefinition?) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val rules by viewModel.rules.collectAsState()
    var ruleToDelete by remember { mutableStateOf<RuleDefinition?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isBatteryExempted by remember {
        mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isBatteryExempted = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var ruleToToggleActive by remember { mutableStateOf<RuleDefinition?>(null) }
    var showLocationPermissionDialog by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val grantedFine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val target = ruleToToggleActive
        ruleToToggleActive = null
        if (grantedFine && target != null) {
            viewModel.toggleRuleActive(target.ruleId, false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Krono",
                        color = KronoOnSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = KronoOnSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KronoSurface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToEditor(null) },
                icon = { Icon(Icons.Default.Add, contentDescription = null, tint = KronoOnPrimary) },
                text = { Text("Buat Aturan", color = KronoOnPrimary, fontWeight = FontWeight.SemiBold) },
                containerColor = KronoPrimary,
                shape = RoundedCornerShape(14.dp)
            )
        },
        containerColor = KronoBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!isBatteryExempted) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = KronoSurfaceContainer
                    ),
                    border = BorderStroke(1.dp, KronoWarning.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mode Layar Kunci (Anti-Tidur)",
                                color = KronoOnSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Izinkan Krono berjalan tanpa batasan baterai agar otomasi lokasi tetap aktif saat layar HP mati/terkunci.",
                                color = KronoOnSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KronoWarning.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Izinkan",
                                color = KronoWarning,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (rules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = KronoSurfaceContainer,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = KronoOutline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = KronoPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum Ada Otomasi",
                            color = KronoOnSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Buat aturan pertama Anda untuk mengotomasi wallpaper, mode suara, atau volume perangkat.",
                            color = KronoOnSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "${rules.size} OTOMASI AKTIF & TERSIMPAN",
                    color = KronoOnSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(rules, key = { it.first.ruleId }) { (rule, isActive) ->
                        RuleCard(
                            rule = rule,
                            isActive = isActive,
                            onActiveChanged = {
                                if (!isActive && rule.trigger.family == TriggerFamily.LOCATION) {
                                    val hasFine = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!hasFine) {
                                        ruleToToggleActive = rule
                                        showLocationPermissionDialog = true
                                        return@RuleCard
                                    }
                                }
                                viewModel.toggleRuleActive(rule.ruleId, isActive)
                            },
                            onClick = { onNavigateToEditor(rule) },
                            onDelete = { ruleToDelete = rule }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            if (showLocationPermissionDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showLocationPermissionDialog = false
                        ruleToToggleActive = null
                    },
                    title = {
                        Text(
                            text = "Izin Lokasi Presisi Diperlukan",
                            color = KronoOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Aturan '${ruleToToggleActive?.name}' memerlukan izin Lokasi Tepat (Presisi) agar Krono dapat memantau batas area dan menjalankan otomasi di latar belakang.",
                            color = KronoOnSurfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showLocationPermissionDialog = false
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KronoPrimary)
                        ) {
                            Text("Berikan Izin", color = KronoOnPrimary, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showLocationPermissionDialog = false
                                ruleToToggleActive = null
                            }
                        ) {
                            Text("Batal", color = KronoPrimary)
                        }
                    },
                    containerColor = KronoSurfaceContainer
                )
            }

            ruleToDelete?.let { rule ->
                AlertDialog(
                    onDismissRequest = { ruleToDelete = null },
                    title = {
                        Text(
                            text = "Hapus Aturan?",
                            color = KronoOnSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Aturan '${rule.name}' akan dihapus dari daftar. Riwayat eksekusi yang sudah terjadi tetap disimpan.",
                            color = KronoOnSurfaceVariant,
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteRule(rule.ruleId)
                                ruleToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KronoError)
                        ) {
                            Text("Hapus", color = KronoOnSurface, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { ruleToDelete = null }) {
                            Text("Batal", color = KronoPrimary)
                        }
                    },
                    containerColor = KronoSurfaceContainer
                )
            }
        }
    }
}
