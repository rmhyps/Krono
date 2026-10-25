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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoSurfaceHigh
import com.krono.app.ui.theme.KronoTertiary
import com.krono.core.model.ActionKind

@Composable
fun ActionPickerDialog(
    onDismiss: () -> Unit,
    onActionSelected: (ActionKind) -> Unit
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
                    text = "Pilih Aksi (LAKUKAN)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KronoOnSurface
                )
                Text(
                    text = "Tindakan yang akan dieksekusi di HP Anda",
                    fontSize = 13.sp,
                    color = KronoOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AKSI TERSEDIA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = KronoOnSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                ActionOptionItem(
                    icon = Icons.Default.PlayArrow,
                    title = "Atur Volume Media HP",
                    desc = "Menyesuaikan tingkat suara musik/media (0 - 100%)",
                    enabled = true,
                    onClick = {
                        onActionSelected(ActionKind.SET_MEDIA_VOLUME)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                ActionOptionItem(
                    icon = Icons.Default.Star,
                    title = "Ganti Wallpaper Layar",
                    desc = "Memasang gambar latar ke Layar Utama, Layar Kunci, atau Keduanya",
                    enabled = true,
                    onClick = {
                        onActionSelected(ActionKind.SET_WALLPAPER)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                ActionOptionItem(
                    icon = Icons.Default.Notifications,
                    title = "Ubah Mode Suara HP",
                    desc = "Mengatur profil suara ke Normal, Getar, atau Hening",
                    enabled = true,
                    onClick = {
                        onActionSelected(ActionKind.SET_RINGER_MODE)
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

                ActionOptionItem(
                    icon = Icons.Default.Notifications,
                    title = "Mode Jangan Ganggu (DND)",
                    desc = "Mengaktifkan atau menonaktifkan mode senyap sistem",
                    enabled = false,
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(4.dp))
                ActionOptionItem(
                    icon = Icons.Default.Build,
                    title = "Mode Hemat Daya Sistem",
                    desc = "Mengubah profil efisiensi konsumsi baterai",
                    enabled = false,
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(4.dp))
                ActionOptionItem(
                    icon = Icons.Default.Info,
                    title = "Buka Aplikasi Tertentu",
                    desc = "Meluncurkan aplikasi atau navigasi rute secara otomatis",
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
private fun ActionOptionItem(
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
