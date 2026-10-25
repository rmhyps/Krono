package com.krono.runtime.journal

import com.krono.core.result.ActionExecutionResult
import com.krono.core.result.ActionExecutionStatus

enum class JournalPhase {
    PREPARED,
    REQUESTING,
    OBSERVING,
    FINISHED
}

data class JournalEntry(
    val attemptId: String,
    val planId: String,
    val stepIndex: Int,
    val actionId: String,
    val effectKey: String,
    val runtimeEpoch: String,
    var phase: JournalPhase,
    var result: ActionExecutionResult? = null,
    val preparedElapsedMs: Long,
    var requestingElapsedMs: Long? = null,
    var finishedAtUtcMs: Long? = null
)

class EffectJournalRecorder {
    private val entries = mutableMapOf<String, JournalEntry>()

    fun recordPrepared(
        attemptId: String,
        planId: String,
        stepIndex: Int,
        actionId: String,
        effectKey: String,
        runtimeEpoch: String,
        preparedElapsedMs: Long
    ): JournalEntry {
        val entry = JournalEntry(
            attemptId = attemptId,
            planId = planId,
            stepIndex = stepIndex,
            actionId = actionId,
            effectKey = effectKey,
            runtimeEpoch = runtimeEpoch,
            phase = JournalPhase.PREPARED,
            preparedElapsedMs = preparedElapsedMs
        )
        entries[attemptId] = entry
        return entry
    }

    fun transitionToRequesting(attemptId: String, nowElapsedMs: Long): Boolean {
        val entry = entries[attemptId] ?: return false
        if (entry.phase != JournalPhase.PREPARED) return false
        entry.phase = JournalPhase.REQUESTING
        entry.requestingElapsedMs = nowElapsedMs
        return true
    }

    fun transitionToObserving(attemptId: String): Boolean {
        val entry = entries[attemptId] ?: return false
        if (entry.phase != JournalPhase.REQUESTING) return false
        entry.phase = JournalPhase.OBSERVING
        return true
    }

    fun recordFinished(
        attemptId: String,
        result: ActionExecutionResult,
        nowUtcMs: Long
    ): Boolean {
        val entry = entries[attemptId] ?: return false
        entry.phase = JournalPhase.FINISHED
        entry.result = result
        entry.finishedAtUtcMs = nowUtcMs
        return true
    }

    fun getEntry(attemptId: String): JournalEntry? = entries[attemptId]

    fun getEntryByEffectKey(effectKey: String): JournalEntry? =
        entries.values.firstOrNull { it.effectKey == effectKey }

    fun findDanglingAttempts(currentEpoch: String): List<JournalEntry> {
        return entries.values.filter {
            it.runtimeEpoch != currentEpoch && (it.phase == JournalPhase.REQUESTING || it.phase == JournalPhase.OBSERVING)
        }
    }
}
