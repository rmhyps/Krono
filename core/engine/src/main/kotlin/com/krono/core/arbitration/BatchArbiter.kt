package com.krono.core.arbitration

object BatchArbiter {

    fun arbitrate(candidates: List<ArbitrationCandidate>): BatchArbitrationResult {
        val sorted = candidates.sortedBy { it.priorityPosition }
        val reservedTargets = mutableSetOf<String>()
        val decisions = mutableListOf<ArbitrationDecision>()
        val winners = mutableListOf<ArbitrationDecision.Winner>()

        for (candidate in sorted) {
            if (!candidate.isPreflightReady) {
                decisions.add(ArbitrationDecision.PreflightBlocked(candidate))
                continue
            }

            val branchTargets = candidate.branch.requiredTargetResources()
            val conflicting = branchTargets.intersect(reservedTargets)

            if (conflicting.isNotEmpty()) {
                decisions.add(ArbitrationDecision.ConflictLost(candidate, conflicting))
            } else {
                val winner = ArbitrationDecision.Winner(candidate, branchTargets)
                decisions.add(winner)
                winners.add(winner)
                reservedTargets.addAll(branchTargets)
            }
        }

        return BatchArbitrationResult(winners = winners, decisions = decisions)
    }
}
