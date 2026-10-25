package com.krono.core.model

enum class TriggerFamily {
    LOCATION,
    BLUETOOTH
}

enum class TriggerKind {
    GEOFENCE_MEMBERSHIP_CHANGED,
    BLUETOOTH_CONNECTED,
    BLUETOOTH_DISCONNECTED,
    BLUETOOTH_STATE_CHANGED
}

data class TriggerDefinition(
    val family: TriggerFamily,
    val kind: TriggerKind,
    val areaIds: List<String> = emptyList(),
    val deviceId: String? = null
)
