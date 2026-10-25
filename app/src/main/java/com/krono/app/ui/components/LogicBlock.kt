package com.krono.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOutline
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSecondary
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoSurfaceHigh
import com.krono.app.ui.theme.KronoTertiary

@Composable
fun LogicBlock(
    tag: String,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val tagAccent = KronoPrimary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = KronoSurfaceContainer, shape = RoundedCornerShape(16.dp))
            .border(
                border = BorderStroke(1.dp, KronoOutline.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(color = KronoSurfaceHigh, shape = RoundedCornerShape(6.dp))
                    .border(1.dp, tagAccent.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = tag,
                    color = tagAccent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = title,
                color = KronoOnSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        content()
    }
}
