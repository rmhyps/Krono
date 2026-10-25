package com.krono.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krono.app.ui.theme.KronoError
import com.krono.app.ui.theme.KronoOnPrimaryContainer
import com.krono.app.ui.theme.KronoPrimaryContainer
import com.krono.app.ui.theme.KronoTertiary
import com.krono.app.ui.theme.KronoWarning
import com.krono.core.result.RuleExecutionSummary

@Composable
fun StatusBadge(
    summary: RuleExecutionSummary,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when (summary) {
        RuleExecutionSummary.SUCCESS -> Triple(KronoTertiary.copy(alpha = 0.2f), KronoTertiary, "Berhasil")
        RuleExecutionSummary.PARTIAL -> Triple(KronoWarning.copy(alpha = 0.2f), KronoWarning, "Sebagian berhasil")
        RuleExecutionSummary.UNVERIFIED -> Triple(KronoWarning.copy(alpha = 0.2f), KronoWarning, "Belum terverifikasi")
        RuleExecutionSummary.FAILED -> Triple(KronoError.copy(alpha = 0.2f), KronoError, "Gagal")
        RuleExecutionSummary.BLOCKED -> Triple(KronoWarning.copy(alpha = 0.2f), KronoWarning, "Terhalang")
        RuleExecutionSummary.SKIPPED -> Triple(Color.Gray.copy(alpha = 0.2f), Color.LightGray, "Dilewati")
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
    }
}
