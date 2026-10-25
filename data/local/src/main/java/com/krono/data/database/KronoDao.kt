package com.krono.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.krono.data.entity.ActionAttemptEntity
import com.krono.data.entity.ActionEntity
import com.krono.data.entity.ActionHistoryEntity
import com.krono.data.entity.AssetEntity
import com.krono.data.entity.AssetReadLeaseEntity
import com.krono.data.entity.BranchEntity
import com.krono.data.entity.CommandReceiptEntity
import com.krono.data.entity.DraftAssetRefEntity
import com.krono.data.entity.EngineMetaEntity
import com.krono.data.entity.EpisodeClaimEntity
import com.krono.data.entity.EvaluationBatchEntity
import com.krono.data.entity.EvaluationRoundEntity
import com.krono.data.entity.EventInboxEntity
import com.krono.data.entity.EventSourceEntity
import com.krono.data.entity.ExecutionHistoryEntity
import com.krono.data.entity.MonitoringStateEntity
import com.krono.data.entity.OpportunityEntity
import com.krono.data.entity.OutboxEntity
import com.krono.data.entity.PlanEntity
import com.krono.data.entity.PlanTargetEntity
import com.krono.data.entity.RoundDecisionEntity
import com.krono.data.entity.RuleDraftEntity
import com.krono.data.entity.RuleEntity
import com.krono.data.entity.RuleEpisodeStateEntity
import com.krono.data.entity.RulePriorityEntity
import com.krono.data.entity.RuleRevisionEntity
import com.krono.data.entity.RuleSourceEntity
import com.krono.data.entity.RuleStateEntity
import com.krono.data.entity.RuntimeSessionEntity
import com.krono.data.entity.SourceDefinitionEntity
import com.krono.data.entity.SourceStateEntity
import com.krono.data.entity.TargetHeadEntity

@Dao
interface KronoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRule(rule: RuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleRevision(revision: RuleRevisionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRuleState(state: RuleStateEntity)

    @Query("SELECT * FROM rules WHERE deleted_at_utc_ms IS NULL")
    suspend fun getActiveRules(): List<RuleEntity>

    @Query("UPDATE rules SET deleted_at_utc_ms = :now WHERE rule_id = :ruleId")
    suspend fun softDeleteRule(ruleId: String, now: Long): Int

    @Query("SELECT * FROM rule_state WHERE rule_id = :ruleId")
    suspend fun getRuleState(ruleId: String): RuleStateEntity?

    @Query("SELECT * FROM rule_revisions WHERE rule_id = :ruleId AND revision = :revision")
    suspend fun getRuleRevision(ruleId: String, revision: Int): RuleRevisionEntity?

    @Query("SELECT * FROM branches WHERE rule_id = :ruleId AND revision = :revision ORDER BY position ASC")
    suspend fun getBranches(ruleId: String, revision: Int): List<BranchEntity>

    @Query("SELECT * FROM actions WHERE rule_id = :ruleId AND revision = :revision ORDER BY position ASC")
    suspend fun getActions(ruleId: String, revision: Int): List<ActionEntity>

    @Query("SELECT * FROM rule_sources WHERE rule_id = :ruleId AND revision = :revision")
    suspend fun getRuleSources(ruleId: String, revision: Int): List<RuleSourceEntity>

    @Query("DELETE FROM branches WHERE rule_id = :ruleId AND revision = :revision")
    suspend fun deleteBranches(ruleId: String, revision: Int)

    @Query("DELETE FROM actions WHERE rule_id = :ruleId AND revision = :revision")
    suspend fun deleteActions(ruleId: String, revision: Int)

    @Query("DELETE FROM rule_sources WHERE rule_id = :ruleId AND revision = :revision")
    suspend fun deleteRuleSources(ruleId: String, revision: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranches(branches: List<BranchEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActions(actions: List<ActionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleSources(sources: List<RuleSourceEntity>)

    @Query("SELECT * FROM rule_priority ORDER BY position ASC")
    suspend fun getRulePriorities(): List<RulePriorityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRulePriorities(priorities: List<RulePriorityEntity>)

    @Query("DELETE FROM rule_priority")
    suspend fun clearRulePriorities()

    @Query("DELETE FROM rule_priority WHERE rule_id = :ruleId")
    suspend fun deleteRulePriority(ruleId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSourceDefinition(source: SourceDefinitionEntity)

    @Query("SELECT * FROM source_definitions WHERE source_id = :sourceId AND source_revision = :revision")
    suspend fun getSourceDefinition(sourceId: String, revision: Int): SourceDefinitionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: RuleDraftEntity)

    @Query("SELECT * FROM rule_drafts WHERE draft_id = :draftId")
    suspend fun getDraft(draftId: String): RuleDraftEntity?

    @Query("DELETE FROM rule_drafts WHERE draft_id = :draftId")
    suspend fun deleteDraft(draftId: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEngineMeta(meta: EngineMetaEntity)

    @Query("SELECT * FROM engine_meta WHERE singleton_id = 1")
    suspend fun getEngineMeta(): EngineMetaEntity?

    @Update
    suspend fun updateEngineMeta(meta: EngineMetaEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRuntimeSession(session: RuntimeSessionEntity)

    @Query("SELECT * FROM runtime_sessions WHERE runtime_epoch = :epoch")
    suspend fun getRuntimeSession(epoch: String): RuntimeSessionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCommandReceipt(receipt: CommandReceiptEntity)

    @Query("SELECT * FROM command_receipts WHERE command_id = :commandId")
    suspend fun getCommandReceipt(commandId: String): CommandReceiptEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEvent(event: EventInboxEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBatch(batch: EvaluationBatchEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRound(round: EvaluationRoundEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOpportunity(opportunity: OpportunityEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRoundDecision(decision: RoundDecisionEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEpisodeClaim(claim: EpisodeClaimEntity)

    @Query("SELECT * FROM episode_claims WHERE rule_id = :ruleId AND rule_revision = :revision AND activation_generation = :gen AND branch_id = :branchId AND episode_key = :episodeKey")
    suspend fun getEpisodeClaim(ruleId: String, revision: Int, gen: Int, branchId: String, episodeKey: String): EpisodeClaimEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlan(plan: PlanEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPlanTargets(targets: List<PlanTargetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTargetHead(head: TargetHeadEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertActionAttempt(attempt: ActionAttemptEntity)

    @Query("SELECT * FROM action_attempts WHERE attempt_id = :attemptId")
    suspend fun getActionAttempt(attemptId: String): ActionAttemptEntity?

    @Query("UPDATE action_attempts SET phase = :phase, requesting_elapsed_ms = :elapsed, row_version = row_version + 1 WHERE attempt_id = :attemptId AND phase = 'PREPARED'")
    suspend fun updateAttemptToRequesting(attemptId: String, phase: String, elapsed: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertOutbox(outbox: OutboxEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExecutionHistory(history: ExecutionHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertActionHistory(historyList: List<ActionHistoryEntity>)

    @Query("SELECT * FROM execution_history ORDER BY finished_at_utc_ms DESC, history_id DESC LIMIT :limit")
    suspend fun getExecutionHistory(limit: Int): List<ExecutionHistoryEntity>

    @Query("DELETE FROM execution_history WHERE origin_event_seq <= :clearThroughSeq")
    suspend fun clearExecutionHistory(clearThroughSeq: Long): Int
}
