package com.krono.runtime.plan

import com.krono.core.model.ActionDefinition

data class ExecutionPlan(
    val planId: String,
    val ruleId: String,
    val ruleRevision: Int,
    val activationGeneration: Int,
    val branchId: String,
    val causeKey: String,
    val intentSeq: Long,
    val deadlineElapsedMs: Long,
    val requiredTargets: Set<String>,
    val actions: List<ActionDefinition>,
    val runtimeEpoch: String
) {
    fun effectKeyForAction(actionId: String): String {
        return "$ruleId:$ruleRevision:$activationGeneration:$causeKey:$branchId:$actionId"
    }
}
