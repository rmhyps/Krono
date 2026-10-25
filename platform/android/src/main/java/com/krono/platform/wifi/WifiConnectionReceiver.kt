package com.krono.platform.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import com.krono.platform.diagnostic.KronoDiagLog

class WifiConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        KronoDiagLog.d("KRONO_DIAG", "[WIFI_RECEIVER] onReceive action=$action")

        val (isConnected, ssid) = getWifiState(context)
        val nowElapsed = SystemClock.elapsedRealtime()
        val nowUtc = System.currentTimeMillis()

        if (isConnected && !ssid.isNullOrBlank() && ssid != "<unknown ssid>") {
            KronoDiagLog.d("KRONO_DIAG", "[WIFI_RECEIVER] Connected to Wi-Fi SSID=$ssid")
            WifiEventHandler.onConnected(ssid, nowElapsed, nowUtc)
        } else if (!isConnected) {
            KronoDiagLog.d("KRONO_DIAG", "[WIFI_RECEIVER] Disconnected from Wi-Fi")
            WifiEventHandler.onDisconnected(ssid ?: "", nowElapsed, nowUtc)
        }
    }

    companion object {
        fun getWifiState(context: Context): Pair<Boolean, String?> {
            return try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val activeNetwork = cm?.activeNetwork
                    val caps = cm?.getNetworkCapabilities(activeNetwork)
                    val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

                    if (isWifi) {
                        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                        val rawSsid = wm?.connectionInfo?.ssid?.removeSurrounding("\"")
                        Pair(true, if (rawSsid == "<unknown ssid>") null else rawSsid)
                    } else {
                        Pair(false, null)
                    }
                } else {
                    val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    val info = wm?.connectionInfo
                    val isConnected = info?.networkId != -1
                    val rawSsid = info?.ssid?.removeSurrounding("\"")
                    Pair(isConnected, if (rawSsid == "<unknown ssid>") null else rawSsid)
                }
            } catch (e: Exception) {
                KronoDiagLog.w("KRONO_DIAG", "[WIFI_RECEIVER] Failed to get Wi-Fi state: ${e.message}")
                Pair(false, null)
            }
        }
    }
}

object WifiEventHandler {
    var listener: ((ssid: String, isConnected: Boolean, elapsedMs: Long, utcMs: Long) -> Unit)? = null

    fun onConnected(ssid: String, elapsedMs: Long, utcMs: Long) {
        KronoDiagLog.d("KRONO_DIAG", "[WIFI_EVENT] onConnected ssid=$ssid listenerActive=${listener != null}")
        listener?.invoke(ssid, true, elapsedMs, utcMs)
    }

    fun onDisconnected(ssid: String, elapsedMs: Long, utcMs: Long) {
        KronoDiagLog.d("KRONO_DIAG", "[WIFI_EVENT] onDisconnected ssid=$ssid listenerActive=${listener != null}")
        listener?.invoke(ssid, false, elapsedMs, utcMs)
    }
}
