package com.krono.runtime.coordinator

import com.krono.core.evaluator.BluetoothConnectionState
import com.krono.core.evaluator.ConditionResult
import com.krono.core.evaluator.EvaluationSnapshot
import com.krono.core.fixtures.KronoFixtures
import com.krono.core.model.ActionDefinition
import com.krono.core.model.ActionKind
import com.krono.core.model.BranchDefinition
import com.krono.core.model.BranchKind
import com.krono.core.model.ConditionPredicate
import com.krono.core.model.PredicateKind
import com.krono.core.model.RuleDefinition
import com.krono.core.model.RuleMode
import com.krono.core.model.TriggerDefinition
import com.krono.core.model.TriggerFamily
import com.krono.core.model.TriggerKind
import com.krono.core.ports.AutomationStore
import com.krono.core.ports.CapabilityPort
import com.krono.core.ports.CapabilityStatus
import com.krono.core.ports.ClockPort
import com.krono.core.ports.EffectPort
import com.krono.core.ports.MonitoringPort
import com.krono.core.result.ActionExecutionResult
import com.krono.core.result.ActionExecutionStatus
import com.krono.core.result.RuleExecutionSummary
import com.krono.runtime.ingress.LogicalEvent
import com.krono.runtime.journal.EffectJournalRecorder
import com.krono.runtime.journal.JournalPhase
import com.krono.runtime.plan.ExecutionPlan
import com.krono.runtime.recovery.RuntimeRecoveryHandler
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KronoRuntimeCoordinatorTest {

    private lateinit var clock: TestClock
    private lateinit var capability: TestCapability
    private lateinit var effect: TestEffect
    private lateinit var store: TestStore
    private lateinit var monitoring: TestMonitoring
    private lateinit var journal: EffectJournalRecorder
    private lateinit var recovery: RuntimeRecoveryHandler
    private lateinit var coordinator: KronoRuntimeCoordinator

    private class TestClock(var elapsed: Long = 1000L, var utc: Long = 1000L) : ClockPort {
        override fun nowElapsedMs(): Long = elapsed
        override fun nowUtcMs(): Long = utc
    }

    private class TestCapability(var status: CapabilityStatus = CapabilityStatus.Ready) : CapabilityPort {
        override fun checkCapability(action: ActionDefinition): CapabilityStatus = status
    }

    private class TestEffect : EffectPort {
        val executedActions = mutableListOf<ActionDefinition>()
        var nextResultStatus: ActionExecutionStatus = ActionExecutionStatus.APPLIED
        var failOnSecond: Boolean = false

        override suspend fun executeAction(action: ActionDefinition): ActionExecutionResult {
            executedActions.add(action)
            val status = if (failOnSecond && executedActions.size == 2) {
                ActionExecutionStatus.FAILED
            } else {
                nextResultStatus
            }
            return ActionExecutionResult(
                actionId = action.actionId,
                status = status
            )
        }
    }

    private class TestStore : AutomationStore {
        val rules = mutableMapOf<String, RuleDefinition>()
        val active = mutableMapOf<String, Boolean>()
        val priority = mutableListOf<String>()

        override suspend fun getActiveRules(): List<RuleDefinition> =
            rules.values.filter { active[it.ruleId] == true }

        override suspend fun saveRule(rule: RuleDefinition): Boolean {
            rules[rule.ruleId] = rule
            if (rule.ruleId !in priority) priority.add(rule.ruleId)
            return true
        }

        override suspend fun setRuleActive(ruleId: String, active: Boolean): Boolean {
            this.active[ruleId] = active
            return true
        }

        override suspend fun deleteRule(ruleId: String): Boolean {
            rules.remove(ruleId)
            active.remove(ruleId)
            priority.remove(ruleId)
            return true
        }

        override suspend fun getRulePriorityList(): List<String> = priority.toList()

        override suspend fun updateRulePriorityList(ruleIdsInOrder: List<String>): Boolean {
            priority.clear()
            priority.addAll(ruleIdsInOrder)
            return true
        }
    }

    private class TestMonitoring : MonitoringPort {
        override suspend fun reconcileMonitoring(desiredAreaIds: Set<String>, desiredDeviceIds: Set<String>): Boolean = true
    }

    @Before
    fun setUp() {
        clock = TestClock()
        capability = TestCapability()
        effect = TestEffect()
        store = TestStore()
        monitoring = TestMonitoring()
        journal = EffectJournalRecorder()
        recovery = RuntimeRecoveryHandler(journal)
        coordinator = KronoRuntimeCoordinator(
            clockPort = clock,
            capabilityPort = capability,
            effectPort = effect,
            store = store,
            monitoringPort = monitoring,
            journalRecorder = journal,
            recoveryHandler = recovery
        )
    }

    @Test
    fun startupRecoveryFencesOldEpochAttemptsAsUnverified() = runBlocking {
        journal.recordPrepared(
            attemptId = "old-att",
            planId = "old-plan",
            stepIndex = 0,
            actionId = "a1",
            effectKey = "ek-1",
            runtimeEpoch = "old-epoch-123",
            preparedElapsedMs = 500L
        )
        journal.transitionToRequesting("old-att", 550L)

        coordinator.initialize()

        val recoveredEntry = journal.getEntry("old-att")
        assertEquals(JournalPhase.FINISHED, recoveredEntry?.phase)
        assertEquals(ActionExecutionStatus.UNVERIFIED, recoveredEntry?.result?.status)
    }

    @Test
    fun executePlanAllSuccessResultsInSuccessSummary() = runBlocking {
        val plan = ExecutionPlan(
            planId = "p1",
            ruleId = "r1",
            ruleRevision = 1,
            activationGeneration = 1,
            branchId = "b1",
            causeKey = "cause-1",
            intentSeq = 1,
            deadlineElapsedMs = 50000L,
            requiredTargets = setOf("audio.media"),
            actions = listOf(
                ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20),
                ActionDefinition(actionId = "a2", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 25)
            ),
            runtimeEpoch = coordinator.runtimeEpoch
        )

        val report = coordinator.executePlan(plan)
        assertEquals(RuleExecutionSummary.SUCCESS, report.summary)
        assertEquals(2, effect.executedActions.size)
        assertEquals(ActionExecutionStatus.APPLIED, report.actionResults[0].status)
        assertEquals(ActionExecutionStatus.APPLIED, report.actionResults[1].status)
    }

    @Test
    fun executePlanSecondActionFailsStopsSubsequentActionsAndYieldsPartial() = runBlocking {
        effect.failOnSecond = true
        val plan = ExecutionPlan(
            planId = "p1",
            ruleId = "r1",
            ruleRevision = 1,
            activationGeneration = 1,
            branchId = "b1",
            causeKey = "cause-1",
            intentSeq = 1,
            deadlineElapsedMs = 50000L,
            requiredTargets = setOf("audio.media"),
            actions = listOf(
                ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20),
                ActionDefinition(actionId = "a2", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 25),
                ActionDefinition(actionId = "a3", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 30)
            ),
            runtimeEpoch = coordinator.runtimeEpoch
        )

        val report = coordinator.executePlan(plan)
        assertEquals(RuleExecutionSummary.PARTIAL, report.summary)
        assertEquals(2, effect.executedActions.size)
        assertEquals(ActionExecutionStatus.APPLIED, report.actionResults[0].status)
        assertEquals(ActionExecutionStatus.FAILED, report.actionResults[1].status)
        assertEquals(ActionExecutionStatus.SKIPPED, report.actionResults[2].status)
    }

    @Test
    fun deadlineExceededAbortsActionsWithoutExecutingSetter() = runBlocking {
        clock.elapsed = 60000L
        val plan = ExecutionPlan(
            planId = "p1",
            ruleId = "r1",
            ruleRevision = 1,
            activationGeneration = 1,
            branchId = "b1",
            causeKey = "cause-1",
            intentSeq = 1,
            deadlineElapsedMs = 50000L,
            requiredTargets = setOf("audio.media"),
            actions = listOf(
                ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20)
            ),
            runtimeEpoch = coordinator.runtimeEpoch
        )

        val report = coordinator.executePlan(plan)
        assertEquals(RuleExecutionSummary.SKIPPED, report.summary)
        assertEquals(0, effect.executedActions.size)
        assertEquals(ActionExecutionStatus.EXPIRED, report.actionResults[0].status)
    }

    @Test
    fun supersededPlanAbortsWithoutExecutingSetter() = runBlocking {
        coordinator.supersedeTargets(setOf("audio.media"))
        val plan = ExecutionPlan(
            planId = "p1",
            ruleId = "r1",
            ruleRevision = 1,
            activationGeneration = 1,
            branchId = "b1",
            causeKey = "cause-1",
            intentSeq = 0,
            deadlineElapsedMs = 50000L,
            requiredTargets = setOf("audio.media"),
            actions = listOf(
                ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20)
            ),
            runtimeEpoch = coordinator.runtimeEpoch
        )

        val report = coordinator.executePlan(plan)
        assertEquals(RuleExecutionSummary.SKIPPED, report.summary)
        assertEquals(0, effect.executedActions.size)
        assertEquals(ActionExecutionStatus.SUPERSEDED, report.actionResults[0].status)
    }

    @Test
    fun blockedCapabilityPreventsSetterExecution() = runBlocking {
        capability.status = CapabilityStatus.Blocked("Background audio not permitted")
        val plan = ExecutionPlan(
            planId = "p1",
            ruleId = "r1",
            ruleRevision = 1,
            activationGeneration = 1,
            branchId = "b1",
            causeKey = "cause-1",
            intentSeq = 1,
            deadlineElapsedMs = 50000L,
            requiredTargets = setOf("audio.media"),
            actions = listOf(
                ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20)
            ),
            runtimeEpoch = coordinator.runtimeEpoch
        )

        val report = coordinator.executePlan(plan)
        assertEquals(RuleExecutionSummary.BLOCKED, report.summary)
        assertEquals(0, effect.executedActions.size)
        assertEquals(ActionExecutionStatus.BLOCKED, report.actionResults[0].status)
    }

    @Test
    fun executeTestActionRunsDirectlyWithJournal() = runBlocking {
        val action = ActionDefinition(actionId = "a-test", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 15)
        val result = coordinator.executeTestAction(action, "cmd-999")

        assertEquals(ActionExecutionStatus.APPLIED, result.status)
        assertEquals(1, effect.executedActions.size)
        assertEquals("a-test", effect.executedActions[0].actionId)
    }

    @Test
    fun onLogicalEventExecutesMatchedActiveRule() = runBlocking {
        val rule = RuleDefinition(
            ruleId = "r-tws",
            name = "TWS Volume",
            mode = RuleMode.SIMPLE,
            trigger = TriggerDefinition(
                family = TriggerFamily.BLUETOOTH,
                kind = TriggerKind.BLUETOOTH_CONNECTED,
                deviceId = KronoFixtures.TWS_DEVICE_ID
            ),
            branches = listOf(
                BranchDefinition(
                    branchId = "b1",
                    kind = BranchKind.ALWAYS,
                    condition = ConditionPredicate(kind = PredicateKind.ALWAYS),
                    actions = listOf(
                        ActionDefinition(actionId = "a-vol", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20)
                    )
                )
            )
        )

        store.saveRule(rule)
        store.setRuleActive("r-tws", true)

        val event = LogicalEvent(
            eventId = "ev-1",
            eventKey = "ek-1",
            eventKind = "BLUETOOTH_CONNECTED",
            causeKey = "cause-bt-1",
            receivedElapsedMs = 1000L,
            receivedAtUtcMs = 1000L,
            sourceId = KronoFixtures.TWS_DEVICE_ID,
            sourceFamily = "BLUETOOTH"
        )

        val snapshot = EvaluationSnapshot(
            bluetoothStates = mapOf(KronoFixtures.TWS_DEVICE_ID to BluetoothConnectionState.CONNECTED),
            nowElapsedMs = 1000L,
            nowUtcMs = 1000L
        )

        val reports = coordinator.onLogicalEvent(event, snapshot)
        assertEquals(1, reports.size)
        assertEquals("r-tws", reports[0].ruleId)
        assertEquals(RuleExecutionSummary.SUCCESS, reports[0].summary)
        assertEquals(1, effect.executedActions.size)
    }

    @Test
    fun onLogicalEventDeduplicatesConcurrentLocationReportsFromMultipleSources() = runBlocking {
        val rule = RuleDefinition(
            ruleId = "r-loc",
            name = "Location Rule",
            mode = RuleMode.BRANCHED,
            trigger = TriggerDefinition(
                family = TriggerFamily.LOCATION,
                kind = TriggerKind.GEOFENCE_MEMBERSHIP_CHANGED,
                areaIds = listOf("area-1")
            ),
            branches = listOf(
                BranchDefinition(
                    branchId = "b1",
                    kind = BranchKind.IF,
                    condition = ConditionPredicate(
                        kind = PredicateKind.LOCATION_IN_AREA,
                        areaId = "area-1"
                    ),
                    actions = listOf(
                        ActionDefinition(actionId = "a-wall", kind = ActionKind.SET_WALLPAPER, assetId = "asset-1")
                    )
                )
            )
        )

        store.saveRule(rule)
        store.setRuleActive("r-loc", true)

        val snapshotInside = EvaluationSnapshot(
            areaStates = mapOf("area-1" to ConditionResult.True),
            nowElapsedMs = 1000L,
            nowUtcMs = 1000L
        )

        val eventFromFused = LogicalEvent(
            eventId = "ev-fused-1",
            eventKey = "ek-fused-1",
            eventKind = "GEOFENCE_MEMBERSHIP_CHANGED",
            causeKey = "cause-loc-area-1:true",
            receivedElapsedMs = 1000L,
            receivedAtUtcMs = 1000L,
            sourceId = "area-1",
            sourceFamily = "LOCATION"
        )

        val eventFromGeofence = LogicalEvent(
            eventId = "ev-geo-1",
            eventKey = "ek-geo-1",
            eventKind = "GEOFENCE_MEMBERSHIP_CHANGED",
            causeKey = "cause-loc-area-1:true",
            receivedElapsedMs = 1025L,
            receivedAtUtcMs = 1025L,
            sourceId = "area-1",
            sourceFamily = "LOCATION"
        )

        // Event 1 from Fused
        val reports1 = coordinator.onLogicalEvent(eventFromFused, snapshotInside)
        assertEquals(1, reports1.size)
        assertEquals(1, effect.executedActions.size)
        assertEquals("a-wall", effect.executedActions[0].actionId)

        // Event 2 from Geofence 25ms later in the same episode
        val reports2 = coordinator.onLogicalEvent(eventFromGeofence, snapshotInside)
        assertEquals(0, reports2.size) // Deduplicated at arbitration/episode layer!
        assertEquals(1, effect.executedActions.size) // Effect NOT called a second time!

        // Device exits area
        val snapshotOutside = EvaluationSnapshot(
            areaStates = mapOf("area-1" to ConditionResult.False),
            nowElapsedMs = 2000L,
            nowUtcMs = 2000L
        )
        val eventExit = LogicalEvent(
            eventId = "ev-exit-1",
            eventKey = "ek-exit-1",
            eventKind = "GEOFENCE_MEMBERSHIP_CHANGED",
            causeKey = "cause-loc-area-1:false",
            receivedElapsedMs = 2000L,
            receivedAtUtcMs = 2000L,
            sourceId = "area-1",
            sourceFamily = "LOCATION"
        )
        val reportsExit = coordinator.onLogicalEvent(eventExit, snapshotOutside)
        assertEquals(0, reportsExit.size)
        assertEquals(1, effect.executedActions.size)

        // Device re-enters area in new episode
        val snapshotReEnter = EvaluationSnapshot(
            areaStates = mapOf("area-1" to ConditionResult.True),
            nowElapsedMs = 3000L,
            nowUtcMs = 3000L
        )
        val eventReEnter = LogicalEvent(
            eventId = "ev-reenter-1",
            eventKey = "ek-reenter-1",
            eventKind = "GEOFENCE_MEMBERSHIP_CHANGED",
            causeKey = "cause-loc-area-1:true",
            receivedElapsedMs = 3000L,
            receivedAtUtcMs = 3000L,
            sourceId = "area-1",
            sourceFamily = "LOCATION"
        )
        val reportsReEnter = coordinator.onLogicalEvent(eventReEnter, snapshotReEnter)
        assertEquals(1, reportsReEnter.size)
        assertEquals(2, effect.executedActions.size) // Executed for the second visit!
    }
}
