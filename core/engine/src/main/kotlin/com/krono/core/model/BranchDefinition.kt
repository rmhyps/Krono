package com.krono.core.model

enum class BranchKind {
    ALWAYS,
    IF,
    ELSE_IF,
    ELSE
}

data class BranchDefinition(
    val branchId: String,
    val kind: BranchKind,
    val condition: ConditionPredicate,
    val actions: List<ActionDefinition>
) {
    fun requiredTargetResources(): Set<String> {
        return actions.flatMap { it.requiredTargetResources() }.toSet()
    }
}
