package com.krono.platform.location

import com.krono.core.ports.MonitoringPort
import com.krono.platform.diagnostic.KronoDiagLog

class AndroidMonitoringAdapter(
    val geofenceManager: GeofenceManager,
    val locationMonitor: KronoLocationMonitor
) : MonitoringPort {

    fun updateAreas(areas: List<GeofenceArea>) {
        KronoDiagLog.d("KRONO_DIAG", "[MONITOR_ADAPTER] updateAreas() count=${areas.size} areas=${areas.map { it.areaId }}")
        geofenceManager.registerGeofences(
            areas,
            onSuccess = {
                KronoDiagLog.d("KRONO_DIAG", "[GEOFENCE] registerGeofences SUCCESS for ${areas.size} areas")
            },
            onFailure = { err ->
                KronoDiagLog.e("KRONO_DIAG", "[GEOFENCE] registerGeofences FAILED: ${err.javaClass.name}: ${err.message}", err)
            }
        )
        locationMonitor.updateMonitoredAreas(areas)
    }

    override suspend fun reconcileMonitoring(
        desiredAreaIds: Set<String>,
        desiredDeviceIds: Set<String>
    ): Boolean {
        return true
    }
}
