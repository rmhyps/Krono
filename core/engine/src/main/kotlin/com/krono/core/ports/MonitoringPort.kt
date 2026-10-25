package com.krono.core.ports

interface MonitoringPort {
    suspend fun reconcileMonitoring(
        desiredAreaIds: Set<String>,
        desiredDeviceIds: Set<String>
    ): Boolean
}
