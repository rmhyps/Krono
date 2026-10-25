package com.krono.platform.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.os.SystemClock
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.krono.platform.diagnostic.KronoDiagLog

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        KronoDiagLog.d("KRONO_DIAG", "[GEOFENCE_RECEIVER] onReceive() intent action=${intent.action}")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            KronoDiagLog.w("KRONO_DIAG", "[GEOFENCE_RECEIVER] GeofencingEvent is null")
            return
        }
        if (geofencingEvent.hasError()) {
            KronoDiagLog.e("KRONO_DIAG", "[GEOFENCE_RECEIVER] GeofencingEvent has error: errorCode=${geofencingEvent.errorCode}")
            return
        }

        val transition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences
        if (triggeringGeofences == null || triggeringGeofences.isEmpty()) {
            KronoDiagLog.w("KRONO_DIAG", "[GEOFENCE_RECEIVER] triggeringGeofences is null or empty")
            return
        }

        val pendingResult = goAsync()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "krono:geofence_receiver")?.apply {
            setReferenceCounted(false)
            acquire(20000L) // 20s safety timeout
        }

        val nowElapsed = SystemClock.elapsedRealtime()
        val nowUtc = System.currentTimeMillis()
        KronoDiagLog.d("KRONO_DIAG", "[GEOFENCE_RECEIVER] transition=$transition geofencesCount=${triggeringGeofences.size} goAsync+WakeLock active")

        var remaining = triggeringGeofences.size
        val onOneComplete: () -> Unit = {
            synchronized(this) {
                remaining--
                if (remaining <= 0) {
                    try {
                        if (wakeLock?.isHeld == true) {
                            wakeLock.release()
                        }
                    } catch (_: Exception) {}
                    try {
                        pendingResult.finish()
                    } catch (_: Exception) {}
                    KronoDiagLog.d("KRONO_DIAG", "[GEOFENCE_RECEIVER] All geofence jobs completed, goAsync finished & WakeLock released")
                }
            }
        }

        for (geofence in triggeringGeofences) {
            val areaId = geofence.requestId
            when (transition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    GeofenceEventHandler.onAreaEntered(areaId, nowElapsed, nowUtc, onOneComplete)
                }
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    GeofenceEventHandler.onAreaExited(areaId, nowElapsed, nowUtc, onOneComplete)
                }
                else -> {
                    onOneComplete()
                }
            }
        }
    }
}

object GeofenceEventHandler {
    var listener: ((areaId: String, isInside: Boolean, elapsedMs: Long, utcMs: Long) -> Unit)? = null
    var asyncListener: ((areaId: String, isInside: Boolean, elapsedMs: Long, utcMs: Long, onComplete: (() -> Unit)?) -> Unit)? = null

    fun onAreaEntered(areaId: String, elapsedMs: Long, utcMs: Long, onComplete: (() -> Unit)? = null) {
        KronoDiagLog.d("KRONO_DIAG", "[GEOFENCE_EVENT] onAreaEntered areaId=$areaId listenerActive=${listener != null || asyncListener != null}")
        val al = asyncListener
        if (al != null) {
            al.invoke(areaId, true, elapsedMs, utcMs, onComplete)
        } else {
            listener?.invoke(areaId, true, elapsedMs, utcMs)
            onComplete?.invoke()
        }
    }

    fun onAreaExited(areaId: String, elapsedMs: Long, utcMs: Long, onComplete: (() -> Unit)? = null) {
        KronoDiagLog.d("KRONO_DIAG", "[GEOFENCE_EVENT] onAreaExited areaId=$areaId listenerActive=${listener != null || asyncListener != null}")
        val al = asyncListener
        if (al != null) {
            al.invoke(areaId, false, elapsedMs, utcMs, onComplete)
        } else {
            listener?.invoke(areaId, false, elapsedMs, utcMs)
            onComplete?.invoke()
        }
    }
}
