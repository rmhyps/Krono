package com.krono.app.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krono.app.ui.theme.KronoError
import com.krono.app.ui.theme.KronoOnPrimary
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoSurfaceHigh
import com.krono.core.model.RuleDefinition
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = KronoSurfaceContainer, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.name,
                    color = KronoOnSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                val summaryText = "${rule.branches.size} cabang · ${rule.trigger.family.name.lowercase()}"
                Text(
                    text = summaryText,
                    color = KronoOnSurfaceVariant,
                    fontSize = 13.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Aturan",
                        tint = KronoError.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = isActive,
                    onCheckedChange = onActiveChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = KronoOnPrimary,
                        checkedTrackColor = KronoPrimary,
                        uncheckedThumbColor = KronoOnSurfaceVariant,
                        uncheckedTrackColor = KronoSurfaceHigh
                    )
                )
            }
        }

        if (lastSummary != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Terakhir: ",
                    color = KronoOnSurfaceVariant,
                    fontSize = 12.sp
                )
                StatusBadge(summary = lastSummary)
            }
        }
    }
}
