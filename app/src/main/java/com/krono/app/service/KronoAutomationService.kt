package com.krono.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.Manifest
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import com.krono.app.MainActivity
import com.krono.app.hub.KronoRuntimeHub

class KronoAutomationService : Service() {

    companion object {
        const val CHANNEL_ID = "krono_automation_channel"
        const val NOTIFICATION_ID = 1010

        fun start(context: Context): Boolean {
            val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            Log.d("KRONO_DIAG", "[FGS] start() called. finePermission=${fine == PackageManager.PERMISSION_GRANTED}, coarsePermission=${coarse == PackageManager.PERMISSION_GRANTED}")
            if (fine != PackageManager.PERMISSION_GRANTED) {
                Log.w("KRONO_DIAG", "[FGS] start() ABORTED: ACCESS_FINE_LOCATION is not granted")
                return false
            }
            val intent = Intent(context, KronoAutomationService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("KRONO_DIAG", "[FGS] Calling startForegroundService(intent)")
                    context.startForegroundService(intent)
                } else {
                    Log.d("KRONO_DIAG", "[FGS] Calling startService(intent)")
                    context.startService(intent)
                }
                return true
            } catch (e: Exception) {
                Log.e("KRONO_DIAG", "[FGS] start() failed to start service: ${e.javaClass.name}: ${e.message}", e)
                Log.w("KronoAutomationService", "Tidak dapat memulai pemantauan lokasi", e)
                return false
            }
        }

        fun stop(context: Context) {
            Log.d("KRONO_DIAG", "[FGS] stop() requested")
            val intent = Intent(context, KronoAutomationService::class.java)
            try {
                context.stopService(intent)
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("KRONO_DIAG", "[FGS] onCreate() invoked")
        createNotificationChannel()
        val notification = buildForegroundNotification()

        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED) {
            Log.w("KRONO_DIAG", "[FGS] onCreate() stopping self: fine permission not granted")
            stopSelf()
            return
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d("KRONO_DIAG", "[FGS] Calling startForeground with TYPE_LOCATION")
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                Log.d("KRONO_DIAG", "[FGS] Calling startForeground (pre-Q)")
                startForeground(NOTIFICATION_ID, notification)
            }
            Log.d("KRONO_DIAG", "[FGS] startForeground completed successfully")
        } catch (e: Exception) {
            Log.e("KRONO_DIAG", "[FGS] startForeground THREW EXCEPTION: ${e.javaClass.name}: ${e.message}", e)
            Log.e("KronoAutomationService", "Layanan lokasi tidak memenuhi syarat Android", e)
            stopSelf()
            return
        }

        val hub = KronoRuntimeHub.getInstance(this)
        hub.setupEventHandlers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("KRONO_DIAG", "[FGS] onStartCommand() flags=$flags startId=$startId")
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("KRONO_DIAG", "[FGS] onStartCommand() fine permission missing, stopping self")
            stopSelf()
            return START_NOT_STICKY
        }
        val hub = KronoRuntimeHub.getInstance(this)
        hub.setupEventHandlers()
        hub.scope.launch { hub.syncMonitoring(startService = false) }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("KRONO_DIAG", "[FGS] onDestroy() invoked, calling locationMonitor.stopMonitoring()")
        val hub = KronoRuntimeHub.getInstance(this)
        hub.locationMonitor.stopMonitoring()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Krono Otomasi Latar Belakang",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Menjaga pemantauan lokasi dan bluetooth tetap aktif di background"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Krono")
            .setContentText("Otomasi latar belakang aktif")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
