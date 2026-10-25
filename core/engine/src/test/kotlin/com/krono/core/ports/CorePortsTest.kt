package com.krono.core.ports

import com.krono.core.fixtures.KronoFixtures
import com.krono.core.model.ActionDefinition
import com.krono.core.model.ActionKind
import com.krono.core.model.RuleDefinition
import com.krono.core.model.RuleMode
import com.krono.core.model.TriggerDefinition
import com.krono.core.model.TriggerFamily
import com.krono.core.model.TriggerKind
import com.krono.core.result.ActionExecutionResult
import com.krono.core.result.ActionExecutionStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CorePortsTest {

    private class FakeClock(var elapsed: Long = 1000L, var utc: Long = 1000L) : ClockPort {
        override fun nowElapsedMs(): Long = elapsed
        override fun nowUtcMs(): Long = utc
    }

    private class FakeCapability(var status: CapabilityStatus = CapabilityStatus.Ready) : CapabilityPort {
        override fun checkCapability(action: ActionDefinition): CapabilityStatus = status
    }

    private class FakeEffect : EffectPort {
        val executed = mutableListOf<ActionDefinition>()
        override suspend fun executeAction(action: ActionDefinition): ActionExecutionResult {
            executed.add(action)
            return ActionExecutionResult(actionId = action.actionId, status = ActionExecutionStatus.APPLIED)
        }
    }

    private class FakeStore : AutomationStore {
        private val rules = mutableMapOf<String, RuleDefinition>()
        private val active = mutableMapOf<String, Boolean>()
        private val priority = mutableListOf<String>()

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

    @Test
    fun fakeClockReturnsConfiguredTime() {
        val clock = FakeClock(2500L, 5000L)
        assertEquals(2500L, clock.nowElapsedMs())
        assertEquals(5000L, clock.nowUtcMs())
    }

    @Test
    fun fakeCapabilityReturnsConfiguredStatus() {
        val capability = FakeCapability(CapabilityStatus.Ready)
        val action = ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20)
        assertTrue(capability.checkCapability(action) is CapabilityStatus.Ready)

        capability.status = CapabilityStatus.Blocked("No audio permission")
        val blocked = capability.checkCapability(action)
        assertTrue(blocked is CapabilityStatus.Blocked)
        assertEquals("No audio permission", (blocked as CapabilityStatus.Blocked).reason)
    }

    @Test
    fun fakeEffectExecutesAndRecordsAction() = runBlocking {
        val effect = FakeEffect()
        val action = ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20)
        val result = effect.executeAction(action)

        assertEquals(ActionExecutionStatus.APPLIED, result.status)
        assertEquals(1, effect.executed.size)
        assertEquals("a1", effect.executed[0].actionId)
    }

    @Test
    fun fakeStoreManagesRulesAndPriority() = runBlocking {
        val store = FakeStore()
        val rule = RuleDefinition(
            ruleId = "r1",
            name = "Test Rule",
            mode = RuleMode.SIMPLE,
            trigger = TriggerDefinition(
                family = TriggerFamily.BLUETOOTH,
                kind = TriggerKind.BLUETOOTH_CONNECTED,
                deviceId = KronoFixtures.TWS_DEVICE_ID
            ),
            branches = emptyList()
        )

        store.saveRule(rule)
        store.setRuleActive("r1", true)

        val active = store.getActiveRules()
        assertEquals(1, active.size)
        assertEquals("r1", active[0].ruleId)

        val priority = store.getRulePriorityList()
        assertEquals(listOf("r1"), priority)
    }
}
