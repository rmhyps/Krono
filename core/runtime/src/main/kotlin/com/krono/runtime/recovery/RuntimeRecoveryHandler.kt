package com.krono.runtime.recovery

import com.krono.core.result.ActionExecutionResult
import com.krono.core.result.ActionExecutionStatus
import com.krono.runtime.journal.EffectJournalRecorder

class RuntimeRecoveryHandler(
    private val journalRecorder: EffectJournalRecorder
) {

    fun performStartupRecovery(currentEpoch: String, nowUtcMs: Long): Int {
        val dangling = journalRecorder.findDanglingAttempts(currentEpoch)
        var recoveredCount = 0
        for (attempt in dangling) {
            journalRecorder.recordFinished(
                attemptId = attempt.attemptId,
                result = ActionExecutionResult(
                    actionId = attempt.actionId,
                    status = ActionExecutionStatus.UNVERIFIED,
                    reason = "Process terminated while action was in ${attempt.phase} phase; conservative recovery applied"
                ),
                nowUtcMs = nowUtcMs
            )
            recoveredCount++
        }
        return recoveredCount
    }
}
