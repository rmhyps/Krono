package com.krono.runtime.coordinator

import com.krono.core.arbitration.ArbitrationCandidate
import com.krono.core.arbitration.BatchArbiter
import com.krono.core.episode.BluetoothEpisodeTracker
import com.krono.core.episode.BluetoothTransitionResult
import com.krono.core.episode.LocationEpisodeTracker
import com.krono.core.episode.LocationEpisodeTransition
import com.krono.core.evaluator.BranchSelectionResult
import com.krono.core.evaluator.EvaluationSnapshot
import com.krono.core.evaluator.RuleEvaluator
import com.krono.core.model.ActionDefinition
import com.krono.core.model.TriggerFamily
import com.krono.core.ports.AutomationStore
import com.krono.core.ports.CapabilityPort
import com.krono.core.ports.CapabilityStatus
import com.krono.core.ports.ClockPort
import com.krono.core.ports.EffectPort
import com.krono.core.ports.MonitoringPort
import com.krono.core.result.ActionExecutionResult
import com.krono.core.result.ActionExecutionStatus
import com.krono.core.result.ResultAggregator
import com.krono.core.result.RuleExecutionSummary
import com.krono.runtime.ingress.LogicalEvent
import com.krono.runtime.journal.EffectJournalRecorder
import com.krono.runtime.journal.JournalPhase
import com.krono.runtime.plan.ExecutionPlan
import com.krono.runtime.recovery.RuntimeRecoveryHandler
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

data class PlanExecutionReport(
    val planId: String,
    val ruleId: String,
    val summary: RuleExecutionSummary,
    val actionResults: List<ActionExecutionResult>
)

class KronoRuntimeCoordinator(
    private val clockPort: ClockPort,
    private val capabilityPort: CapabilityPort,
    private val effectPort: EffectPort,
    private val store: AutomationStore,
    private val monitoringPort: MonitoringPort,
    private val journalRecorder: EffectJournalRecorder,
    private val recoveryHandler: RuntimeRecoveryHandler,
    val runtimeEpoch: String = RuntimeEpoch.current
) {

    private val invokerMutex = Mutex()
    private var nextIntentSeq: Long = 1
    private val targetIntentHeads = mutableMapOf<String, Long>()
    private val locationTrackers = mutableMapOf<String, LocationEpisodeTracker>()
    private val bluetoothTrackers = mutableMapOf<String, BluetoothEpisodeTracker>()

    suspend fun initialize(): Boolean {
        recoveryHandler.performStartupRecovery(runtimeEpoch, clockPort.nowUtcMs())
        return true
    }

    suspend fun onLogicalEvent(
        event: LogicalEvent,
        snapshot: EvaluationSnapshot
    ): List<PlanExecutionReport> {
        val activeRules = store.getActiveRules()
        val priorityList = store.getRulePriorityList()

        val matchingRules = activeRules.filter { rule ->
            rule.trigger.family.name == event.sourceFamily
        }

        val candidatesWithEpisode = mutableListOf<Pair<ArbitrationCandidate, String>>()

        for (rule in matchingRules) {
            val selection = RuleEvaluator.selectBranch(rule, snapshot)
            val episodeKey: String? = when (rule.trigger.family) {
                TriggerFamily.LOCATION -> {
                    val tracker = locationTrackers.getOrPut(rule.ruleId) { LocationEpisodeTracker(rule.ruleId) }
                    when (val transition = tracker.onBranchSelectionChanged(selection)) {
                        is LocationEpisodeTransition.NewBranchEpisode -> transition.episodeKey
                        is LocationEpisodeTransition.ExistingEpisodeContinued -> null
                        is LocationEpisodeTransition.EpisodeClosed,
                        LocationEpisodeTransition.WaitingDataRetained -> null
                    }
                }
                TriggerFamily.BLUETOOTH -> {
                    val deviceId = rule.trigger.deviceId ?: event.sourceId ?: ""
                    val btState = snapshot.bluetoothStates[deviceId]
                    val tracker = bluetoothTrackers.getOrPut(rule.ruleId) { BluetoothEpisodeTracker() }
                    if (btState != null && deviceId.isNotEmpty()) {
                        when (val transition = tracker.onConnectionStateChanged(deviceId, btState)) {
                            is BluetoothTransitionResult.NewEpisode -> transition.episodeKey
                            is BluetoothTransitionResult.DuplicateReport -> null
                            is BluetoothTransitionResult.Disconnected,
                            BluetoothTransitionResult.Ignored -> null
                        }
                    } else if (selection is BranchSelectionResult.Match) {
                        event.causeKey
                    } else null
                }
            }

            if (selection is BranchSelectionResult.Match && episodeKey != null) {
                val priorityPos = priorityList.indexOf(rule.ruleId).let {
                    if (it == -1) Int.MAX_VALUE else it
                }
                val candidate = ArbitrationCandidate(
                    ruleId = rule.ruleId,
                    ruleRevision = rule.revision,
                    activationGeneration = 1,
                    priorityPosition = priorityPos,
                    branch = selection.branch,
                    isPreflightReady = true
                )
                candidatesWithEpisode.add(candidate to episodeKey)
            }
        }

        val arbitration = BatchArbiter.arbitrate(candidatesWithEpisode.map { it.first })
        val reports = mutableListOf<PlanExecutionReport>()

        for (winner in arbitration.winners) {
            val matchedEpisodeKey = candidatesWithEpisode.firstOrNull { it.first.ruleId == winner.candidate.ruleId }?.second ?: event.causeKey
            val plan = ExecutionPlan(
                planId = "plan-" + UUID.randomUUID().toString(),
                ruleId = winner.candidate.ruleId,
                ruleRevision = winner.candidate.ruleRevision,
                activationGeneration = winner.candidate.activationGeneration,
                branchId = winner.candidate.branch.branchId,
                causeKey = matchedEpisodeKey,
                intentSeq = allocateIntentSeq(winner.reservedTargets),
                deadlineElapsedMs = event.receivedElapsedMs + 30000L,
                requiredTargets = winner.reservedTargets,
                actions = winner.candidate.branch.actions,
                runtimeEpoch = runtimeEpoch
            )

            val report = executePlan(plan)
            reports.add(report)
        }

        return reports
    }

    suspend fun executePlan(plan: ExecutionPlan): PlanExecutionReport = invokerMutex.withLock {
        val actionResults = mutableListOf<ActionExecutionResult>()

        for ((index, action) in plan.actions.withIndex()) {
            val nowElapsed = clockPort.nowElapsedMs()

            if (nowElapsed >= plan.deadlineElapsedMs) {
                actionResults.add(
                    ActionExecutionResult(
                        actionId = action.actionId,
                        status = ActionExecutionStatus.EXPIRED,
                        reason = "Deadline exceeded ($nowElapsed >= ${plan.deadlineElapsedMs})"
                    )
                )
                break
            }

            if (isPlanSuperseded(plan)) {
                actionResults.add(
                    ActionExecutionResult(
                        actionId = action.actionId,
                        status = ActionExecutionStatus.SUPERSEDED,
                        reason = "Plan superseded by newer intent"
                    )
                )
                break
            }

            val capStatus = capabilityPort.checkCapability(action)
            if (capStatus !is CapabilityStatus.Ready) {
                val reason = when (capStatus) {
                    is CapabilityStatus.Blocked -> capStatus.reason
                    is CapabilityStatus.Unknown -> capStatus.reason
                    CapabilityStatus.Ready -> ""
                }
                actionResults.add(
                    ActionExecutionResult(
                        actionId = action.actionId,
                        status = ActionExecutionStatus.BLOCKED,
                        reason = reason
                    )
                )
                break
            }

            val attemptId = "att-" + UUID.randomUUID().toString()
            val effectKey = plan.effectKeyForAction(action.actionId)

            val existingEntry = journalRecorder.getEntryByEffectKey(effectKey)
            if (existingEntry != null && (existingEntry.phase == JournalPhase.REQUESTING || existingEntry.phase == JournalPhase.OBSERVING || existingEntry.phase == JournalPhase.FINISHED)) {
                actionResults.add(
                    existingEntry.result ?: ActionExecutionResult(
                        actionId = action.actionId,
                        status = ActionExecutionStatus.NO_CHANGE_NEEDED,
                        evidenceSummary = "Effect key already recorded in journal for this episode: $effectKey"
                    )
                )
                continue
            }

            journalRecorder.recordPrepared(
                attemptId = attemptId,
                planId = plan.planId,
                stepIndex = index,
                actionId = action.actionId,
                effectKey = effectKey,
                runtimeEpoch = runtimeEpoch,
                preparedElapsedMs = nowElapsed
            )

            val requestingOk = journalRecorder.transitionToRequesting(attemptId, nowElapsed)
            if (!requestingOk) {
                actionResults.add(
                    ActionExecutionResult(
                        actionId = action.actionId,
                        status = ActionExecutionStatus.FAILED,
                        reason = "Failed to commit REQUESTING phase"
                    )
                )
                break
            }

            val executedResult = effectPort.executeAction(action)
            journalRecorder.transitionToObserving(attemptId)
            journalRecorder.recordFinished(attemptId, executedResult, clockPort.nowUtcMs())
            actionResults.add(executedResult)

            if (executedResult.status != ActionExecutionStatus.APPLIED &&
                executedResult.status != ActionExecutionStatus.NO_CHANGE_NEEDED
            ) {
                for (remainingIdx in (index + 1) until plan.actions.size) {
                    val remainingAction = plan.actions[remainingIdx]
                    actionResults.add(
                        ActionExecutionResult(
                            actionId = remainingAction.actionId,
                            status = ActionExecutionStatus.SKIPPED,
                            reason = "Previous action failed with status ${executedResult.status}"
                        )
                    )
                }
                break
            }
        }

        val summary = ResultAggregator.aggregate(actionResults)
        PlanExecutionReport(
            planId = plan.planId,
            ruleId = plan.ruleId,
            summary = summary,
            actionResults = actionResults
        )
    }

    suspend fun executeTestAction(action: ActionDefinition, commandId: String): ActionExecutionResult = invokerMutex.withLock {
        val nowElapsed = clockPort.nowElapsedMs()
        val capStatus = capabilityPort.checkCapability(action)
        if (capStatus !is CapabilityStatus.Ready) {
            val reason = when (capStatus) {
                is CapabilityStatus.Blocked -> capStatus.reason
                is CapabilityStatus.Unknown -> capStatus.reason
                CapabilityStatus.Ready -> ""
            }
            return ActionExecutionResult(
                actionId = action.actionId,
                status = ActionExecutionStatus.BLOCKED,
                reason = reason
            )
        }

        val attemptId = "att-test-" + UUID.randomUUID().toString()
        val effectKey = "test:$commandId:${action.actionId}"

        journalRecorder.recordPrepared(
            attemptId = attemptId,
            planId = "plan-test-$commandId",
            stepIndex = 0,
            actionId = action.actionId,
            effectKey = effectKey,
            runtimeEpoch = runtimeEpoch,
            preparedElapsedMs = nowElapsed
        )

        journalRecorder.transitionToRequesting(attemptId, nowElapsed)
        val result = effectPort.executeAction(action)
        journalRecorder.transitionToObserving(attemptId)
        journalRecorder.recordFinished(attemptId, result, clockPort.nowUtcMs())
        result
    }

    private fun allocateIntentSeq(targets: Set<String>): Long {
        val seq = nextIntentSeq++
        for (target in targets) {
            targetIntentHeads[target] = seq
        }
        return seq
    }

    private fun isPlanSuperseded(plan: ExecutionPlan): Boolean {
        for (target in plan.requiredTargets) {
            val currentHead = targetIntentHeads[target]
            if (currentHead != null && currentHead > plan.intentSeq) {
                return true
            }
        }
        return false
    }

    fun supersedeTargets(targets: Set<String>) {
        val nextSeq = nextIntentSeq++
        for (t in targets) {
            targetIntentHeads[t] = nextSeq
        }
    }
}
