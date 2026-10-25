package com.krono.app.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.krono.app.ui.theme.KronoOnPrimary
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoOutline
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSecondary
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoSurfaceHigh

data class PairedDeviceInfo(
    val name: String,
    val address: String
)

@SuppressLint("MissingPermission")
@Composable
fun BluetoothDevicePickerDialog(
    onDismiss: () -> Unit,
    onDeviceSelected: (deviceName: String, deviceAddress: String) -> Unit
) {
    val context = LocalContext.current
    val bluetoothManager = remember {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    val pairedDevices = remember { mutableStateListOf<PairedDeviceInfo>() }
    var hasPermission by remember { mutableStateOf(false) }
    var isManualInput by remember { mutableStateOf(false) }
    var manualAddress by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            refreshPairedDevices(bluetoothAdapter, pairedDevices)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val check = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            if (check == PackageManager.PERMISSION_GRANTED) {
                hasPermission = true
                refreshPairedDevices(bluetoothAdapter, pairedDevices)
            } else {
                permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            hasPermission = true
            refreshPairedDevices(bluetoothAdapter, pairedDevices)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = KronoSurfaceContainer),
            border = BorderStroke(1.dp, KronoOutline.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pilih Perangkat Bluetooth",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = KronoOnSurface
                        )
                        Text(
                            text = "Daftar perangkat yang sudah dipasangkan",
                            fontSize = 12.sp,
                            color = KronoOnSurfaceVariant
                        )
                    }
                    IconButton(onClick = { refreshPairedDevices(bluetoothAdapter, pairedDevices) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang", tint = KronoSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isManualInput) {
                    OutlinedTextField(
                        value = manualAddress,
                        onValueChange = { manualAddress = it },
                        label = { Text("Alamat MAC / Device ID") },
                        placeholder = { Text("Contoh: 00:11:22:33:44:55") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KronoSecondary,
                            unfocusedBorderColor = KronoOutline,
                            focusedTextColor = KronoOnSurface,
                            unfocusedTextColor = KronoOnSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (manualAddress.isNotBlank()) {
                                onDeviceSelected(manualAddress, manualAddress)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = KronoPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Gunakan ID Ini", color = KronoOnPrimary, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { isManualInput = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kembali ke Daftar Perangkat", color = KronoPrimary)
                    }
                } else {
                    if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = KronoSurfaceHigh, shape = RoundedCornerShape(10.dp))
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Bluetooth HP belum aktif",
                                color = KronoOnSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KronoSecondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Buka Pengaturan Bluetooth", color = KronoOnPrimary, fontSize = 12.sp)
                            }
                        }
                    } else if (pairedDevices.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = KronoSurfaceHigh, shape = RoundedCornerShape(10.dp))
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum ada perangkat yang terpasang (paired)",
                                color = KronoOnSurfaceVariant,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KronoSecondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Pasangkan di Pengaturan HP", color = KronoOnPrimary, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            items(pairedDevices, key = { it.address }) { device ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color = KronoSurfaceHigh, shape = RoundedCornerShape(8.dp))
                                        .clickable {
                                            onDeviceSelected(device.name, device.address)
                                            onDismiss()
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = KronoSecondary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = device.name,
                                            color = KronoOnSurface,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = device.address,
                                            color = KronoOnSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { isManualInput = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ketik Alamat MAC Manual", color = KronoOnSurfaceVariant, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal", color = KronoPrimary)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun refreshPairedDevices(
    adapter: BluetoothAdapter?,
    list: MutableList<PairedDeviceInfo>
) {
    list.clear()
    if (adapter == null || !adapter.isEnabled) return
    try {
        val bonded: Set<BluetoothDevice>? = adapter.bondedDevices
        bonded?.forEach { dev ->
            val name = dev.name ?: "Perangkat Tanpa Nama"
            val address = dev.address ?: ""
            if (address.isNotEmpty()) {
                list.add(PairedDeviceInfo(name, address))
            }
        }
    } catch (_: SecurityException) {}
}
