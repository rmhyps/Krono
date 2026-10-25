package com.krono.platform.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.krono.platform.diagnostic.KronoDiagLog

class KronoLocationMonitor(private val context: Context) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private var isMonitoring = false
    private val monitoredAreas = mutableListOf<GeofenceArea>()
    private val areaInsideState = mutableMapOf<String, Boolean>()
    private val lastEvalLogTime = mutableMapOf<String, Long>()

    var onTransitionListener: ((areaId: String, isInside: Boolean, elapsedMs: Long, utcMs: Long) -> Unit)? = null

    private val fusedCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            evaluateLocation(location)
        }
    }

    private val nativeLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            evaluateLocation(location)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun updateMonitoredAreas(areas: List<GeofenceArea>) {
        synchronized(monitoredAreas) {
            monitoredAreas.clear()
            monitoredAreas.addAll(areas)
        }
        if (areas.isNotEmpty() && !isMonitoring) {
            startMonitoring()
        } else if (areas.isEmpty() && isMonitoring) {
            stopMonitoring()
        }
    }

    @SuppressLint("MissingPermission")
    fun startMonitoring() {
        if (isMonitoring) {
            KronoDiagLog.d("KRONO_DIAG", "[LOCATION_MONITOR] startMonitoring() skipped: already monitoring")
            return
        }
        KronoDiagLog.d("KRONO_DIAG", "[LOCATION_MONITOR] startMonitoring() initiated for ${monitoredAreas.size} areas")
        try {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(0f)
                .build()

            fusedClient.requestLocationUpdates(request, fusedCallback, Looper.getMainLooper())
            KronoDiagLog.d("KRONO_DIAG", "[LOCATION_MONITOR] FusedLocationProviderClient registered")

            locationManager?.let { lm ->
                try {
                    val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    KronoDiagLog.d("KRONO_DIAG", "[LOCATION_MONITOR] GPS_PROVIDER enabled=$gpsEnabled")
                    if (gpsEnabled) {
                        lm.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            1000L,
                            0f,
                            nativeLocationListener,
                            Looper.getMainLooper()
                        )
                    }
                } catch (e: Exception) {
                    KronoDiagLog.w("KRONO_DIAG", "[LOCATION_MONITOR] GPS_PROVIDER registration failed: ${e.message}")
                }

                try {
                    val networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    KronoDiagLog.d("KRONO_DIAG", "[LOCATION_MONITOR] NETWORK_PROVIDER enabled=$networkEnabled")
                    if (networkEnabled) {
                        lm.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            1000L,
                            0f,
                            nativeLocationListener,
                            Looper.getMainLooper()
                        )
                    }
                } catch (e: Exception) {
                    KronoDiagLog.w("KRONO_DIAG", "[LOCATION_MONITOR] NETWORK_PROVIDER registration failed: ${e.message}")
                }
            }

            isMonitoring = true
            KronoDiagLog.d("KRONO_DIAG", "[LOCATION_MONITOR] isMonitoring set to TRUE")

            fusedClient.lastLocation.addOnSuccessListener { loc ->
                try {
                    if (loc != null) {
                        KronoDiagLog.d("KRONO_DIAG", "[LOCATION_MONITOR] fused lastLocation obtained: provider=${loc.provider}")
                        evaluateLocation(loc)
                    }
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            KronoDiagLog.e("KRONO_DIAG", "[LOCATION_MONITOR] startMonitoring EXCEPTION: ${e.javaClass.name}: ${e.message}", e)
            isMonitoring = false
        }
    }

    fun stopMonitoring() {
        if (!isMonitoring) return
        KronoDiagLog.d("KRONO_DIAG", "[LOCATION_MONITOR] stopMonitoring() unregistering listeners and clearing areaInsideState")
        try {
            fusedClient.removeLocationUpdates(fusedCallback)
            locationManager?.removeUpdates(nativeLocationListener)
        } catch (_: Exception) {}
        isMonitoring = false
        areaInsideState.clear()
    }

    fun evaluateLocation(location: Location) {
        val nowElapsed = SystemClock.elapsedRealtime()
        val nowUtc = System.currentTimeMillis()
        val locationAgeMs = (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000L

        val copyOfAreas = synchronized(monitoredAreas) { monitoredAreas.toList() }
        for (area in copyOfAreas) {
            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                area.latitude,
                area.longitude,
                results
            )
            val distanceM = results[0]
            val isInside = distanceM <= area.radiusM
            val previousState = areaInsideState[area.areaId]

            val lastLog = lastEvalLogTime[area.areaId] ?: 0L
            val isTransition = previousState == null || previousState != isInside
            if (isTransition || (nowElapsed - lastLog >= 5000L)) {
                lastEvalLogTime[area.areaId] = nowElapsed
                KronoDiagLog.d(
                    "KRONO_DIAG",
                    "[LOCATION_MONITOR] eval areaId=${area.areaId} dist=${distanceM.toInt()}m rad=${area.radiusM.toInt()}m isInside=$isInside prev=$previousState age=${locationAgeMs}ms prov=${location.provider}"
                )
            }

            if (isTransition) {
                areaInsideState[area.areaId] = isInside
                KronoDiagLog.d(
                    "KRONO_DIAG",
                    "[LOCATION_MONITOR] Transition detected on ${area.areaId}: $previousState -> $isInside (listenerPresent=${onTransitionListener != null})"
                )
                onTransitionListener?.invoke(area.areaId, isInside, nowElapsed, nowUtc)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun checkCurrentLocation(onEvaluated: (Map<String, Boolean>) -> Unit) {
        try {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                val fallback = try {
                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } catch (_: Exception) {
                    null
                }
                val chosenLoc = loc ?: fallback
                if (chosenLoc != null) {
                    val resultMap = mutableMapOf<String, Boolean>()
                    val copyOfAreas = synchronized(monitoredAreas) { monitoredAreas.toList() }
                    for (area in copyOfAreas) {
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            chosenLoc.latitude,
                            chosenLoc.longitude,
                            area.latitude,
                            area.longitude,
                            results
                        )
                        resultMap[area.areaId] = (results[0] <= area.radiusM)
                    }
                    onEvaluated(resultMap)
                } else {
                    onEvaluated(emptyMap())
                }
            }.addOnFailureListener {
                val fallbackLoc = try {
                    locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } catch (_: Exception) {
                    null
                }
                if (fallbackLoc != null) {
                    val resultMap = mutableMapOf<String, Boolean>()
                    val copyOfAreas = synchronized(monitoredAreas) { monitoredAreas.toList() }
                    for (area in copyOfAreas) {
                        val results = FloatArray(1)
                        Location.distanceBetween(
                            fallbackLoc.latitude,
                            fallbackLoc.longitude,
                            area.latitude,
                            area.longitude,
                            results
                        )
                        resultMap[area.areaId] = (results[0] <= area.radiusM)
                    }
                    onEvaluated(resultMap)
                } else {
                    onEvaluated(emptyMap())
                }
            }
        } catch (_: Exception) {
            onEvaluated(emptyMap())
        }
    }
}
