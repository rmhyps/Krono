package com.krono.core.ports

import com.krono.core.model.RuleDefinition

interface AutomationStore {
    suspend fun getActiveRules(): List<RuleDefinition>
    suspend fun saveRule(rule: RuleDefinition): Boolean
    suspend fun setRuleActive(ruleId: String, active: Boolean): Boolean
    suspend fun deleteRule(ruleId: String): Boolean
    suspend fun getRulePriorityList(): List<String>
    suspend fun updateRulePriorityList(ruleIdsInOrder: List<String>): Boolean
}
