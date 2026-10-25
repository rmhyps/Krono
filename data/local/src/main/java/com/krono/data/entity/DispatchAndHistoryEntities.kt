package com.krono.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outbox",
    indices = [Index(value = ["delivery_key"], unique = true)]
)
data class OutboxEntity(
    @PrimaryKey
    @ColumnInfo(name = "outbox_id")
    val outboxId: String,
    @ColumnInfo(name = "delivery_key")
    val deliveryKey: String,
    @ColumnInfo(name = "work_kind")
    val workKind: String,
    @ColumnInfo(name = "reference_id")
    val referenceId: String? = null,
    @ColumnInfo(name = "expected_runtime_epoch")
    val expectedRuntimeEpoch: String? = null,
    @ColumnInfo(name = "expected_monitoring_revision")
    val expectedMonitoringRevision: Int? = null,
    @ColumnInfo(name = "state")
    val state: String,
    @ColumnInfo(name = "external_work_id")
    val externalWorkId: String? = null,
    @ColumnInfo(name = "dispatch_count")
    val dispatchCount: Int = 0,
    @ColumnInfo(name = "next_dispatch_at_utc_ms")
    val nextDispatchAtUtcMs: Long? = null,
    @ColumnInfo(name = "last_error_code")
    val lastErrorCode: String? = null,
    @ColumnInfo(name = "created_at_utc_ms")
    val createdAtUtcMs: Long
)

@Entity(
    tableName = "execution_history",
    indices = [
        Index(value = ["opportunity_key"], unique = true),
        Index(value = ["finished_at_utc_ms", "history_id"]),
        Index(value = ["rule_id_snapshot", "finished_at_utc_ms", "history_id"])
    ]
)
data class ExecutionHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "history_id")
    val historyId: String,
    @ColumnInfo(name = "opportunity_key")
    val opportunityKey: String,
    @ColumnInfo(name = "origin_event_seq")
    val originEventSeq: Long,
    @ColumnInfo(name = "rule_id_snapshot")
    val ruleIdSnapshot: String? = null,
    @ColumnInfo(name = "rule_revision_snapshot")
    val ruleRevisionSnapshot: Int? = null,
    @ColumnInfo(name = "rule_name_snapshot")
    val ruleNameSnapshot: String,
    @ColumnInfo(name = "origin_kind")
    val originKind: String,
    @ColumnInfo(name = "branch_label_snapshot")
    val branchLabelSnapshot: String? = null,
    @ColumnInfo(name = "source_labels_json")
    val sourceLabelsJson: String,
    @ColumnInfo(name = "summary_result")
    val summaryResult: String,
    @ColumnInfo(name = "reason_code")
    val reasonCode: String? = null,
    @ColumnInfo(name = "started_at_utc_ms")
    val startedAtUtcMs: Long,
    @ColumnInfo(name = "finished_at_utc_ms")
    val finishedAtUtcMs: Long
)

@Entity(
    tableName = "action_history",
    primaryKeys = ["history_id", "step_index"]
)
data class ActionHistoryEntity(
    @ColumnInfo(name = "history_id")
    val historyId: String,
    @ColumnInfo(name = "step_index")
    val stepIndex: Int,
    @ColumnInfo(name = "action_id_snapshot")
    val actionIdSnapshot: String,
    @ColumnInfo(name = "action_kind")
    val actionKind: String,
    @ColumnInfo(name = "target_summary_json")
    val targetSummaryJson: String,
    @ColumnInfo(name = "result_code")
    val resultCode: String,
    @ColumnInfo(name = "reason_code")
    val reasonCode: String? = null,
    @ColumnInfo(name = "evidence_level")
    val evidenceLevel: String,
    @ColumnInfo(name = "evidence_summary_json")
    val evidenceSummaryJson: String? = null
)
