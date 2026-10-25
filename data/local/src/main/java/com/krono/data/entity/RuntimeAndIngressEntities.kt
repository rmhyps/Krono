package com.krono.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "runtime_sessions")
data class RuntimeSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "runtime_epoch")
    val runtimeEpoch: String,
    @ColumnInfo(name = "started_at_utc_ms")
    val startedAtUtcMs: Long,
    @ColumnInfo(name = "ended_at_utc_ms")
    val endedAtUtcMs: Long? = null,
    @ColumnInfo(name = "recovery_complete")
    val recoveryComplete: Boolean = false
)

@Entity(tableName = "engine_meta")
data class EngineMetaEntity(
    @PrimaryKey
    @ColumnInfo(name = "singleton_id")
    val singletonId: Int = 1,
    @ColumnInfo(name = "current_runtime_epoch")
    val currentRuntimeEpoch: String? = null,
    @ColumnInfo(name = "next_event_seq")
    val nextEventSeq: Long = 1,
    @ColumnInfo(name = "next_batch_seq")
    val nextBatchSeq: Long = 1,
    @ColumnInfo(name = "next_intent_seq")
    val nextIntentSeq: Long = 1,
    @ColumnInfo(name = "priority_revision")
    val priorityRevision: Int = 1,
    @ColumnInfo(name = "monitoring_revision")
    val monitoringRevision: Int = 1,
    @ColumnInfo(name = "control_version")
    val controlVersion: Long = 1,
    @ColumnInfo(name = "history_clear_through_seq")
    val historyClearThroughSeq: Long = 0,
    @ColumnInfo(name = "row_version")
    val rowVersion: Int = 1
)

@Entity(
    tableName = "monitoring_state",
    primaryKeys = ["source_id", "source_revision"],
    indices = [Index(value = ["registration_key"], unique = true)]
)
data class MonitoringStateEntity(
    @ColumnInfo(name = "source_id")
    val sourceId: String,
    @ColumnInfo(name = "source_revision")
    val sourceRevision: Int,
    @ColumnInfo(name = "desired_enabled")
    val desiredEnabled: Boolean,
    @ColumnInfo(name = "desired_version")
    val desiredVersion: Int,
    @ColumnInfo(name = "registration_key")
    val registrationKey: String,
    @ColumnInfo(name = "observed_version")
    val observedVersion: Int? = null,
    @ColumnInfo(name = "registration_state")
    val registrationState: String,
    @ColumnInfo(name = "last_error_code")
    val lastErrorCode: String? = null,
    @ColumnInfo(name = "checked_at_utc_ms")
    val checkedAtUtcMs: Long? = null,
    @ColumnInfo(name = "row_version")
    val rowVersion: Int = 1
)

@Entity(
    tableName = "source_state",
    primaryKeys = ["source_id", "source_revision"]
)
data class SourceStateEntity(
    @ColumnInfo(name = "source_id")
    val sourceId: String,
    @ColumnInfo(name = "source_revision")
    val sourceRevision: Int,
    @ColumnInfo(name = "state_version")
    val stateVersion: Int,
    @ColumnInfo(name = "facts_json")
    val factsJson: String,
    @ColumnInfo(name = "quality_code")
    val qualityCode: String,
    @ColumnInfo(name = "observation_group_id")
    val observationGroupId: String? = null,
    @ColumnInfo(name = "observed_elapsed_ms")
    val observedElapsedMs: Long? = null,
    @ColumnInfo(name = "received_elapsed_ms")
    val receivedElapsedMs: Long,
    @ColumnInfo(name = "runtime_epoch")
    val runtimeEpoch: String,
    @ColumnInfo(name = "coverage_version")
    val coverageVersion: Int,
    @ColumnInfo(name = "source_episode_key")
    val sourceEpisodeKey: String? = null,
    @ColumnInfo(name = "source_episode_counter")
    val sourceEpisodeCounter: Int = 0
)

@Entity(
    tableName = "rule_episode_state",
    primaryKeys = ["rule_id", "revision", "activation_generation"]
)
data class RuleEpisodeStateEntity(
    @ColumnInfo(name = "rule_id")
    val ruleId: String,
    @ColumnInfo(name = "revision")
    val revision: Int,
    @ColumnInfo(name = "activation_generation")
    val activationGeneration: Int,
    @ColumnInfo(name = "last_known_branch_id")
    val lastKnownBranchId: String? = null,
    @ColumnInfo(name = "branch_episode_key")
    val branchEpisodeKey: String? = null,
    @ColumnInfo(name = "branch_episode_counter")
    val branchEpisodeCounter: Int = 0,
    @ColumnInfo(name = "source_episode_key")
    val sourceEpisodeKey: String? = null,
    @ColumnInfo(name = "continuity_state")
    val continuityState: String,
    @ColumnInfo(name = "row_version")
    val rowVersion: Int = 1
)

@Entity(tableName = "command_receipts")
data class CommandReceiptEntity(
    @PrimaryKey
    @ColumnInfo(name = "command_id")
    val commandId: String,
    @ColumnInfo(name = "command_kind")
    val commandKind: String,
    @ColumnInfo(name = "request_hash")
    val requestHash: String,
    @ColumnInfo(name = "subject_key")
    val subjectKey: String,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "result_json")
    val resultJson: String? = null,
    @ColumnInfo(name = "created_at_utc_ms")
    val createdAtUtcMs: Long
)

@Entity(
    tableName = "event_inbox",
    indices = [
        Index(value = ["event_seq"], unique = true),
        Index(value = ["event_key"], unique = true)
    ]
)
data class EventInboxEntity(
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    val eventId: String,
    @ColumnInfo(name = "event_seq")
    val eventSeq: Long,
    @ColumnInfo(name = "event_key")
    val eventKey: String,
    @ColumnInfo(name = "event_kind")
    val eventKind: String,
    @ColumnInfo(name = "cause_key")
    val causeKey: String,
    @ColumnInfo(name = "origin_command_id")
    val originCommandId: String? = null,
    @ColumnInfo(name = "runtime_epoch")
    val runtimeEpoch: String,
    @ColumnInfo(name = "received_elapsed_ms")
    val receivedElapsedMs: Long,
    @ColumnInfo(name = "received_at_utc_ms")
    val receivedAtUtcMs: Long,
    @ColumnInfo(name = "payload_version")
    val payloadVersion: Int = 1,
    @ColumnInfo(name = "event_payload_json")
    val eventPayloadJson: String? = null,
    @ColumnInfo(name = "processing_state")
    val processingState: String,
    @ColumnInfo(name = "row_version")
    val rowVersion: Int = 1
)

@Entity(
    tableName = "event_sources",
    primaryKeys = ["event_id", "source_id", "source_revision"]
)
data class EventSourceEntity(
    @ColumnInfo(name = "event_id")
    val eventId: String,
    @ColumnInfo(name = "source_id")
    val sourceId: String,
    @ColumnInfo(name = "source_revision")
    val sourceRevision: Int
)
