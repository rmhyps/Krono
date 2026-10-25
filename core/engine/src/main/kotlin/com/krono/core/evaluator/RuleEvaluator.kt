package com.krono.core.evaluator

import com.krono.core.model.ConditionPredicate
import com.krono.core.model.PredicateKind
import com.krono.core.model.RuleDefinition

object RuleEvaluator {

    fun evaluatePredicate(predicate: ConditionPredicate, snapshot: EvaluationSnapshot): ConditionResult {
        return when (predicate.kind) {
            PredicateKind.ALWAYS -> ConditionResult.True
            PredicateKind.NONE -> ConditionResult.False
            PredicateKind.LOCATION_IN_AREA -> {
                val areaId = predicate.areaId ?: return ConditionResult.Unknown("Area ID missing")
                snapshot.areaStates[areaId] ?: ConditionResult.Unknown("Area state not observed")
            }
            PredicateKind.BLUETOOTH_IS_CONNECTED -> {
                val deviceId = predicate.deviceId ?: return ConditionResult.Unknown("Device ID missing")
                when (snapshot.bluetoothStates[deviceId]) {
                    BluetoothConnectionState.CONNECTED -> ConditionResult.True
                    BluetoothConnectionState.DISCONNECTED -> ConditionResult.False
                    BluetoothConnectionState.UNKNOWN, null -> ConditionResult.Unknown("Bluetooth state not observed")
                }
            }
            PredicateKind.BLUETOOTH_IS_DISCONNECTED -> {
                val deviceId = predicate.deviceId ?: return ConditionResult.Unknown("Device ID missing")
                when (snapshot.bluetoothStates[deviceId]) {
                    BluetoothConnectionState.DISCONNECTED -> ConditionResult.True
                    BluetoothConnectionState.CONNECTED -> ConditionResult.False
                    BluetoothConnectionState.UNKNOWN, null -> ConditionResult.Unknown("Bluetooth state not observed")
                }
            }
        }
    }

    fun selectBranch(rule: RuleDefinition, snapshot: EvaluationSnapshot): BranchSelectionResult {
        for (branch in rule.branches) {
            val result = evaluatePredicate(branch.condition, snapshot)
            when (result) {
                is ConditionResult.Unknown -> return BranchSelectionResult.WaitingData(result.reason)
                is ConditionResult.True -> return BranchSelectionResult.Match(branch)
                is ConditionResult.False -> Unit
            }
        }
        val elseBranch = rule.elseBranch
        if (elseBranch != null) {
            return BranchSelectionResult.Match(elseBranch)
        }
        return BranchSelectionResult.NoMatch
    }
}
