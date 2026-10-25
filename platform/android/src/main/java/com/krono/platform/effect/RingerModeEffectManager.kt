package com.krono.platform.effect

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.krono.core.model.ActionDefinition
import com.krono.core.model.RingerModeTarget
import com.krono.core.result.ActionExecutionResult
import com.krono.core.result.ActionExecutionStatus
import com.krono.platform.diagnostic.KronoDiagLog

class RingerModeEffectManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    fun executeSetRingerMode(action: ActionDefinition): ActionExecutionResult {
        val am = audioManager ?: return ActionExecutionResult(
            actionId = action.actionId,
            status = ActionExecutionStatus.FAILED,
            reason = "AudioManager unavailable"
        )

        val target = action.ringerMode ?: return ActionExecutionResult(
            actionId = action.actionId,
            status = ActionExecutionStatus.FAILED,
            reason = "RingerModeTarget missing"
        )

        KronoDiagLog.d("KRONO_DIAG", "[RINGER_EFFECT] executeSetRingerMode actionId=${action.actionId} target=$target")

        return try {
            when (target) {
                RingerModeTarget.NORMAL -> {
                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    KronoDiagLog.d("KRONO_DIAG", "[RINGER_EFFECT] ringerMode set to NORMAL")
                    ActionExecutionResult(
                        actionId = action.actionId,
                        status = ActionExecutionStatus.APPLIED,
                        evidenceSummary = "Mode suara berhasil diubah ke Normal"
                    )
                }
                RingerModeTarget.VIBRATE -> {
                    am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                    KronoDiagLog.d("KRONO_DIAG", "[RINGER_EFFECT] ringerMode set to VIBRATE")
                    ActionExecutionResult(
                        actionId = action.actionId,
                        status = ActionExecutionStatus.APPLIED,
                        evidenceSummary = "Mode suara berhasil diubah ke Getar"
                    )
                }
                RingerModeTarget.SILENT -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (notificationManager?.isNotificationPolicyAccessGranted == false) {
                            // Fallback to VIBRATE if DND permission is not yet granted
                            am.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                            KronoDiagLog.w("KRONO_DIAG", "[RINGER_EFFECT] DND access not granted; fell back to VIBRATE")
                            return ActionExecutionResult(
                                actionId = action.actionId,
                                status = ActionExecutionStatus.APPLIED,
                                evidenceSummary = "Izin DND belum ada; beralih ke Getar"
                            )
                        }
                    }
                    am.ringerMode = AudioManager.RINGER_MODE_SILENT
                    KronoDiagLog.d("KRONO_DIAG", "[RINGER_EFFECT] ringerMode set to SILENT")
                    ActionExecutionResult(
                        actionId = action.actionId,
                        status = ActionExecutionStatus.APPLIED,
                        evidenceSummary = "Mode suara berhasil diubah ke Hening"
                    )
                }
            }
        } catch (e: Exception) {
            KronoDiagLog.e("KRONO_DIAG", "[RINGER_EFFECT] Failed to set ringer mode: ${e.message}", e)
            ActionExecutionResult(
                actionId = action.actionId,
                status = ActionExecutionStatus.FAILED,
                reason = "Gagal mengubah mode suara: ${e.message}"
            )
        }
    }
}
