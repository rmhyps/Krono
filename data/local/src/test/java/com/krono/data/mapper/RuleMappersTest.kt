package com.krono.data.mapper

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
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleMappersTest {

    @Test
    fun bidirectionalMappingPreservesRuleDefinition() {
        val originalRule = RuleDefinition(
            ruleId = "rule-tws-1",
            revision = 1,
            name = "Auto Volume",
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
                        ActionDefinition(
                            actionId = "act-1",
                            kind = ActionKind.SET_MEDIA_VOLUME,
                            volumePercent = 25
                        )
                    )
                )
            )
        )

        val (revisionEntity, details) = RuleMappers.toEntities(originalRule, 1000L)
        val (branches, actions, sources) = details

        val reconstructed = RuleMappers.toDomain(revisionEntity, branches, actions, sources)

        assertEquals(originalRule.ruleId, reconstructed.ruleId)
        assertEquals(originalRule.name, reconstructed.name)
        assertEquals(originalRule.mode, reconstructed.mode)
        assertEquals(originalRule.trigger.family, reconstructed.trigger.family)
        assertEquals(originalRule.trigger.deviceId, reconstructed.trigger.deviceId)
        assertEquals(1, reconstructed.branches.size)
        assertEquals("b1", reconstructed.branches[0].branchId)
        assertEquals(25, reconstructed.branches[0].actions[0].volumePercent)
    }
}
