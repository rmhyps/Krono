package com.krono.core.validation

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

class RuleValidatorTest {

    @Test
    fun validSimpleTwsRulePasses() {
        val rule = RuleDefinition(
            ruleId = "rule-tws",
            name = "TWS Auto Volume",
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
                            actionId = "a1",
                            kind = ActionKind.SET_MEDIA_VOLUME,
                            volumePercent = 20
                        )
                    )
                )
            )
        )

        val result = RuleValidator.validate(rule)
        assertTrue(result is RuleValidationResult.Valid)
    }

    @Test
    fun validBranchedLocationRulePasses() {
        val rule = RuleDefinition(
            ruleId = "rule-loc",
            name = "Campus Wallpaper",
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

        val result = RuleValidator.validate(rule)
        assertTrue(result is RuleValidationResult.Valid)
    }

    @Test
    fun duplicateActionIdFails() {
        val rule = RuleDefinition(
            ruleId = "rule-dup",
            name = "Duplicate Actions",
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
                        ActionDefinition(actionId = "dup-id", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20),
                        ActionDefinition(actionId = "dup-id", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 30)
                    )
                )
            )
        )

        val result = RuleValidator.validate(rule)
        assertTrue(result is RuleValidationResult.Invalid)
        val invalid = result as RuleValidationResult.Invalid
        assertTrue(invalid.errors.any { it.errorCode == "DUPLICATE_ACTION_ID" })
    }

    @Test
    fun invalidVolumePercentFails() {
        val rule = RuleDefinition(
            ruleId = "rule-vol",
            name = "Bad Volume",
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
                        ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 150)
                    )
                )
            )
        )

        val result = RuleValidator.validate(rule)
        assertTrue(result is RuleValidationResult.Invalid)
        val invalid = result as RuleValidationResult.Invalid
        assertEquals("INVALID_VOLUME_PERCENT", invalid.errors[0].errorCode)
    }

    @Test
    fun areaMismatchWithTriggerFails() {
        val rule = RuleDefinition(
            ruleId = "rule-loc-mismatch",
            name = "Campus Wallpaper",
            mode = RuleMode.BRANCHED,
            trigger = TriggerDefinition(
                family = TriggerFamily.LOCATION,
                kind = TriggerKind.GEOFENCE_MEMBERSHIP_CHANGED,
                areaIds = listOf(KronoFixtures.AREA_A_ID)
            ),
            branches = listOf(
                BranchDefinition(
                    branchId = "branch-b",
                    kind = BranchKind.IF,
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

        val result = RuleValidator.validate(rule)
        assertTrue(result is RuleValidationResult.Invalid)
        val invalid = result as RuleValidationResult.Invalid
        assertTrue(invalid.errors.any { it.errorCode == "UNREGISTERED_AREA_ID" })
    }
}
