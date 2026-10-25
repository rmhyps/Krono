package com.krono.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.krono.app.ui.components.StatusBadge
import com.krono.app.ui.theme.KronoBackground
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSurface
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.viewmodel.KronoViewModel
import com.krono.core.result.RuleExecutionSummary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: KronoViewModel
) {
    val history by viewModel.history.collectAsState()
    val dateFormat = SimpleDateFormat("HH:mm:ss · dd MMM", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Riwayat Eksekusi",
                        color = KronoOnSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (history.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Bersihkan", color = KronoPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KronoSurface)
            )
        },
        containerColor = KronoBackground
    ) { padding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada riwayat eksekusi",
                    color = KronoOnSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(history, key = { it.historyId }) { item ->
                    val summary = try {
                        RuleExecutionSummary.valueOf(item.summaryResult)
                    } catch (e: Exception) {
                        RuleExecutionSummary.SKIPPED
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = KronoSurfaceContainer, shape = RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.ruleNameSnapshot,
                                color = KronoOnSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            StatusBadge(summary = summary)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.branchLabelSnapshot ?: item.originKind,
                            color = KronoOnSurfaceVariant,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = dateFormat.format(Date(item.finishedAtUtcMs)),
                            color = KronoOnSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
