package com.krono.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "evaluation_batches",
    indices = [
        Index(value = ["batch_seq"], unique = true),
        Index(value = ["event_id"], unique = true)
    ]
)
data class EvaluationBatchEntity(
    @PrimaryKey
    @ColumnInfo(name = "batch_id")
    val batchId: String,
    @ColumnInfo(name = "batch_seq")
    val batchSeq: Long,
    @ColumnInfo(name = "event_id")
    val eventId: String,
    @ColumnInfo(name = "priority_revision")
    val priorityRevision: Int,
    @ColumnInfo(name = "runtime_epoch")
    val runtimeEpoch: String,
    @ColumnInfo(name = "state")
    val state: String,
    @ColumnInfo(name = "created_elapsed_ms")
    val createdElapsedMs: Long
)

@Entity(
    tableName = "evaluation_rounds",
    indices = [Index(value = ["batch_id", "round_number"], unique = true)]
)
data class EvaluationRoundEntity(
    @PrimaryKey
    @ColumnInfo(name = "round_id")
    val roundId: String,
    @ColumnInfo(name = "batch_id")
    val batchId: String,
    @ColumnInfo(name = "round_number")
    val roundNumber: Int,
    @ColumnInfo(name = "runtime_epoch")
    val runtimeEpoch: String,
    @ColumnInfo(name = "snapshot_version")
    val snapshotVersion: Int = 1,
    @ColumnInfo(name = "snapshot_json")
    val snapshotJson: String? = null,
    @ColumnInfo(name = "snapshot_hash")
    val snapshotHash: String,
    @ColumnInfo(name = "captured_elapsed_ms")
    val capturedElapsedMs: Long
)

@Entity(
    tableName = "opportunities",
    indices = [
        Index(value = ["opportunity_key"], unique = true),
        Index(value = ["test_command_id"], unique = true),
        Index(value = ["batch_id", "priority_position"], unique = true),
        Index(value = ["rule_id", "rule_revision", "activation_generation", "cause_key"], unique = true)
    ]
)
data class OpportunityEntity(
    @PrimaryKey
    @ColumnInfo(name = "opportunity_id")
    val opportunityId: String,
    @ColumnInfo(name = "opportunity_key")
    val opportunityKey: String,
    @ColumnInfo(name = "owner_kind")
    val ownerKind: String,
    @ColumnInfo(name = "batch_id")
    val batchId: String,
    @ColumnInfo(name = "priority_position")
    val priorityPosition: Int,
    @ColumnInfo(name = "rule_id")
    val ruleId: String? = null,
    @ColumnInfo(name = "rule_revision")
    val ruleRevision: Int? = null,
    @ColumnInfo(name = "activation_generation")
    val activationGeneration: Int? = null,
    @ColumnInfo(name = "test_command_id")
    val testCommandId: String? = null,
    @ColumnInfo(name = "cause_key")
    val causeKey: String,
    @ColumnInfo(name = "runtime_epoch")
    val runtimeEpoch: String,
    @ColumnInfo(name = "created_elapsed_ms")
    val createdElapsedMs: Long,
    @ColumnInfo(name = "deadline_elapsed_ms")
    val deadlineElapsedMs: Long,
    @ColumnInfo(name = "state")
    val state: String,
    @ColumnInfo(name = "final_result")
    val finalResult: String? = null,
    @ColumnInfo(name = "reason_code")
    val reasonCode: String? = null,
    @ColumnInfo(name = "history_projection_state")
    val historyProjectionState: String = "NONE",
    @ColumnInfo(name = "row_version")
    val rowVersion: Int = 1
)

@Entity(
    tableName = "round_decisions",
    primaryKeys = ["round_id", "opportunity_id"]
)
data class RoundDecisionEntity(
    @ColumnInfo(name = "round_id")
    val roundId: String,
    @ColumnInfo(name = "opportunity_id")
    val opportunityId: String,
    @ColumnInfo(name = "selected_branch_id")
    val selectedBranchId: String? = null,
    @ColumnInfo(name = "decision_code")
    val decisionCode: String,
    @ColumnInfo(name = "reason_code")
    val reasonCode: String? = null,
    @ColumnInfo(name = "decision_json")
    val decisionJson: String? = null
)

@Entity(
    tableName = "episode_claims",
    primaryKeys = ["rule_id", "rule_revision", "activation_generation", "branch_id", "episode_key"]
)
data class EpisodeClaimEntity(
    @ColumnInfo(name = "rule_id")
    val ruleId: String,
    @ColumnInfo(name = "rule_revision")
    val ruleRevision: Int,
    @ColumnInfo(name = "activation_generation")
    val activationGeneration: Int,
    @ColumnInfo(name = "branch_id")
    val branchId: String,
    @ColumnInfo(name = "episode_key")
    val episodeKey: String,
    @ColumnInfo(name = "first_opportunity_id")
    val firstOpportunityId: String? = null,
    @ColumnInfo(name = "claim_outcome")
    val claimOutcome: String
)

@Entity(
    tableName = "plans",
    indices = [
        Index(value = ["opportunity_id"], unique = true),
        Index(value = ["intent_seq"], unique = true)
    ]
)
data class PlanEntity(
    @PrimaryKey
    @ColumnInfo(name = "plan_id")
    val planId: String,
    @ColumnInfo(name = "opportunity_id")
    val opportunityId: String,
    @ColumnInfo(name = "decision_round_id")
    val decisionRoundId: String,
    @ColumnInfo(name = "intent_seq")
    val intentSeq: Long,
    @ColumnInfo(name = "runtime_epoch")
    val runtimeEpoch: String,
    @ColumnInfo(name = "state")
    val state: String,
    @ColumnInfo(name = "context_json")
    val contextJson: String? = null,
    @ColumnInfo(name = "created_elapsed_ms")
    val createdElapsedMs: Long,
    @ColumnInfo(name = "row_version")
    val rowVersion: Int = 1
)

@Entity(
    tableName = "plan_targets",
    primaryKeys = ["plan_id", "resource_key"]
)
data class PlanTargetEntity(
    @ColumnInfo(name = "plan_id")
    val planId: String,
    @ColumnInfo(name = "resource_key")
    val resourceKey: String,
    @ColumnInfo(name = "intent_seq")
    val intentSeq: Long
)

@Entity(tableName = "target_heads")
data class TargetHeadEntity(
    @PrimaryKey
    @ColumnInfo(name = "resource_key")
    val resourceKey: String,
    @ColumnInfo(name = "intent_seq")
    val intentSeq: Long,
    @ColumnInfo(name = "current_plan_id")
    val currentPlanId: String? = null,
    @ColumnInfo(name = "runtime_epoch")
    val runtimeEpoch: String,
    @ColumnInfo(name = "row_version")
    val rowVersion: Int = 1
)

@Entity(
    tableName = "action_attempts",
    indices = [
        Index(value = ["effect_key"], unique = true),
        Index(value = ["plan_id", "step_index"], unique = true)
    ]
)
data class ActionAttemptEntity(
    @PrimaryKey
    @ColumnInfo(name = "attempt_id")
    val attemptId: String,
    @ColumnInfo(name = "plan_id")
    val planId: String,
    @ColumnInfo(name = "step_index")
    val stepIndex: Int,
    @ColumnInfo(name = "action_id")
    val actionId: String,
    @ColumnInfo(name = "effect_key")
    val effectKey: String,
    @ColumnInfo(name = "action_kind")
    val actionKind: String,
    @ColumnInfo(name = "action_params_json")
    val actionParamsJson: String? = null,
    @ColumnInfo(name = "asset_id")
    val assetId: String? = null,
    @ColumnInfo(name = "runtime_epoch")
    val runtimeEpoch: String,
    @ColumnInfo(name = "phase")
    val phase: String,
    @ColumnInfo(name = "result_code")
    val resultCode: String? = null,
    @ColumnInfo(name = "reason_code")
    val reasonCode: String? = null,
    @ColumnInfo(name = "prepared_elapsed_ms")
    val preparedElapsedMs: Long,
    @ColumnInfo(name = "requesting_elapsed_ms")
    val requestingElapsedMs: Long? = null,
    @ColumnInfo(name = "finished_at_utc_ms")
    val finishedAtUtcMs: Long? = null,
    @ColumnInfo(name = "evidence_json")
    val evidenceJson: String? = null,
    @ColumnInfo(name = "payload_retained")
    val payloadRetained: Boolean = true,
    @ColumnInfo(name = "row_version")
    val rowVersion: Int = 1
)
