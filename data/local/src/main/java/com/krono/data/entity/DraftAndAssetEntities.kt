package com.krono.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "rule_drafts")
data class RuleDraftEntity(
    @PrimaryKey
    @ColumnInfo(name = "draft_id")
    val draftId: String,
    @ColumnInfo(name = "target_rule_id")
    val targetRuleId: String? = null,
    @ColumnInfo(name = "base_revision")
    val baseRevision: Int? = null,
    @ColumnInfo(name = "draft_format_version")
    val draftFormatVersion: Int = 1,
    @ColumnInfo(name = "draft_json")
    val draftJson: String,
    @ColumnInfo(name = "row_version")
    val rowVersion: Int = 1,
    @ColumnInfo(name = "updated_at_utc_ms")
    val updatedAtUtcMs: Long
)

@Entity(
    tableName = "draft_asset_refs",
    primaryKeys = ["draft_id", "asset_id"]
)
data class DraftAssetRefEntity(
    @ColumnInfo(name = "draft_id")
    val draftId: String,
    @ColumnInfo(name = "asset_id")
    val assetId: String
)

@Entity(
    tableName = "assets",
    indices = [Index(value = ["relative_path"], unique = true)]
)
data class AssetEntity(
    @PrimaryKey
    @ColumnInfo(name = "asset_id")
    val assetId: String,
    @ColumnInfo(name = "state")
    val state: String,
    @ColumnInfo(name = "relative_path")
    val relativePath: String? = null,
    @ColumnInfo(name = "mime_type")
    val mimeType: String? = null,
    @ColumnInfo(name = "byte_count")
    val byteCount: Long? = null,
    @ColumnInfo(name = "width_px")
    val widthPx: Int? = null,
    @ColumnInfo(name = "height_px")
    val heightPx: Int? = null,
    @ColumnInfo(name = "sha256")
    val sha256: String? = null,
    @ColumnInfo(name = "error_code")
    val errorCode: String? = null,
    @ColumnInfo(name = "row_version")
    val rowVersion: Int = 1,
    @ColumnInfo(name = "created_at_utc_ms")
    val createdAtUtcMs: Long,
    @ColumnInfo(name = "updated_at_utc_ms")
    val updatedAtUtcMs: Long
)

@Entity(tableName = "asset_read_leases")
data class AssetReadLeaseEntity(
    @PrimaryKey
    @ColumnInfo(name = "lease_id")
    val leaseId: String,
    @ColumnInfo(name = "asset_id")
    val assetId: String,
    @ColumnInfo(name = "runtime_epoch")
    val runtimeEpoch: String,
    @ColumnInfo(name = "owner_key")
    val ownerKey: String,
    @ColumnInfo(name = "created_elapsed_ms")
    val createdElapsedMs: Long
)
