package com.krono.data.store

import androidx.room.withTransaction
import com.krono.core.model.RuleDefinition
import com.krono.core.ports.AutomationStore
import com.krono.data.database.KronoDatabase
import com.krono.data.entity.ActionAttemptEntity
import com.krono.data.entity.ActionHistoryEntity
import com.krono.data.entity.CommandReceiptEntity
import com.krono.data.entity.ExecutionHistoryEntity
import com.krono.data.entity.RuleEntity
import com.krono.data.entity.RulePriorityEntity
import com.krono.data.entity.RuleStateEntity
import com.krono.data.entity.RuntimeSessionEntity
import com.krono.data.mapper.RuleMappers

class RoomAutomationStore(
    private val database: KronoDatabase
) : AutomationStore {

    private val dao = database.kronoDao()

    override suspend fun getActiveRules(): List<RuleDefinition> = database.withTransaction {
        val rules = dao.getActiveRules()
        val result = mutableListOf<RuleDefinition>()
        for (r in rules) {
            val state = dao.getRuleState(r.ruleId)
            if (state?.desiredActive == true && state.currentRevision != null) {
                val rev = dao.getRuleRevision(r.ruleId, state.currentRevision)
                if (rev != null) {
                    val branches = dao.getBranches(r.ruleId, state.currentRevision)
                    val actions = dao.getActions(r.ruleId, state.currentRevision)
                    val sources = dao.getRuleSources(r.ruleId, state.currentRevision)

                    val sourceDefs = mutableMapOf<String, com.krono.data.entity.SourceDefinitionEntity>()
                    for (b in branches) {
                        b.predicateSourceId?.let { sId ->
                            if (sId !in sourceDefs) {
                                dao.getSourceDefinition(sId, b.predicateSourceRevision ?: 1)?.let {
                                    sourceDefs[sId] = it
                                }
                            }
                        }
                    }

                    result.add(RuleMappers.toDomain(rev, branches, actions, sources, sourceDefs))
                }
            }
        }
        result
    }

    suspend fun getAllRulesWithStatus(): List<Pair<RuleDefinition, Boolean>> = database.withTransaction {
        val rules = dao.getActiveRules()
        val result = mutableListOf<Pair<RuleDefinition, Boolean>>()
        for (r in rules) {
            val state = dao.getRuleState(r.ruleId)
            val currentRev = state?.currentRevision
            if (currentRev != null) {
                val rev = dao.getRuleRevision(r.ruleId, currentRev)
                if (rev != null) {
                    val branches = dao.getBranches(r.ruleId, currentRev)
                    val actions = dao.getActions(r.ruleId, currentRev)
                    val sources = dao.getRuleSources(r.ruleId, currentRev)

                    val sourceDefs = mutableMapOf<String, com.krono.data.entity.SourceDefinitionEntity>()
                    for (b in branches) {
                        b.predicateSourceId?.let { sId ->
                            if (sId !in sourceDefs) {
                                dao.getSourceDefinition(sId, b.predicateSourceRevision ?: 1)?.let {
                                    sourceDefs[sId] = it
                                }
                            }
                        }
                    }

                    val domainRule = RuleMappers.toDomain(rev, branches, actions, sources, sourceDefs)
                    result.add(domainRule to (state.desiredActive))
                }
            }
        }
        result
    }

    override suspend fun saveRule(rule: RuleDefinition): Boolean = database.withTransaction {
        val now = System.currentTimeMillis()
        dao.insertRule(RuleEntity(ruleId = rule.ruleId, createdAtUtcMs = now))

        val (revisionEntity, details) = RuleMappers.toEntities(rule, now)
        val (branches, actions, sources) = details

        dao.deleteBranches(rule.ruleId, rule.revision)
        dao.deleteActions(rule.ruleId, rule.revision)
        dao.deleteRuleSources(rule.ruleId, rule.revision)

        dao.insertRuleRevision(revisionEntity)
        dao.insertBranches(branches)
        dao.insertActions(actions)
        dao.insertRuleSources(sources)

        for (b in rule.branches) {
            if (b.condition.kind == com.krono.core.model.PredicateKind.LOCATION_IN_AREA) {
                val areaId = b.condition.areaId ?: "area-${b.branchId}"
                val lat = b.condition.latitude ?: -7.894303
                val lng = b.condition.longitude ?: 112.595458
                val rad = b.condition.radiusM ?: 150.0
                dao.insertSourceDefinition(
                    com.krono.data.entity.SourceDefinitionEntity(
                        sourceId = areaId,
                        sourceRevision = 1,
                        sourceKind = "GEO_AREA",
                        label = areaId,
                        latitude = lat,
                        longitude = lng,
                        radiusM = rad,
                        createdAtUtcMs = now
                    )
                )
            }
        }

        val existingState = dao.getRuleState(rule.ruleId)
        val nextGeneration = (existingState?.activationGeneration ?: 0) + 1
        val nextRowVersion = (existingState?.rowVersion ?: 0) + 1

        dao.insertOrUpdateRuleState(
            RuleStateEntity(
                ruleId = rule.ruleId,
                currentRevision = rule.revision,
                desiredActive = existingState?.desiredActive ?: false,
                activationGeneration = nextGeneration,
                rowVersion = nextRowVersion,
                updatedAtUtcMs = now
            )
        )

        val priorities = dao.getRulePriorities()
        if (priorities.none { it.ruleId == rule.ruleId }) {
            val nextPos = priorities.size
            dao.insertRulePriorities(listOf(RulePriorityEntity(rule.ruleId, nextPos)))
        }

        true
    }

    override suspend fun setRuleActive(ruleId: String, active: Boolean): Boolean = database.withTransaction {
        val existingState = dao.getRuleState(ruleId) ?: return@withTransaction false
        val now = System.currentTimeMillis()
        val nextGeneration = existingState.activationGeneration + 1

        dao.insertOrUpdateRuleState(
            existingState.copy(
                desiredActive = active,
                activationGeneration = nextGeneration,
                rowVersion = existingState.rowVersion + 1,
                updatedAtUtcMs = now
            )
        )
        true
    }

    override suspend fun deleteRule(ruleId: String): Boolean = database.withTransaction {
        val now = System.currentTimeMillis()
        dao.softDeleteRule(ruleId, now)
        dao.deleteRulePriority(ruleId)

        val existingState = dao.getRuleState(ruleId)
        if (existingState != null) {
            dao.insertOrUpdateRuleState(
                existingState.copy(
                    desiredActive = false,
                    activationGeneration = existingState.activationGeneration + 1,
                    rowVersion = existingState.rowVersion + 1,
                    updatedAtUtcMs = now
                )
            )
        }
        true
    }

    override suspend fun getRulePriorityList(): List<String> = database.withTransaction {
        dao.getRulePriorities().sortedBy { it.position }.map { it.ruleId }
    }

    override suspend fun updateRulePriorityList(ruleIdsInOrder: List<String>): Boolean = database.withTransaction {
        dao.clearRulePriorities()
        val list = ruleIdsInOrder.mapIndexed { index, id ->
            RulePriorityEntity(ruleId = id, position = index)
        }
        dao.insertRulePriorities(list)
        true
    }

    suspend fun initializeRuntimeSession(epoch: String): Boolean = database.withTransaction {
        val now = System.currentTimeMillis()
        dao.insertRuntimeSession(
            RuntimeSessionEntity(
                runtimeEpoch = epoch,
                startedAtUtcMs = now,
                recoveryComplete = true
            )
        )
        true
    }

    suspend fun claimAttemptForExecution(attemptId: String, nowElapsedMs: Long): Boolean = database.withTransaction {
        val rowsAffected = dao.updateAttemptToRequesting(attemptId, "REQUESTING", nowElapsedMs)
        rowsAffected == 1
    }

    suspend fun saveActionAttempt(attempt: ActionAttemptEntity) = database.withTransaction {
        dao.insertActionAttempt(attempt)
    }

    suspend fun saveCommandReceipt(receipt: CommandReceiptEntity) = database.withTransaction {
        dao.insertCommandReceipt(receipt)
    }

    suspend fun getCommandReceipt(commandId: String): CommandReceiptEntity? = database.withTransaction {
        dao.getCommandReceipt(commandId)
    }

    suspend fun recordExecutionHistory(
        history: ExecutionHistoryEntity,
        actions: List<ActionHistoryEntity>
    ) = database.withTransaction {
        dao.insertExecutionHistory(history)
        dao.insertActionHistory(actions)
    }

    suspend fun getExecutionHistory(limit: Int): List<ExecutionHistoryEntity> = database.withTransaction {
        dao.getExecutionHistory(limit)
    }

    suspend fun clearHistory(clearThroughSeq: Long): Int = database.withTransaction {
        dao.clearExecutionHistory(clearThroughSeq)
    }
}
