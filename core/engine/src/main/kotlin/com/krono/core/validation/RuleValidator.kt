package com.krono.core.validation

import com.krono.core.model.ActionKind
import com.krono.core.model.BranchDefinition
import com.krono.core.model.BranchKind
import com.krono.core.model.PredicateKind
import com.krono.core.model.RuleDefinition
import com.krono.core.model.RuleMode
import com.krono.core.model.TriggerFamily
import com.krono.core.model.TriggerKind

object RuleValidator {

    fun validate(rule: RuleDefinition): RuleValidationResult {
        val errors = mutableListOf<ValidationError>()

        if (rule.ruleId.isBlank()) {
            errors.add(ValidationError("ruleId", "BLANK_RULE_ID", "Rule ID must not be blank"))
        }

        if (rule.name.isBlank()) {
            errors.add(ValidationError("name", "BLANK_NAME", "Rule name must not be blank"))
        }

        validateTrigger(rule, errors)

        val allBranches = mutableListOf<BranchDefinition>()
        allBranches.addAll(rule.branches)
        rule.elseBranch?.let { allBranches.add(it) }

        val branchIds = mutableSetOf<String>()
        for (branch in allBranches) {
            if (branch.branchId.isBlank()) {
                errors.add(ValidationError("branchId", "BLANK_BRANCH_ID", "Branch ID must not be blank"))
            } else if (!branchIds.add(branch.branchId)) {
                errors.add(ValidationError("branchId", "DUPLICATE_BRANCH_ID", "Duplicate branch ID: ${branch.branchId}"))
            }
        }

        val actionIds = mutableSetOf<String>()
        for (branch in allBranches) {
            for (action in branch.actions) {
                if (action.actionId.isBlank()) {
                    errors.add(ValidationError("actionId", "BLANK_ACTION_ID", "Action ID must not be blank"))
                } else if (!actionIds.add(action.actionId)) {
                    errors.add(ValidationError("actionId", "DUPLICATE_ACTION_ID", "Duplicate action ID: ${action.actionId}"))
                }
                validateAction(action, errors)
            }
        }

        when (rule.mode) {
            RuleMode.SIMPLE -> validateSimpleRule(rule, errors)
            RuleMode.BRANCHED -> validateBranchedRule(rule, errors)
        }

        return if (errors.isEmpty()) {
            RuleValidationResult.Valid
        } else {
            RuleValidationResult.Invalid(errors)
        }
    }

    private fun validateTrigger(rule: RuleDefinition, errors: MutableList<ValidationError>) {
        val trigger = rule.trigger
        when (trigger.family) {
            TriggerFamily.LOCATION -> {
                if (trigger.kind != TriggerKind.GEOFENCE_MEMBERSHIP_CHANGED) {
                    errors.add(ValidationError("trigger.kind", "INVALID_TRIGGER_KIND", "Location trigger must be GEOFENCE_MEMBERSHIP_CHANGED"))
                }
                if (trigger.areaIds.isEmpty()) {
                    errors.add(ValidationError("trigger.areaIds", "EMPTY_AREA_IDS", "Location trigger must specify at least one area"))
                }
                if (trigger.deviceId != null) {
                    errors.add(ValidationError("trigger.deviceId", "UNEXPECTED_DEVICE_ID", "Location trigger must not specify deviceId"))
                }
            }
            TriggerFamily.BLUETOOTH -> {
                val validKinds = setOf(
                    TriggerKind.BLUETOOTH_CONNECTED,
                    TriggerKind.BLUETOOTH_DISCONNECTED,
                    TriggerKind.BLUETOOTH_STATE_CHANGED
                )
                if (trigger.kind !in validKinds) {
                    errors.add(ValidationError("trigger.kind", "INVALID_TRIGGER_KIND", "Invalid Bluetooth trigger kind: ${trigger.kind}"))
                }
                if (trigger.deviceId.isNullOrBlank()) {
                    errors.add(ValidationError("trigger.deviceId", "BLANK_DEVICE_ID", "Bluetooth trigger must specify deviceId"))
                }
                if (trigger.areaIds.isNotEmpty()) {
                    errors.add(ValidationError("trigger.areaIds", "UNEXPECTED_AREA_IDS", "Bluetooth trigger must not specify areaIds"))
                }
            }
            TriggerFamily.WIFI -> {
                val validKinds = setOf(
                    TriggerKind.WIFI_CONNECTED,
                    TriggerKind.WIFI_DISCONNECTED,
                    TriggerKind.WIFI_STATE_CHANGED
                )
                if (trigger.kind !in validKinds) {
                    errors.add(ValidationError("trigger.kind", "INVALID_TRIGGER_KIND", "Invalid Wi-Fi trigger kind: ${trigger.kind}"))
                }
                if (trigger.wifiSsid.isNullOrBlank()) {
                    errors.add(ValidationError("trigger.wifiSsid", "BLANK_WIFI_SSID", "Wi-Fi trigger must specify wifiSsid"))
                }
                if (trigger.areaIds.isNotEmpty()) {
                    errors.add(ValidationError("trigger.areaIds", "UNEXPECTED_AREA_IDS", "Wi-Fi trigger must not specify areaIds"))
                }
            }
        }
    }

    private fun validateSimpleRule(rule: RuleDefinition, errors: MutableList<ValidationError>) {
        if (rule.branches.size != 1) {
            errors.add(ValidationError("branches", "SIMPLE_RULE_BRANCH_COUNT", "Simple rule must have exactly one branch"))
            return
        }
        val branch = rule.branches[0]
        if (branch.kind != BranchKind.ALWAYS) {
            errors.add(ValidationError("branches[0].kind", "INVALID_BRANCH_KIND", "Simple rule branch must be ALWAYS"))
        }
        if (branch.condition.kind != PredicateKind.ALWAYS) {
            errors.add(ValidationError("branches[0].condition", "INVALID_CONDITION_KIND", "Simple rule condition must be ALWAYS"))
        }
        if (branch.actions.isEmpty()) {
            errors.add(ValidationError("branches[0].actions", "EMPTY_ACTIONS", "Simple rule branch must have at least one action"))
        }
        if (rule.elseBranch != null) {
            errors.add(ValidationError("elseBranch", "UNEXPECTED_ELSE_BRANCH", "Simple rule must not have an elseBranch"))
        }
    }

    private fun validateBranchedRule(rule: RuleDefinition, errors: MutableList<ValidationError>) {
        if (rule.branches.isEmpty()) {
            errors.add(ValidationError("branches", "EMPTY_BRANCHES", "Branched rule must have at least one branch"))
            return
        }

        rule.branches.forEachIndexed { index, branch ->
            if (index == 0) {
                if (branch.kind != BranchKind.IF) {
                    errors.add(ValidationError("branches[0].kind", "INVALID_BRANCH_KIND", "First branch of branched rule must be IF"))
                }
            } else {
                if (branch.kind != BranchKind.ELSE_IF) {
                    errors.add(ValidationError("branches[$index].kind", "INVALID_BRANCH_KIND", "Subsequent branch must be ELSE_IF"))
                }
            }

            if (branch.actions.isEmpty()) {
                errors.add(ValidationError("branches[$index].actions", "EMPTY_ACTIONS", "Branch ${branch.branchId} must have at least one action"))
            }

            validatePredicate(branch.condition, rule.trigger.family, rule.trigger.areaIds, rule.trigger.deviceId, errors, "branches[$index].condition")
        }

        rule.elseBranch?.let { elseB ->
            if (elseB.kind != BranchKind.ELSE) {
                errors.add(ValidationError("elseBranch.kind", "INVALID_BRANCH_KIND", "elseBranch must have kind ELSE"))
            }
            if (elseB.condition.kind != PredicateKind.NONE) {
                errors.add(ValidationError("elseBranch.condition", "INVALID_CONDITION_KIND", "elseBranch condition must have kind NONE"))
            }
            if (elseB.actions.isEmpty()) {
                errors.add(ValidationError("elseBranch.actions", "EMPTY_ACTIONS", "elseBranch must have at least one action"))
            }
        }
    }

    private fun validatePredicate(
        condition: com.krono.core.model.ConditionPredicate,
        family: TriggerFamily,
        areaIds: List<String>,
        deviceId: String?,
        errors: MutableList<ValidationError>,
        fieldPrefix: String
    ) {
        when (condition.kind) {
            PredicateKind.LOCATION_IN_AREA -> {
                if (family != TriggerFamily.LOCATION) {
                    errors.add(ValidationError(fieldPrefix, "FAMILY_MISMATCH", "LOCATION_IN_AREA condition used with non-location trigger"))
                }
                if (condition.areaId.isNullOrBlank()) {
                    errors.add(ValidationError("$fieldPrefix.areaId", "BLANK_AREA_ID", "Area ID must not be blank"))
                } else if (condition.areaId !in areaIds) {
                    errors.add(ValidationError("$fieldPrefix.areaId", "UNREGISTERED_AREA_ID", "Area ${condition.areaId} not registered in trigger"))
                }
                if (condition.deviceId != null) {
                    errors.add(ValidationError("$fieldPrefix.deviceId", "UNEXPECTED_DEVICE_ID", "Location condition must not specify deviceId"))
                }
            }
            PredicateKind.BLUETOOTH_IS_CONNECTED, PredicateKind.BLUETOOTH_IS_DISCONNECTED -> {
                if (family != TriggerFamily.BLUETOOTH) {
                    errors.add(ValidationError(fieldPrefix, "FAMILY_MISMATCH", "Bluetooth condition used with non-Bluetooth trigger"))
                }
                if (condition.deviceId.isNullOrBlank()) {
                    errors.add(ValidationError("$fieldPrefix.deviceId", "BLANK_DEVICE_ID", "Device ID must not be blank"))
                } else if (condition.deviceId != deviceId) {
                    errors.add(ValidationError("$fieldPrefix.deviceId", "UNREGISTERED_DEVICE_ID", "Device ${condition.deviceId} does not match trigger device"))
                }
                if (condition.areaId != null) {
                    errors.add(ValidationError("$fieldPrefix.areaId", "UNEXPECTED_AREA_ID", "Bluetooth condition must not specify areaId"))
                }
            }
            PredicateKind.WIFI_IS_CONNECTED, PredicateKind.WIFI_IS_DISCONNECTED -> {
                if (family != TriggerFamily.WIFI) {
                    errors.add(ValidationError(fieldPrefix, "FAMILY_MISMATCH", "Wi-Fi condition used with non-Wi-Fi trigger"))
                }
                if (condition.wifiSsid.isNullOrBlank()) {
                    errors.add(ValidationError("$fieldPrefix.wifiSsid", "BLANK_WIFI_SSID", "Wi-Fi SSID must not be blank"))
                }
            }
            PredicateKind.ALWAYS, PredicateKind.NONE -> {
                if (condition.areaId != null || condition.deviceId != null) {
                    errors.add(ValidationError(fieldPrefix, "UNEXPECTED_PREDICATE_TARGET", "ALWAYS/NONE must not specify target"))
                }
            }
        }
    }

    private fun validateAction(action: com.krono.core.model.ActionDefinition, errors: MutableList<ValidationError>) {
        when (action.kind) {
            ActionKind.SET_MEDIA_VOLUME -> {
                val percent = action.volumePercent
                if (percent == null || percent !in 0..100) {
                    errors.add(ValidationError("action.${action.actionId}.volumePercent", "INVALID_VOLUME_PERCENT", "Volume percent must be between 0 and 100"))
                }
                if (action.assetId != null) {
                    errors.add(ValidationError("action.${action.actionId}.assetId", "UNEXPECTED_ASSET_ID", "Volume action must not specify assetId"))
                }
                if (action.wallpaperTarget != null) {
                    errors.add(ValidationError("action.${action.actionId}.wallpaperTarget", "UNEXPECTED_WALLPAPER_TARGET", "Volume action must not specify wallpaperTarget"))
                }
            }
            ActionKind.SET_WALLPAPER -> {
                if (action.assetId.isNullOrBlank()) {
                    errors.add(ValidationError("action.${action.actionId}.assetId", "BLANK_ASSET_ID", "Wallpaper action must specify assetId"))
                }
                if (action.wallpaperTarget == null) {
                    errors.add(ValidationError("action.${action.actionId}.wallpaperTarget", "NULL_WALLPAPER_TARGET", "Wallpaper action must specify wallpaperTarget"))
                }
                if (action.volumePercent != null) {
                    errors.add(ValidationError("action.${action.actionId}.volumePercent", "UNEXPECTED_VOLUME_PERCENT", "Wallpaper action must not specify volumePercent"))
                }
            }
            ActionKind.SET_RINGER_MODE -> {
                if (action.ringerMode == null) {
                    errors.add(ValidationError("action.${action.actionId}.ringerMode", "NULL_RINGER_MODE", "Ringer mode action must specify ringerMode"))
                }
                if (action.assetId != null) {
                    errors.add(ValidationError("action.${action.actionId}.assetId", "UNEXPECTED_ASSET_ID", "Ringer mode action must not specify assetId"))
                }
                if (action.wallpaperTarget != null) {
                    errors.add(ValidationError("action.${action.actionId}.wallpaperTarget", "UNEXPECTED_WALLPAPER_TARGET", "Ringer mode action must not specify wallpaperTarget"))
                }
                if (action.volumePercent != null) {
                    errors.add(ValidationError("action.${action.actionId}.volumePercent", "UNEXPECTED_VOLUME_PERCENT", "Ringer mode action must not specify volumePercent"))
                }
            }
        }
    }
}
