package com.krono.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krono.app.ui.theme.KronoBackground
import com.krono.app.ui.theme.KronoOnPrimary
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoOutline
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSurface
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoTertiary
import com.krono.app.ui.viewmodel.KronoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: KronoViewModel,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan",
                        color = KronoOnSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = KronoOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KronoSurface)
            )
        },
        containerColor = KronoBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Tema Tampilan",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = KronoOnSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            val currentTheme by viewModel.themeMode.collectAsState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KronoSurfaceContainer, RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, KronoOutline.copy(alpha = 0.35f)), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "SYSTEM" to "Sistem",
                    "LIGHT" to "Terang",
                    "DARK" to "Gelap"
                ).forEach { (mode, label) ->
                    val isSelected = currentTheme.equals(mode, ignoreCase = true) || (mode == "DARK" && currentTheme.equals("KRONO_DARK", ignoreCase = true))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) KronoPrimary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setThemeMode(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false,
                            color = if (isSelected) KronoOnPrimary else KronoOnSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Kesiapan Layanan & Platform",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = KronoOnSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = KronoSurfaceContainer, shape = RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, KronoOutline.copy(alpha = 0.35f)), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                ReadinessItem(title = "Wallpaper Manager", desc = "Dukungan layar utama dan layar kunci")
                Spacer(modifier = Modifier.height(12.dp))
                ReadinessItem(title = "Audio Manager", desc = "Pengaturan volume stream musik & profil suara")
                Spacer(modifier = Modifier.height(12.dp))
                ReadinessItem(title = "Penyimpanan Lokal", desc = "Room 3 database privat & TRUNCATE journal")
                Spacer(modifier = Modifier.height(12.dp))
                ReadinessItem(title = "Mesin Aturan", desc = "Evaluator logika 3 nilai & whole-branch conflict")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Retensi Data Riwayat",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = KronoOnSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = KronoSurfaceContainer, shape = RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, KronoOutline.copy(alpha = 0.35f)), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Batas Default: 30 Hari / 10.000 Catatan",
                    color = KronoOnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Riwayat yang lebih tua dari batas akan dibersihkan secara otomatis tanpa memengaruhi konfigurasi aturan.",
                    color = KronoOnSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tentang Krono",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = KronoOnSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = KronoSurfaceContainer, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Krono Android v0.1.0",
                    color = KronoOnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aplikasi otomasi pribadi offline. Memproses aturan dan aksi secara aman langsung di perangkat.",
                    color = KronoOnSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ReadinessItem(title: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KronoTertiary)
        Spacer(modifier = Modifier.padding(horizontal = 6.dp))
        Column {
            Text(text = title, color = KronoOnSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = desc, color = KronoOnSurfaceVariant, fontSize = 12.sp)
        }
    }
}
