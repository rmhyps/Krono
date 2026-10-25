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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krono.app.ui.theme.KronoError
import com.krono.app.ui.theme.KronoOnPrimary
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoOutline
import com.krono.app.ui.theme.KronoOutlineVariant
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSecondary
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoSurfaceHigh
import com.krono.core.model.ActionKind
import com.krono.core.model.RingerModeTarget
import com.krono.core.model.RuleDefinition
import com.krono.core.model.TriggerFamily
import com.krono.core.model.WallpaperTarget
import com.krono.core.result.RuleExecutionSummary

@Composable
fun RuleCard(
    rule: RuleDefinition,
    isActive: Boolean,
    onActiveChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    lastSummary: RuleExecutionSummary? = null,
    modifier: Modifier = Modifier
) {
    val triggerIcon = when (rule.trigger.family) {
        TriggerFamily.LOCATION -> Icons.Default.LocationOn
        TriggerFamily.BLUETOOTH -> Icons.Default.Share
        TriggerFamily.WIFI -> Icons.Default.Share
    }

    val triggerText = when (rule.trigger.family) {
        TriggerFamily.LOCATION -> {
            val area = rule.trigger.areaIds.firstOrNull() ?: "Area"
            "Lokasi: $area"
        }
        TriggerFamily.BLUETOOTH -> {
            val dev = rule.trigger.deviceId?.ifBlank { "Bluetooth" } ?: "Bluetooth"
            "Bluetooth: $dev"
        }
        TriggerFamily.WIFI -> {
            val ssid = rule.trigger.wifiSsid?.ifBlank { "Jaringan Wi-Fi" } ?: "Jaringan Wi-Fi"
            "Wi-Fi: $ssid"
        }
    }

    val actionText = run {
        val actions = rule.branches.flatMap { it.actions }
        if (actions.isEmpty()) {
            "Belum ada aksi"
        } else {
            actions.joinToString(" • ") { a ->
                when (a.kind) {
                    ActionKind.SET_WALLPAPER -> {
                        val tgt = when (a.wallpaperTarget) {
                            WallpaperTarget.SYSTEM -> "Layar Utama"
                            WallpaperTarget.LOCK -> "Layar Kunci"
                            WallpaperTarget.BOTH, null -> "Semua Layar"
                        }
                        "Ganti Wallpaper ($tgt)"
                    }
                    ActionKind.SET_MEDIA_VOLUME -> "Volume ${a.volumePercent ?: 20}%"
                    ActionKind.SET_RINGER_MODE -> "Mode ${
                        when (a.ringerMode) {
                            RingerModeTarget.NORMAL -> "Normal"
                            RingerModeTarget.VIBRATE -> "Getar"
                            RingerModeTarget.SILENT -> "Hening"
                            null -> "Suara"
                        }
                    }"
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KronoSurfaceContainer),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) KronoOutline.copy(alpha = 0.5f) else KronoOutlineVariant.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = if (isActive) KronoPrimary.copy(alpha = 0.15f) else KronoSurfaceHigh,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive) KronoPrimary.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = triggerIcon,
                            contentDescription = null,
                            tint = if (isActive) KronoPrimary else KronoOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = rule.name,
                            color = KronoOnSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = triggerText,
                            color = if (isActive) KronoSecondary else KronoOnSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Aturan",
                            tint = KronoOnSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Switch(
                        checked = isActive,
                        onCheckedChange = onActiveChanged,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = KronoOnPrimary,
                            checkedTrackColor = KronoPrimary,
                            uncheckedThumbColor = KronoOnSurfaceVariant,
                            uncheckedTrackColor = KronoSurfaceHigh,
                            uncheckedBorderColor = KronoOutlineVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action summary strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = KronoSurfaceHigh.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aksi: $actionText",
                        color = KronoOnSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    if (lastSummary != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(summary = lastSummary)
                    }
                }
            }
        }
    }
}
