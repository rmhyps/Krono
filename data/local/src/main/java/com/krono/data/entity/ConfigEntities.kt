package com.krono.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey
    @ColumnInfo(name = "rule_id")
    val ruleId: String,
    @ColumnInfo(name = "created_at_utc_ms")
    val createdAtUtcMs: Long,
    @ColumnInfo(name = "deleted_at_utc_ms")
    val deletedAtUtcMs: Long? = null
)

@Entity(
    tableName = "rule_revisions",
    primaryKeys = ["rule_id", "revision"]
)
data class RuleRevisionEntity(
    @ColumnInfo(name = "rule_id")
    val ruleId: String,
    @ColumnInfo(name = "revision")
    val revision: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "mode")
    val mode: String,
    @ColumnInfo(name = "trigger_family")
    val triggerFamily: String,
    @ColumnInfo(name = "trigger_kind")
    val triggerKind: String,
    @ColumnInfo(name = "rule_format_version")
    val ruleFormatVersion: Int = 1,
    @ColumnInfo(name = "semantics_version")
    val semanticsVersion: String = "0.1",
    @ColumnInfo(name = "created_at_utc_ms")
    val createdAtUtcMs: Long
)

@Entity(tableName = "rule_state")
data class RuleStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "rule_id")
    val ruleId: String,
    @ColumnInfo(name = "current_revision")
    val currentRevision: Int?,
    @ColumnInfo(name = "desired_active")
    val desiredActive: Boolean,
    @ColumnInfo(name = "activation_generation")
    val activationGeneration: Int,
    @ColumnInfo(name = "row_version")
    val rowVersion: Int = 1,
    @ColumnInfo(name = "history_clear_through_seq")
    val historyClearThroughSeq: Long = 0,
    @ColumnInfo(name = "updated_at_utc_ms")
    val updatedAtUtcMs: Long
)

@Entity(
    tableName = "rule_priority",
    indices = [Index(value = ["position"], unique = true)]
)
data class RulePriorityEntity(
    @PrimaryKey
    @ColumnInfo(name = "rule_id")
    val ruleId: String,
    @ColumnInfo(name = "position")
    val position: Int
)

@Entity(
    tableName = "branches",
    primaryKeys = ["rule_id", "revision", "branch_id"],
    indices = [Index(value = ["rule_id", "revision", "position"], unique = true)]
)
data class BranchEntity(
    @ColumnInfo(name = "rule_id")
    val ruleId: String,
    @ColumnInfo(name = "revision")
    val revision: Int,
    @ColumnInfo(name = "branch_id")
    val branchId: String,
    @ColumnInfo(name = "position")
    val position: Int,
    @ColumnInfo(name = "branch_kind")
    val branchKind: String,
    @ColumnInfo(name = "predicate_kind")
    val predicateKind: String,
    @ColumnInfo(name = "predicate_source_id")
    val predicateSourceId: String? = null,
    @ColumnInfo(name = "predicate_source_revision")
    val predicateSourceRevision: Int? = null
)

@Entity(
    tableName = "actions",
    primaryKeys = ["rule_id", "revision", "action_id"],
    indices = [Index(value = ["rule_id", "revision", "branch_id", "position"], unique = true)]
)
data class ActionEntity(
    @ColumnInfo(name = "rule_id")
    val ruleId: String,
    @ColumnInfo(name = "revision")
    val revision: Int,
    @ColumnInfo(name = "action_id")
    val actionId: String,
    @ColumnInfo(name = "branch_id")
    val branchId: String,
    @ColumnInfo(name = "position")
    val position: Int,
    @ColumnInfo(name = "action_kind")
    val actionKind: String,
    @ColumnInfo(name = "asset_id")
    val assetId: String? = null,
    @ColumnInfo(name = "wallpaper_target")
    val wallpaperTarget: String? = null,
    @ColumnInfo(name = "volume_percent")
    val volumePercent: Int? = null,
    @ColumnInfo(name = "ringer_mode")
    val ringerMode: String? = null
)

@Entity(
    tableName = "rule_sources",
    primaryKeys = ["rule_id", "revision", "source_id", "source_revision"]
)
data class RuleSourceEntity(
    @ColumnInfo(name = "rule_id")
    val ruleId: String,
    @ColumnInfo(name = "revision")
    val revision: Int,
    @ColumnInfo(name = "source_id")
    val sourceId: String,
    @ColumnInfo(name = "source_revision")
    val sourceRevision: Int
)

@Entity(
    tableName = "source_definitions",
    primaryKeys = ["source_id", "source_revision"]
)
data class SourceDefinitionEntity(
    @ColumnInfo(name = "source_id")
    val sourceId: String,
    @ColumnInfo(name = "source_revision")
    val sourceRevision: Int,
    @ColumnInfo(name = "source_kind")
    val sourceKind: String,
    @ColumnInfo(name = "label")
    val label: String,
    @ColumnInfo(name = "latitude")
    val latitude: Double? = null,
    @ColumnInfo(name = "longitude")
    val longitude: Double? = null,
    @ColumnInfo(name = "radius_m")
    val radiusM: Double? = null,
    @ColumnInfo(name = "device_identity")
    val deviceIdentity: String? = null,
    @ColumnInfo(name = "transport_policy")
    val transportPolicy: String? = null,
    @ColumnInfo(name = "created_at_utc_ms")
    val createdAtUtcMs: Long
)
