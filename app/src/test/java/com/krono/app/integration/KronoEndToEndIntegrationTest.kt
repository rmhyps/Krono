package com.krono.app.integration

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
import com.krono.core.model.WallpaperTarget
import com.krono.core.ports.CapabilityPort
import com.krono.core.ports.CapabilityStatus
import com.krono.core.ports.ClockPort
import com.krono.core.ports.EffectPort
import com.krono.core.ports.MonitoringPort
import com.krono.core.result.ActionExecutionResult
import com.krono.core.result.ActionExecutionStatus
import com.krono.core.result.RuleExecutionSummary
import com.krono.runtime.coordinator.KronoRuntimeCoordinator
import com.krono.runtime.ingress.LogicalEvent
import com.krono.runtime.journal.EffectJournalRecorder
import com.krono.runtime.recovery.RuntimeRecoveryHandler
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KronoEndToEndIntegrationTest {

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
        val executed = mutableListOf<ActionDefinition>()
        override suspend fun executeAction(action: ActionDefinition): ActionExecutionResult {
            executed.add(action)
            return ActionExecutionResult(
                actionId = action.actionId,
                status = ActionExecutionStatus.APPLIED,
                evidenceSummary = "Effect applied successfully on test runner"
            )
        }
    }

    private class TestStore : com.krono.core.ports.AutomationStore {
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
    fun endToEndTwsVolumeWorkflow() = runBlocking {
        val rule = RuleDefinition(
            ruleId = "r-tws",
            name = "TWS Volume Flow",
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
        assertEquals(RuleExecutionSummary.SUCCESS, reports[0].summary)
        assertEquals(1, effect.executed.size)
        assertEquals("a-vol", effect.executed[0].actionId)
        assertEquals(20, effect.executed[0].volumePercent)
    }

    @Test
    fun endToEndLocationUnknownStateDoesNotTriggerAction() = runBlocking {
        val rule = RuleDefinition(
            ruleId = "r-loc",
            name = "Campus Wallpaper Flow",
            mode = RuleMode.BRANCHED,
            trigger = TriggerDefinition(
                family = TriggerFamily.LOCATION,
                kind = TriggerKind.GEOFENCE_MEMBERSHIP_CHANGED,
                areaIds = listOf(KronoFixtures.AREA_A_ID, KronoFixtures.AREA_B_ID)
            ),
            branches = listOf(
                BranchDefinition(
                    branchId = "branch-a",
                    kind = BranchKind.IF,
                    condition = ConditionPredicate(
                        kind = PredicateKind.LOCATION_IN_AREA,
                        areaId = KronoFixtures.AREA_A_ID
                    ),
                    actions = listOf(
                        ActionDefinition(
                            actionId = "act-a",
                            kind = ActionKind.SET_WALLPAPER,
                            assetId = KronoFixtures.ASSET_IMAGE_A_ID,
                            wallpaperTarget = WallpaperTarget.SYSTEM
                        )
                    )
                ),
                BranchDefinition(
                    branchId = "branch-b",
                    kind = BranchKind.ELSE_IF,
                    condition = ConditionPredicate(
                        kind = PredicateKind.LOCATION_IN_AREA,
                        areaId = KronoFixtures.AREA_B_ID
                    ),
                    actions = listOf(
                        ActionDefinition(
                            actionId = "act-b",
                            kind = ActionKind.SET_WALLPAPER,
                            assetId = KronoFixtures.ASSET_IMAGE_B_ID,
                            wallpaperTarget = WallpaperTarget.SYSTEM
                        )
                    )
                )
            ),
            elseBranch = BranchDefinition(
                branchId = "branch-else",
                kind = BranchKind.ELSE,
                condition = ConditionPredicate(kind = PredicateKind.NONE),
                actions = listOf(
                    ActionDefinition(
                        actionId = "act-else",
                        kind = ActionKind.SET_WALLPAPER,
                        assetId = KronoFixtures.ASSET_IMAGE_A_ID,
                        wallpaperTarget = WallpaperTarget.SYSTEM
                    )
                )
            )
        )

        store.saveRule(rule)
        store.setRuleActive("r-loc", true)

        val event = LogicalEvent(
            eventId = "ev-loc-1",
            eventKey = "ek-loc-1",
            eventKind = "GEOFENCE_MEMBERSHIP_CHANGED",
            causeKey = "cause-loc-1",
            receivedElapsedMs = 1000L,
            receivedAtUtcMs = 1000L,
            sourceFamily = "LOCATION"
        )

        val unknownSnapshot = EvaluationSnapshot(
            areaStates = mapOf(
                KronoFixtures.AREA_A_ID to ConditionResult.Unknown("GPS weak indoor"),
                KronoFixtures.AREA_B_ID to ConditionResult.True
            ),
            nowElapsedMs = 1000L,
            nowUtcMs = 1000L
        )

        val reports = coordinator.onLogicalEvent(event, unknownSnapshot)
        assertEquals(0, reports.size)
        assertEquals(0, effect.executed.size)
    }

    @Test
    fun endToEndLocationMatchedBranchAExecutesOnlyBranchA() = runBlocking {
        val rule = RuleDefinition(
            ruleId = "r-loc",
            name = "Campus Wallpaper Flow",
            mode = RuleMode.BRANCHED,
            trigger = TriggerDefinition(
                family = TriggerFamily.LOCATION,
                kind = TriggerKind.GEOFENCE_MEMBERSHIP_CHANGED,
                areaIds = listOf(KronoFixtures.AREA_A_ID, KronoFixtures.AREA_B_ID)
            ),
            branches = listOf(
                BranchDefinition(
                    branchId = "branch-a",
                    kind = BranchKind.IF,
                    condition = ConditionPredicate(
                        kind = PredicateKind.LOCATION_IN_AREA,
                        areaId = KronoFixtures.AREA_A_ID
                    ),
                    actions = listOf(
                        ActionDefinition(
                            actionId = "act-a",
                            kind = ActionKind.SET_WALLPAPER,
                            assetId = KronoFixtures.ASSET_IMAGE_A_ID,
                            wallpaperTarget = WallpaperTarget.SYSTEM
                        )
                    )
                ),
                BranchDefinition(
                    branchId = "branch-b",
                    kind = BranchKind.ELSE_IF,
                    condition = ConditionPredicate(
                        kind = PredicateKind.LOCATION_IN_AREA,
                        areaId = KronoFixtures.AREA_B_ID
                    ),
                    actions = listOf(
                        ActionDefinition(
                            actionId = "act-b",
                            kind = ActionKind.SET_WALLPAPER,
                            assetId = KronoFixtures.ASSET_IMAGE_B_ID,
                            wallpaperTarget = WallpaperTarget.SYSTEM
                        )
                    )
                )
            )
        )

        store.saveRule(rule)
        store.setRuleActive("r-loc", true)

        val event = LogicalEvent(
            eventId = "ev-loc-2",
            eventKey = "ek-loc-2",
            eventKind = "GEOFENCE_MEMBERSHIP_CHANGED",
            causeKey = "cause-loc-2",
            receivedElapsedMs = 2000L,
            receivedAtUtcMs = 2000L,
            sourceFamily = "LOCATION"
        )

        val aMatchesSnapshot = EvaluationSnapshot(
            areaStates = mapOf(
                KronoFixtures.AREA_A_ID to ConditionResult.True,
                KronoFixtures.AREA_B_ID to ConditionResult.False
            ),
            nowElapsedMs = 2000L,
            nowUtcMs = 2000L
        )

        val reports = coordinator.onLogicalEvent(event, aMatchesSnapshot)
        assertEquals(1, reports.size)
        assertEquals(RuleExecutionSummary.SUCCESS, reports[0].summary)
        assertEquals(1, effect.executed.size)
        assertEquals("act-a", effect.executed[0].actionId)
        assertEquals(KronoFixtures.ASSET_IMAGE_A_ID, effect.executed[0].assetId)
    }
}
