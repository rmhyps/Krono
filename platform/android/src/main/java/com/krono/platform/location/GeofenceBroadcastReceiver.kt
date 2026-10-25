package com.krono.platform.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
        if (triggeringGeofences == null) {
            KronoDiagLog.w("KRONO_DIAG", "[GEOFENCE_RECEIVER] triggeringGeofences is null")
            return
        }

        val nowElapsed = SystemClock.elapsedRealtime()
        val nowUtc = System.currentTimeMillis()
        KronoDiagLog.d("KRONO_DIAG", "[GEOFENCE_RECEIVER] transition=$transition geofencesCount=${triggeringGeofences.size}")

        for (geofence in triggeringGeofences) {
            val areaId = geofence.requestId
            when (transition) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    GeofenceEventHandler.onAreaEntered(areaId, nowElapsed, nowUtc)
                }
                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    GeofenceEventHandler.onAreaExited(areaId, nowElapsed, nowUtc)
                }
            }
        }
    }
}

object GeofenceEventHandler {
    var listener: ((areaId: String, isInside: Boolean, elapsedMs: Long, utcMs: Long) -> Unit)? = null

    fun onAreaEntered(areaId: String, elapsedMs: Long, utcMs: Long) {
        KronoDiagLog.d("KRONO_DIAG", "[GEOFENCE_EVENT] onAreaEntered areaId=$areaId listenerActive=${listener != null}")
        listener?.invoke(areaId, true, elapsedMs, utcMs)
    }

    fun onAreaExited(areaId: String, elapsedMs: Long, utcMs: Long) {
        KronoDiagLog.d("KRONO_DIAG", "[GEOFENCE_EVENT] onAreaExited areaId=$areaId listenerActive=${listener != null}")
        listener?.invoke(areaId, false, elapsedMs, utcMs)
    }
}
