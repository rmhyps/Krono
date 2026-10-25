package com.krono.core.evaluator

enum class BluetoothConnectionState {
    CONNECTED,
    DISCONNECTED,
    UNKNOWN
}

data class EvaluationSnapshot(
    val areaStates: Map<String, ConditionResult> = emptyMap(),
    val bluetoothStates: Map<String, BluetoothConnectionState> = emptyMap(),
    val nowElapsedMs: Long,
    val nowUtcMs: Long,
    val snapshotVersion: Int = 1
)
