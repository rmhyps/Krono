package com.krono.platform.bluetooth

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock

class BluetoothConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) return

        val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED)
        val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        } ?: return

        val deviceAddress = device.address ?: return
        val nowElapsed = SystemClock.elapsedRealtime()
        val nowUtc = System.currentTimeMillis()

        when (state) {
            BluetoothProfile.STATE_CONNECTED -> {
                BluetoothEventHandler.onConnected(deviceAddress, nowElapsed, nowUtc)
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                BluetoothEventHandler.onDisconnected(deviceAddress, nowElapsed, nowUtc)
            }
        }
    }
}

object BluetoothEventHandler {
    var listener: ((deviceAddress: String, isConnected: Boolean, elapsedMs: Long, utcMs: Long) -> Unit)? = null

    fun onConnected(deviceAddress: String, elapsedMs: Long, utcMs: Long) {
        listener?.invoke(deviceAddress, true, elapsedMs, utcMs)
    }

    fun onDisconnected(deviceAddress: String, elapsedMs: Long, utcMs: Long) {
        listener?.invoke(deviceAddress, false, elapsedMs, utcMs)
    }
}
