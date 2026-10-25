package com.krono.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.krono.app.ui.theme.KronoError
import com.krono.app.ui.theme.KronoOnPrimary
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoSurfaceHigh
import com.krono.app.ui.theme.KronoTertiary
import com.krono.app.ui.theme.KronoWarning
import com.krono.app.ui.viewmodel.KronoViewModel
import com.krono.core.evaluator.BluetoothConnectionState
import com.krono.core.evaluator.BranchSelectionResult
import com.krono.core.evaluator.ConditionResult
import com.krono.core.evaluator.EvaluationSnapshot
import com.krono.core.model.BranchKind
import com.krono.core.model.RuleDefinition
import com.krono.core.model.TriggerFamily

@Composable
fun SimulationDialog(
    rule: RuleDefinition,
    viewModel: KronoViewModel,
    onDismiss: () -> Unit
) {
    val simulationResult by viewModel.simulationResult.collectAsState()

    val areaStates = remember(rule) {
        mutableStateMapOf<String, ConditionResult>().apply {
            rule.branches.forEachIndexed { index, b ->
                val id = b.condition.areaId?.ifBlank { "Area ${index + 1}" } ?: "Area ${index + 1}"
                put(id, if (index == 0) ConditionResult.True else ConditionResult.False)
            }
        }
    }

    var bluetoothState by remember(rule) {
        mutableStateOf(BluetoothConnectionState.CONNECTED)
    }

    Dialog(onDismissRequest = {
        viewModel.clearSimulationResult()
        onDismiss()
    }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = KronoSurfaceContainer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Simulasi Logika",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KronoOnSurface
                )
                Text(
                    text = "Periksa keputusan tanpa mengubah pengaturan HP Anda",
                    fontSize = 13.sp,
                    color = KronoOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (rule.trigger.family == TriggerFamily.LOCATION) {
                    rule.branches.forEachIndexed { index, branch ->
                        val areaLabel = branch.condition.areaId?.ifBlank { "Area ${index + 1}" } ?: "Area ${index + 1}"
                        val currentState = areaStates[areaLabel] ?: ConditionResult.False

                        Text(
                            text = "Posisi terhadap $areaLabel:",
                            color = KronoOnSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedButton(
                                onClick = { areaStates[areaLabel] = ConditionResult.True },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (currentState is ConditionResult.True) KronoTertiary.copy(alpha = 0.25f) else KronoSurfaceHigh
                                )
                            ) {
                                Text("Di Dalam", fontSize = 11.sp, maxLines = 1, softWrap = false, color = KronoOnSurface)
                            }

                            OutlinedButton(
                                onClick = { areaStates[areaLabel] = ConditionResult.False },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (currentState is ConditionResult.False) KronoError.copy(alpha = 0.25f) else KronoSurfaceHigh
                                )
                            ) {
                                Text("Di Luar", fontSize = 11.sp, maxLines = 1, softWrap = false, color = KronoOnSurface)
                            }

                            OutlinedButton(
                                onClick = { areaStates[areaLabel] = ConditionResult.Unknown("Sinyal GPS hilang") },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (currentState is ConditionResult.Unknown) KronoWarning.copy(alpha = 0.25f) else KronoSurfaceHigh
                                )
                            ) {
                                Text("Sinyal Hilang", fontSize = 11.sp, maxLines = 1, softWrap = false, color = KronoOnSurface)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                } else {
                    val deviceLabel = rule.trigger.deviceId?.ifBlank { "TWS" } ?: "TWS"
                    Text(
                        text = "Koneksi $deviceLabel:",
                        color = KronoOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = { bluetoothState = BluetoothConnectionState.CONNECTED },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (bluetoothState == BluetoothConnectionState.CONNECTED) KronoTertiary.copy(alpha = 0.25f) else KronoSurfaceHigh
                            )
                        ) {
                            Text("Tersambung", fontSize = 11.sp, maxLines = 1, softWrap = false, color = KronoOnSurface)
                        }

                        OutlinedButton(
                            onClick = { bluetoothState = BluetoothConnectionState.DISCONNECTED },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (bluetoothState == BluetoothConnectionState.DISCONNECTED) KronoError.copy(alpha = 0.25f) else KronoSurfaceHigh
                            )
                        ) {
                            Text("Terputus", fontSize = 11.sp, maxLines = 1, softWrap = false, color = KronoOnSurface)
                        }

                        OutlinedButton(
                            onClick = { bluetoothState = BluetoothConnectionState.UNKNOWN },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (bluetoothState == BluetoothConnectionState.UNKNOWN) KronoWarning.copy(alpha = 0.25f) else KronoSurfaceHigh
                            )
                        ) {
                            Text("Belum Pasti", fontSize = 11.sp, maxLines = 1, softWrap = false, color = KronoOnSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val devId = rule.trigger.deviceId ?: "tws-device"
                        val snapshot = EvaluationSnapshot(
                            areaStates = areaStates.toMap(),
                            bluetoothStates = mapOf(devId to bluetoothState),
                            nowElapsedMs = 1000L,
                            nowUtcMs = 1000L
                        )
                        viewModel.simulateRule(rule, snapshot)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KronoPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Jalankan Evaluasi", color = KronoOnPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                if (simulationResult != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    val (resultTitle, resultDesc, resultColor) = when (val res = simulationResult) {
                        is BranchSelectionResult.Match -> {
                            val branchName = if (res.branch.kind == BranchKind.ELSE) "Cadangan (ELSE)" else res.branch.branchId
                            val count = res.branch.actions.size
                            Triple(
                                "Cabang '$branchName' Terpilih",
                                "$count aksi di cabang ini akan dieksekusi secara berurutan.",
                                KronoTertiary
                            )
                        }
                        is BranchSelectionResult.WaitingData -> {
                            Triple(
                                "Evaluasi Ditahan (Menunggu Data)",
                                "Kondisi belum dapat dipastikan (${res.reason}). Aksi maupun cabang ELSE tidak akan dijalankan.",
                                KronoWarning
                            )
                        }
                        is BranchSelectionResult.NoMatch, null -> {
                            Triple(
                                "Tidak Ada Cabang yang Cocok",
                                "Semua kondisi tidak terpenuhi. Pengaturan HP saat ini akan tetap dipertahankan.",
                                KronoOnSurfaceVariant
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = resultColor.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = resultTitle,
                            color = resultColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = resultDesc,
                            color = KronoOnSurface,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TextButton(
                    onClick = {
                        viewModel.clearSimulationResult()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tutup", color = KronoPrimary)
                }
            }
        }
    }
}
