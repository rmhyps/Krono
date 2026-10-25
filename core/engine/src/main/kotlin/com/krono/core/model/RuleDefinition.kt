package com.krono.core.model

data class RuleDefinition(
    val ruleId: String,
    val revision: Int = 1,
    val name: String,
    val mode: RuleMode,
    val trigger: TriggerDefinition,
    val branches: List<BranchDefinition>,
    val elseBranch: BranchDefinition? = null,
    val ruleFormatVersion: Int = 1,
    val semanticsVersion: String = "0.1"
)
