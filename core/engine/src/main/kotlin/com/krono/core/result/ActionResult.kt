package com.krono.core.result

enum class ActionExecutionStatus {
    APPLIED,
    NO_CHANGE_NEEDED,
    FAILED,
    BLOCKED,
    UNVERIFIED,
    SKIPPED,
    CANCELLED,
    EXPIRED,
    SUPERSEDED
}

data class ActionExecutionResult(
    val actionId: String,
    val status: ActionExecutionStatus,
    val reason: String? = null,
    val evidenceSummary: String? = null
)

enum class RuleExecutionSummary {
    SUCCESS,
    PARTIAL,
    UNVERIFIED,
    FAILED,
    BLOCKED,
    SKIPPED
}
