package com.krono.platform.effect

import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.krono.core.model.ActionDefinition
import com.krono.core.result.ActionExecutionResult
import com.krono.core.result.ActionExecutionStatus
import kotlin.math.roundToInt

class AudioEffectManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun executeSetVolume(action: ActionDefinition): ActionExecutionResult {
        val am = audioManager ?: return ActionExecutionResult(
            actionId = action.actionId,
            status = ActionExecutionStatus.FAILED,
            reason = "AudioManager system service unavailable"
        )

        val percent = action.volumePercent ?: return ActionExecutionResult(
            actionId = action.actionId,
            status = ActionExecutionStatus.FAILED,
            reason = "Volume percent missing in action"
        )

        val minVolume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            am.getStreamMinVolume(AudioManager.STREAM_MUSIC)
        } else {
            0
        }
        val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetIndex = (minVolume + (percent / 100.0) * (maxVolume - minVolume)).roundToInt().coerceIn(minVolume, maxVolume)

        val currentIndex = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (currentIndex == targetIndex) {
            return ActionExecutionResult(
                actionId = action.actionId,
                status = ActionExecutionStatus.NO_CHANGE_NEEDED,
                evidenceSummary = "Current volume already at index $targetIndex"
            )
        }

        return try {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, 0)
            val readBackIndex = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (readBackIndex == targetIndex) {
                ActionExecutionResult(
                    actionId = action.actionId,
                    status = ActionExecutionStatus.APPLIED,
                    evidenceSummary = "Volume changed from $currentIndex to $readBackIndex (target $percent%)"
                )
            } else {
                ActionExecutionResult(
                    actionId = action.actionId,
                    status = ActionExecutionStatus.UNVERIFIED,
                    evidenceSummary = "Setter invoked; observed index $readBackIndex vs target $targetIndex"
                )
            }
        } catch (e: SecurityException) {
            ActionExecutionResult(
                actionId = action.actionId,
                status = ActionExecutionStatus.BLOCKED,
                reason = "SecurityException: ${e.message}"
            )
        } catch (e: Exception) {
            ActionExecutionResult(
                actionId = action.actionId,
                status = ActionExecutionStatus.FAILED,
                reason = "Exception: ${e.message}"
            )
        }
    }
}
