package com.krono.core.arbitration

import com.krono.core.model.BranchDefinition

data class ArbitrationCandidate(
    val ruleId: String,
    val ruleRevision: Int,
    val activationGeneration: Int,
    val priorityPosition: Int,
    val branch: BranchDefinition,
    val isPreflightReady: Boolean = true
)

sealed class ArbitrationDecision {
    data class Winner(val candidate: ArbitrationCandidate, val reservedTargets: Set<String>) : ArbitrationDecision()
    data class ConflictLost(val candidate: ArbitrationCandidate, val conflictingTargets: Set<String>) : ArbitrationDecision()
    data class PreflightBlocked(val candidate: ArbitrationCandidate) : ArbitrationDecision()
}

data class BatchArbitrationResult(
    val winners: List<ArbitrationDecision.Winner>,
    val decisions: List<ArbitrationDecision>
)
