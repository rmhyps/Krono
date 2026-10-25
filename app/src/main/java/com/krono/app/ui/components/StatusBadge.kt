package com.krono.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krono.app.ui.theme.KronoError
import com.krono.app.ui.theme.KronoSurfaceHigh
import com.krono.app.ui.theme.KronoTertiary
import com.krono.app.ui.theme.KronoWarning
import com.krono.core.result.RuleExecutionSummary

@Composable
fun StatusBadge(
    summary: RuleExecutionSummary,
    modifier: Modifier = Modifier
) {
    val (dotColor, text) = when (summary) {
        RuleExecutionSummary.SUCCESS -> Pair(KronoTertiary, "Berhasil")
        RuleExecutionSummary.PARTIAL -> Pair(KronoWarning, "Sebagian")
        RuleExecutionSummary.UNVERIFIED -> Pair(KronoWarning, "Belum terverifikasi")
        RuleExecutionSummary.FAILED -> Pair(KronoError, "Gagal")
        RuleExecutionSummary.BLOCKED -> Pair(KronoWarning, "Terhalang")
        RuleExecutionSummary.SKIPPED -> Pair(Color.Gray, "Dilewati")
    }

    Box(
        modifier = modifier
            .background(color = KronoSurfaceHigh, shape = RoundedCornerShape(20.dp))
            .border(width = 1.dp, color = dotColor.copy(alpha = 0.25f), shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color = dotColor, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = dotColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
