package com.krono.core.result

object ResultAggregator {

    fun aggregate(results: List<ActionExecutionResult>): RuleExecutionSummary {
        if (results.isEmpty()) {
            return RuleExecutionSummary.SKIPPED
        }

        val allAppliedOrNoChange = results.all {
            it.status == ActionExecutionStatus.APPLIED || it.status == ActionExecutionStatus.NO_CHANGE_NEEDED
        }
        if (allAppliedOrNoChange) {
            return RuleExecutionSummary.SUCCESS
        }

        val hasApplied = results.any { it.status == ActionExecutionStatus.APPLIED }
        if (hasApplied) {
            return RuleExecutionSummary.PARTIAL
        }

        val hasUnverified = results.any { it.status == ActionExecutionStatus.UNVERIFIED }
        if (hasUnverified) {
            return RuleExecutionSummary.UNVERIFIED
        }

        val hasFailed = results.any { it.status == ActionExecutionStatus.FAILED }
        if (hasFailed) {
            return RuleExecutionSummary.FAILED
        }

        val hasBlocked = results.any { it.status == ActionExecutionStatus.BLOCKED }
        if (hasBlocked) {
            return RuleExecutionSummary.BLOCKED
        }

        return RuleExecutionSummary.SKIPPED
    }
}
