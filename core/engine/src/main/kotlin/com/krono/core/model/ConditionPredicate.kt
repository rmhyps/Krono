package com.krono.core.model

enum class PredicateKind {
    ALWAYS,
    NONE,
    LOCATION_IN_AREA,
    BLUETOOTH_IS_CONNECTED,
    BLUETOOTH_IS_DISCONNECTED,
    WIFI_IS_CONNECTED,
    WIFI_IS_DISCONNECTED
}

data class ConditionPredicate(
    val kind: PredicateKind,
    val areaId: String? = null,
    val deviceId: String? = null,
    val wifiSsid: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusM: Double? = null
)
