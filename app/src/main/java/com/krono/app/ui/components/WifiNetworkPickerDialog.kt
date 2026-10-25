package com.krono.app.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.Color
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
import com.krono.app.ui.theme.KronoTertiary
import com.krono.platform.wifi.WifiConnectionReceiver

@SuppressLint("MissingPermission")
@Composable
fun WifiNetworkPickerDialog(
    onDismiss: () -> Unit,
    onWifiSelected: (ssid: String) -> Unit
) {
    val context = LocalContext.current
    val wifiManager = remember {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    }

    var currentConnectedSsid by remember { mutableStateOf<String?>(null) }
    val scannedNetworks = remember { mutableStateListOf<String>() }
    var isManualInput by remember { mutableStateOf(false) }
    var manualSsid by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }

    fun refreshNetworks() {
        isScanning = true
        val (isConnected, activeSsid) = WifiConnectionReceiver.getWifiState(context)
        currentConnectedSsid = if (isConnected && !activeSsid.isNullOrBlank() && activeSsid != "<unknown ssid>") {
            activeSsid
        } else null

        scannedNetworks.clear()
        try {
            val results = wifiManager?.scanResults ?: emptyList()
            val list = results
                .mapNotNull { it.SSID?.removeSurrounding("\"")?.trim() }
                .filter { it.isNotBlank() && it != "<unknown ssid>" && it != currentConnectedSsid }
                .distinct()
                .sorted()
            scannedNetworks.addAll(list)
        } catch (_: Exception) {}
        isScanning = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshNetworks()
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            refreshNetworks()
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
                            text = "Pilih Jaringan Wi-Fi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = KronoOnSurface
                        )
                        Text(
                            text = "Pilih Wi-Fi yang akan dijadikan pemicu",
                            fontSize = 12.sp,
                            color = KronoOnSurfaceVariant
                        )
                    }

                    IconButton(onClick = { refreshNetworks() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Pindai Ulang",
                            tint = KronoSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isManualInput) {
                    Text(
                        text = "Ketik Nama Wi-Fi (SSID)",
                        color = KronoOnSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = manualSsid,
                        onValueChange = { manualSsid = it },
                        placeholder = { Text("Contoh: Kampus_UIN, Kost_12") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KronoSecondary,
                            unfocusedBorderColor = KronoOutline,
                            focusedTextColor = KronoOnSurface,
                            unfocusedTextColor = KronoOnSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isManualInput = false }) {
                            Text("Pilih dari Daftar", color = KronoOnSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (manualSsid.isNotBlank()) {
                                    onWifiSelected(manualSsid.trim())
                                    onDismiss()
                                }
                            },
                            enabled = manualSsid.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = KronoSecondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Pilih", color = KronoOnPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        currentConnectedSsid?.let { activeSsid ->
                            item {
                                Text(
                                    text = "SEDANG TERHUBUNG",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KronoPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = KronoPrimary.copy(alpha = 0.15f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onWifiSelected(activeSsid)
                                            onDismiss()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(KronoPrimary, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = activeSsid,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = KronoOnSurface
                                            )
                                            Text(
                                                text = "Wi-Fi aktif saat ini",
                                                fontSize = 11.sp,
                                                color = KronoPrimary
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = KronoPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        item {
                            Text(
                                text = "JARINGAN TERDETEKSI DI SEKITAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KronoOnSurfaceVariant,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        if (scannedNetworks.isEmpty() && currentConnectedSsid == null) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isScanning) "Memindai sinyal Wi-Fi..." else "Tidak ada jaringan Wi-Fi terdeteksi",
                                        color = KronoOnSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            items(scannedNetworks, key = { it }) { ssid ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = KronoSurfaceHigh),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable {
                                            onWifiSelected(ssid)
                                            onDismiss()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ssid,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = KronoOnSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "Pilih",
                                            fontSize = 11.sp,
                                            color = KronoSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { isManualInput = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Input Manual", color = KronoSecondary, fontSize = 12.sp)
                        }

                        TextButton(onClick = onDismiss) {
                            Text("Batal", color = KronoOnSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
