package com.krono.app.integration

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
import com.krono.core.validation.RuleValidationResult
import com.krono.core.validation.RuleValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicRuleCrudTest {

    @Test
    fun userCanCreateRuleWithMultipleDynamicBranchesAndActions() {
        val branch1 = BranchDefinition(
            branchId = "branch-if-campus-a",
            kind = BranchKind.IF,
            condition = ConditionPredicate(
                kind = PredicateKind.LOCATION_IN_AREA,
                areaId = "area-custom-1"
            ),
            actions = listOf(
                ActionDefinition(actionId = "act-vol-1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 10),
                ActionDefinition(actionId = "act-wall-1", kind = ActionKind.SET_WALLPAPER, assetId = "wallpaper-1", wallpaperTarget = WallpaperTarget.SYSTEM)
            )
        )

        val branch2 = BranchDefinition(
            branchId = "branch-elseif-campus-b",
            kind = BranchKind.ELSE_IF,
            condition = ConditionPredicate(
                kind = PredicateKind.LOCATION_IN_AREA,
                areaId = "area-custom-2"
            ),
            actions = listOf(
                ActionDefinition(actionId = "act-vol-2", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 40)
            )
        )

        val branch3 = BranchDefinition(
            branchId = "branch-elseif-campus-c",
            kind = BranchKind.ELSE_IF,
            condition = ConditionPredicate(
                kind = PredicateKind.LOCATION_IN_AREA,
                areaId = "area-custom-3"
            ),
            actions = listOf(
                ActionDefinition(actionId = "act-wall-3", kind = ActionKind.SET_WALLPAPER, assetId = "wallpaper-3", wallpaperTarget = WallpaperTarget.BOTH)
            )
        )

        val elseBranch = BranchDefinition(
            branchId = "branch-else-fallback",
            kind = BranchKind.ELSE,
            condition = ConditionPredicate(kind = PredicateKind.NONE),
            actions = listOf(
                ActionDefinition(actionId = "act-else-vol", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 100)
            )
        )

        val rule = RuleDefinition(
            ruleId = "user-custom-rule",
            name = "Kustom Pengguna 3 Cabang + ELSE",
            mode = RuleMode.BRANCHED,
            trigger = TriggerDefinition(
                family = TriggerFamily.LOCATION,
                kind = TriggerKind.GEOFENCE_MEMBERSHIP_CHANGED,
                areaIds = listOf("area-custom-1", "area-custom-2", "area-custom-3")
            ),
            branches = listOf(branch1, branch2, branch3),
            elseBranch = elseBranch
        )

        val validation = RuleValidator.validate(rule)
        assertTrue(validation is RuleValidationResult.Valid)
        assertEquals(3, rule.branches.size)
        assertEquals(2, rule.branches[0].actions.size)
        assertEquals(1, rule.elseBranch?.actions?.size)
    }

    @Test
    fun userCanDeleteBranchesLeavingSingleBranchRule() {
        val branchesList = mutableListOf(
            BranchDefinition(
                branchId = "b1",
                kind = BranchKind.IF,
                condition = ConditionPredicate(kind = PredicateKind.LOCATION_IN_AREA, areaId = KronoFixtures.AREA_A_ID),
                actions = listOf(ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20))
            ),
            BranchDefinition(
                branchId = "b2",
                kind = BranchKind.ELSE_IF,
                condition = ConditionPredicate(kind = PredicateKind.LOCATION_IN_AREA, areaId = KronoFixtures.AREA_B_ID),
                actions = listOf(ActionDefinition(actionId = "a2", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 30))
            )
        )

        branchesList.removeAt(1)
        assertEquals(1, branchesList.size)
        assertEquals("b1", branchesList[0].branchId)
    }

    @Test
    fun userCanReorderBranches() {
        val branchA = BranchDefinition(
            branchId = "branch-a",
            kind = BranchKind.IF,
            condition = ConditionPredicate(kind = PredicateKind.LOCATION_IN_AREA, areaId = KronoFixtures.AREA_A_ID),
            actions = listOf(ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20))
        )
        val branchB = BranchDefinition(
            branchId = "branch-b",
            kind = BranchKind.ELSE_IF,
            condition = ConditionPredicate(kind = PredicateKind.LOCATION_IN_AREA, areaId = KronoFixtures.AREA_B_ID),
            actions = listOf(ActionDefinition(actionId = "a2", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 50))
        )

        val branchesList = mutableListOf(branchA, branchB)
        val temp = branchesList[0]
        branchesList[0] = branchesList[1].copy(kind = BranchKind.IF)
        branchesList[1] = temp.copy(kind = BranchKind.ELSE_IF)

        assertEquals("branch-b", branchesList[0].branchId)
        assertEquals(BranchKind.IF, branchesList[0].kind)
        assertEquals("branch-a", branchesList[1].branchId)
        assertEquals(BranchKind.ELSE_IF, branchesList[1].kind)
    }

    @Test
    fun locationRulePreservesCustomCoordinates() {
        val customLat = -7.894303
        val customLng = 112.595458
        val customRadius = 250.0

        val branch = BranchDefinition(
            branchId = "branch-karangploso",
            kind = BranchKind.IF,
            condition = ConditionPredicate(
                kind = PredicateKind.LOCATION_IN_AREA,
                areaId = "area-karangploso",
                latitude = customLat,
                longitude = customLng,
                radiusM = customRadius
            ),
            actions = listOf(ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 50))
        )

        assertEquals(customLat, branch.condition.latitude ?: 0.0, 0.000001)
        assertEquals(customLng, branch.condition.longitude ?: 0.0, 0.000001)
        assertEquals(customRadius, branch.condition.radiusM ?: 0.0, 0.01)
    }
}
