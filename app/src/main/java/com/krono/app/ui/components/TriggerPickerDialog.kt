package com.krono.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoOutline
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSecondary
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoSurfaceHigh
import com.krono.core.model.TriggerFamily
import com.krono.core.model.TriggerKind

@Composable
fun TriggerPickerDialog(
    onDismiss: () -> Unit,
    onTriggerSelected: (TriggerFamily, TriggerKind, String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = KronoSurfaceContainer),
            border = BorderStroke(1.dp, KronoOutline.copy(alpha = 0.35f)),
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
                    text = "Pilih Pemicu (KETIKA)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KronoOnSurface
                )
                Text(
                    text = "Kejadian yang membangunkan Krono untuk mengecek aturan",
                    fontSize = 13.sp,
                    color = KronoOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "BLUETOOTH & AKSESORI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KronoOnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                TriggerOptionItem(
                    icon = Icons.Default.Share,
                    title = "Bluetooth TWS Tersambung",
                    desc = "Memicu saat earphone atau perangkat Bluetooth terhubung ke HP",
                    enabled = true,
                    onClick = {
                        onTriggerSelected(TriggerFamily.BLUETOOTH, TriggerKind.BLUETOOTH_CONNECTED, "Bluetooth TWS Tersambung")
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                TriggerOptionItem(
                    icon = Icons.Default.Share,
                    title = "Bluetooth TWS Terputus",
                    desc = "Memicu saat earphone Bluetooth terputus dari HP",
                    enabled = true,
                    onClick = {
                        onTriggerSelected(TriggerFamily.BLUETOOTH, TriggerKind.BLUETOOTH_DISCONNECTED, "Bluetooth TWS Terputus")
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "LOKASI & GEOFENCE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KronoOnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                TriggerOptionItem(
                    icon = Icons.Default.LocationOn,
                    title = "Perubahan Area Geofence",
                    desc = "Memicu saat masuk, keluar, atau berpindah antar-area yang dipantau",
                    enabled = true,
                    onClick = {
                        onTriggerSelected(TriggerFamily.LOCATION, TriggerKind.GEOFENCE_MEMBERSHIP_CHANGED, "Perubahan Area Geofence")
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "WI-FI & JARINGAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KronoOnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                TriggerOptionItem(
                    icon = Icons.Default.Share,
                    title = "Terhubung ke Jaringan Wi-Fi",
                    desc = "Memicu saat HP tersambung ke nama Wi-Fi (SSID) tertentu",
                    enabled = true,
                    onClick = {
                        onTriggerSelected(TriggerFamily.WIFI, TriggerKind.WIFI_CONNECTED, "Terhubung ke Wi-Fi")
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                TriggerOptionItem(
                    icon = Icons.Default.Share,
                    title = "Terputus dari Jaringan Wi-Fi",
                    desc = "Memicu saat HP terputus dari jaringan Wi-Fi tertentu",
                    enabled = true,
                    onClick = {
                        onTriggerSelected(TriggerFamily.WIFI, TriggerKind.WIFI_DISCONNECTED, "Terputus dari Wi-Fi")
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SEGERA HADIR (ROADMAP)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KronoOnSurfaceVariant.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                TriggerOptionItem(
                    icon = Icons.Default.PlayArrow,
                    title = "Jadwal Jam Tertentu",
                    desc = "Memicu pada waktu hari dan jam yang ditetapkan",
                    enabled = false,
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(6.dp))
                TriggerOptionItem(
                    icon = Icons.Default.Notifications,
                    title = "Level Baterai / Pengisian",
                    desc = "Memicu saat baterai berada di bawah persentase tertentu",
                    enabled = false,
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal", color = KronoPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun TriggerOptionItem(
    icon: ImageVector,
    title: String,
    desc: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (enabled) KronoSurfaceHigh else KronoSurfaceHigh.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                border = BorderStroke(1.dp, if (enabled) KronoOutline.copy(alpha = 0.25f) else Color.Transparent),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = if (enabled) KronoPrimary.copy(alpha = 0.12f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) KronoPrimary else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = if (enabled) KronoOnSurface else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (!enabled) {
                    Text(
                        text = "Segera",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = if (enabled) KronoOnSurfaceVariant else Color.Gray.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
