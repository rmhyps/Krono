package com.krono.platform.lifecycle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.krono.platform.scheduler.KronoReconcileWorker

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            KronoReconcileWorker.enqueue(context)
        }
    }
}
