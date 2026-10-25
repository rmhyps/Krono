package com.krono.core.episode

import com.krono.core.evaluator.BluetoothConnectionState

sealed class BluetoothTransitionResult {
    data class NewEpisode(val episodeKey: String, val deviceId: String) : BluetoothTransitionResult()
    data class DuplicateReport(val episodeKey: String, val deviceId: String) : BluetoothTransitionResult()
    data class Disconnected(val deviceId: String) : BluetoothTransitionResult()
    object Ignored : BluetoothTransitionResult()
}

class BluetoothEpisodeTracker {
    private val deviceEpisodes = mutableMapOf<String, String>()
    private val deviceCounters = mutableMapOf<String, Int>()
    private val deviceStates = mutableMapOf<String, BluetoothConnectionState>()

    fun onConnectionStateChanged(
        deviceId: String,
        newState: BluetoothConnectionState
    ): BluetoothTransitionResult {
        val currentState = deviceStates[deviceId] ?: BluetoothConnectionState.DISCONNECTED

        return when (newState) {
            BluetoothConnectionState.CONNECTED -> {
                if (currentState == BluetoothConnectionState.CONNECTED) {
                    val existingKey = deviceEpisodes[deviceId] ?: run {
                        val nextCounter = (deviceCounters[deviceId] ?: 0) + 1
                        deviceCounters[deviceId] = nextCounter
                        val newKey = "bt:$deviceId:$nextCounter"
                        deviceEpisodes[deviceId] = newKey
                        newKey
                    }
                    BluetoothTransitionResult.DuplicateReport(existingKey, deviceId)
                } else {
                    val nextCounter = (deviceCounters[deviceId] ?: 0) + 1
                    deviceCounters[deviceId] = nextCounter
                    val newKey = "bt:$deviceId:$nextCounter"
                    deviceEpisodes[deviceId] = newKey
                    deviceStates[deviceId] = BluetoothConnectionState.CONNECTED
                    BluetoothTransitionResult.NewEpisode(newKey, deviceId)
                }
            }
            BluetoothConnectionState.DISCONNECTED -> {
                deviceStates[deviceId] = BluetoothConnectionState.DISCONNECTED
                deviceEpisodes.remove(deviceId)
                BluetoothTransitionResult.Disconnected(deviceId)
            }
            BluetoothConnectionState.UNKNOWN -> {
                BluetoothTransitionResult.Ignored
            }
        }
    }

    fun getActiveEpisode(deviceId: String): String? = deviceEpisodes[deviceId]

    fun getCurrentState(deviceId: String): BluetoothConnectionState =
        deviceStates[deviceId] ?: BluetoothConnectionState.DISCONNECTED
}
