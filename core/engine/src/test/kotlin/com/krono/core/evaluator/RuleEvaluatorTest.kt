package com.krono.core.evaluator

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleEvaluatorTest {

    private fun createLocationRule(withElse: Boolean = true): RuleDefinition {
        return RuleDefinition(
            ruleId = "rule-loc-eval",
            name = "Campus Wallpaper Evaluator",
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
            elseBranch = if (withElse) {
                BranchDefinition(
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
            } else null
        )
    }

    @Test
    fun reT01_aTrueBTrue_selectsOnlyA() {
        val rule = createLocationRule()
        val snapshot = EvaluationSnapshot(
            areaStates = mapOf(
                KronoFixtures.AREA_A_ID to ConditionResult.True,
                KronoFixtures.AREA_B_ID to ConditionResult.True
            ),
            nowElapsedMs = 1000L,
            nowUtcMs = 1000L
        )

        val result = RuleEvaluator.selectBranch(rule, snapshot)
        assertTrue(result is BranchSelectionResult.Match)
        assertEquals("branch-a", (result as BranchSelectionResult.Match).branch.branchId)
    }

    @Test
    fun reT02_aFalseBTrue_selectsB() {
        val rule = createLocationRule()
        val snapshot = EvaluationSnapshot(
            areaStates = mapOf(
                KronoFixtures.AREA_A_ID to ConditionResult.False,
                KronoFixtures.AREA_B_ID to ConditionResult.True
            ),
            nowElapsedMs = 1000L,
            nowUtcMs = 1000L
        )

        val result = RuleEvaluator.selectBranch(rule, snapshot)
        assertTrue(result is BranchSelectionResult.Match)
        assertEquals("branch-b", (result as BranchSelectionResult.Match).branch.branchId)
    }

    @Test
    fun reT03_aFalseBFalseWithElse_selectsElse() {
        val rule = createLocationRule(withElse = true)
        val snapshot = EvaluationSnapshot(
            areaStates = mapOf(
                KronoFixtures.AREA_A_ID to ConditionResult.False,
                KronoFixtures.AREA_B_ID to ConditionResult.False
            ),
            nowElapsedMs = 1000L,
            nowUtcMs = 1000L
        )

        val result = RuleEvaluator.selectBranch(rule, snapshot)
        assertTrue(result is BranchSelectionResult.Match)
        assertEquals("branch-else", (result as BranchSelectionResult.Match).branch.branchId)
    }

    @Test
    fun reT04_aFalseBFalseWithoutElse_returnsNoMatch() {
        val rule = createLocationRule(withElse = false)
        val snapshot = EvaluationSnapshot(
            areaStates = mapOf(
                KronoFixtures.AREA_A_ID to ConditionResult.False,
                KronoFixtures.AREA_B_ID to ConditionResult.False
            ),
            nowElapsedMs = 1000L,
            nowUtcMs = 1000L
        )

        val result = RuleEvaluator.selectBranch(rule, snapshot)
        assertTrue(result is BranchSelectionResult.NoMatch)
    }

    @Test
    fun reT05_aUnknownBTrue_returnsWaitingDataAndDoesNotSelectB() {
        val rule = createLocationRule()
        val snapshot = EvaluationSnapshot(
            areaStates = mapOf(
                KronoFixtures.AREA_A_ID to ConditionResult.Unknown("GPS signal lost"),
                KronoFixtures.AREA_B_ID to ConditionResult.True
            ),
            nowElapsedMs = 1000L,
            nowUtcMs = 1000L
        )

        val result = RuleEvaluator.selectBranch(rule, snapshot)
        assertTrue(result is BranchSelectionResult.WaitingData)
        assertEquals("GPS signal lost", (result as BranchSelectionResult.WaitingData).reason)
    }

    @Test
    fun reT06_aFalseBUnknownWithElse_returnsWaitingDataAndDoesNotSelectElse() {
        val rule = createLocationRule(withElse = true)
        val snapshot = EvaluationSnapshot(
            areaStates = mapOf(
                KronoFixtures.AREA_A_ID to ConditionResult.False,
                KronoFixtures.AREA_B_ID to ConditionResult.Unknown("Indoor mode")
            ),
            nowElapsedMs = 1000L,
            nowUtcMs = 1000L
        )

        val result = RuleEvaluator.selectBranch(rule, snapshot)
        assertTrue(result is BranchSelectionResult.WaitingData)
        assertEquals("Indoor mode", (result as BranchSelectionResult.WaitingData).reason)
    }

    @Test
    fun reT07_aTrueBUnknown_selectsAWithoutNeedingB() {
        val rule = createLocationRule()
        val snapshot = EvaluationSnapshot(
            areaStates = mapOf(
                KronoFixtures.AREA_A_ID to ConditionResult.True,
                KronoFixtures.AREA_B_ID to ConditionResult.Unknown("Indoor mode")
            ),
            nowElapsedMs = 1000L,
            nowUtcMs = 1000L
        )

        val result = RuleEvaluator.selectBranch(rule, snapshot)
        assertTrue(result is BranchSelectionResult.Match)
        assertEquals("branch-a", (result as BranchSelectionResult.Match).branch.branchId)
    }
}
