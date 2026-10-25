package com.krono.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krono.app.ui.theme.KronoBackground
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoSurface
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan & Kesiapan",
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
                    .padding(16.dp)
            ) {
                ReadinessItem(title = "Wallpaper Manager", desc = "Dukungan layar utama dan layar kunci")
                Spacer(modifier = Modifier.height(12.dp))
                ReadinessItem(title = "Audio Manager", desc = "Pengaturan volume stream musik")
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
