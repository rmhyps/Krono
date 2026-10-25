package com.krono.app.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.krono.app.ui.theme.KronoOnPrimary
import com.krono.app.ui.theme.KronoOnSurface
import com.krono.app.ui.theme.KronoOnSurfaceVariant
import com.krono.app.ui.theme.KronoPrimary
import com.krono.app.ui.theme.KronoSurfaceContainer
import com.krono.app.ui.theme.KronoSurfaceHigh
import com.krono.app.ui.theme.KronoTertiary
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.util.Locale

@Composable
fun MapPickerDialog(
    initialLatitude: Double,
    initialLongitude: Double,
    initialRadiusM: Int,
    onDismiss: () -> Unit,
    onConfirm: (latitude: Double, longitude: Double, radiusM: Int) -> Unit
) {
    val context = LocalContext.current
    var selectedLat by remember { mutableDoubleStateOf(initialLatitude) }
    var selectedLng by remember { mutableDoubleStateOf(initialLongitude) }
    var selectedRadius by remember { mutableIntStateOf(initialRadiusM) }

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(16.0)
            val startPoint = GeoPoint(initialLatitude, initialLongitude)
            controller.setCenter(startPoint)
        }
    }

    val marker = remember {
        Marker(mapView).apply {
            position = GeoPoint(initialLatitude, initialLongitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            isDraggable = true
        }
    }

    val circlePolygon = remember {
        Polygon().apply {
            points = Polygon.pointsAsCircle(GeoPoint(initialLatitude, initialLongitude), initialRadiusM.toDouble())
            fillPaint.color = android.graphics.Color.argb(55, 208, 188, 255)
            outlinePaint.color = android.graphics.Color.argb(220, 208, 188, 255)
            outlinePaint.strokeWidth = 3f
        }
    }

    fun updateOverlays(point: GeoPoint, radiusM: Int) {
        marker.position = point
        circlePolygon.points = Polygon.pointsAsCircle(point, radiusM.toDouble())
        mapView.invalidate()
    }

    DisposableEffect(mapView) {
        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                selectedLat = p.latitude
                selectedLng = p.longitude
                updateOverlays(p, selectedRadius)
                return true
            }

            override fun longPressHelper(p: GeoPoint): Boolean = false
        })

        marker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDrag(m: Marker?) {
                m?.position?.let {
                    selectedLat = it.latitude
                    selectedLng = it.longitude
                    updateOverlays(it, selectedRadius)
                }
            }

            override fun onMarkerDragEnd(m: Marker?) {
                m?.position?.let {
                    selectedLat = it.latitude
                    selectedLng = it.longitude
                    updateOverlays(it, selectedRadius)
                }
            }

            override fun onMarkerDragStart(m: Marker?) {}
        })

        mapView.overlays.add(eventsOverlay)
        mapView.overlays.add(circlePolygon)
        mapView.overlays.add(marker)
        mapView.onResume()

        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KronoSurfaceContainer),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pilih Titik di Peta",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = KronoOnSurface
                        )
                        Text(
                            text = "OpenStreetMap Native",
                            fontSize = 12.sp,
                            color = KronoOnSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = KronoOnSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AndroidView(
                        factory = { mapView },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = KronoSurfaceHigh, shape = RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Koordinat: ${String.format(Locale.US, "%.6f", selectedLat)}, ${String.format(Locale.US, "%.6f", selectedLng)}",
                            color = KronoOnSurface,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Radius: ${selectedRadius}m",
                            color = KronoTertiary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(100, 150, 250, 500).forEach { r ->
                            OutlinedButton(
                                onClick = {
                                    selectedRadius = r
                                    updateOverlays(GeoPoint(selectedLat, selectedLng), r)
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedRadius == r) KronoPrimary.copy(alpha = 0.25f) else KronoSurfaceContainer
                                )
                            ) {
                                Text("${r}m", fontSize = 11.sp, color = KronoOnSurface)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onConfirm(selectedLat, selectedLng, selectedRadius)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KronoPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Gunakan Titik Ini",
                        color = KronoOnPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
