package com.krono.platform.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class KronoReconcileWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "krono_reconcile_work"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<KronoReconcileWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        ReconciliationHandler.onReconcileNeeded?.invoke()
        return Result.success()
    }
}

object ReconciliationHandler {
    var onReconcileNeeded: (suspend () -> Unit)? = null
}
