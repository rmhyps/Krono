package com.krono.data.mapper

import com.krono.core.model.ActionDefinition
import com.krono.core.model.ActionKind
import com.krono.core.model.BranchDefinition
import com.krono.core.model.BranchKind
import com.krono.core.model.ConditionPredicate
import com.krono.core.model.PredicateKind
import com.krono.core.model.RuleDefinition
import com.krono.core.model.RuleMode
import com.krono.core.model.TriggerDefinition
import com.krono.core.model.TriggerFamily
import com.krono.core.model.TriggerKind
import com.krono.core.model.WallpaperTarget
import com.krono.data.entity.ActionEntity
import com.krono.data.entity.BranchEntity
import com.krono.data.entity.RuleRevisionEntity
import com.krono.data.entity.RuleSourceEntity
import com.krono.data.entity.SourceDefinitionEntity

object RuleMappers {

    fun toDomain(
        revisionEntity: RuleRevisionEntity,
        branches: List<BranchEntity>,
        actions: List<ActionEntity>,
        sources: List<RuleSourceEntity>,
        sourceDefinitions: Map<String, SourceDefinitionEntity> = emptyMap()
    ): RuleDefinition {
        val actionsByBranch = actions.groupBy { it.branchId }
        val domainBranches = mutableListOf<BranchDefinition>()
        var domainElseBranch: BranchDefinition? = null

        val sortedBranches = branches.sortedBy { it.position }
        for (b in sortedBranches) {
            val branchActions = actionsByBranch[b.branchId]?.sortedBy { it.position }?.map { a ->
                ActionDefinition(
                    actionId = a.actionId,
                    kind = ActionKind.valueOf(a.actionKind),
                    assetId = a.assetId,
                    wallpaperTarget = a.wallpaperTarget?.let { WallpaperTarget.valueOf(it) },
                    volumePercent = a.volumePercent
                )
            } ?: emptyList()

            val sourceDef = b.predicateSourceId?.let { sourceDefinitions[it] }
            val predicate = ConditionPredicate(
                kind = PredicateKind.valueOf(b.predicateKind),
                areaId = b.predicateSourceId,
                deviceId = b.predicateSourceId,
                latitude = sourceDef?.latitude,
                longitude = sourceDef?.longitude,
                radiusM = sourceDef?.radiusM
            )

            val branch = BranchDefinition(
                branchId = b.branchId,
                kind = BranchKind.valueOf(b.branchKind),
                condition = predicate,
                actions = branchActions
            )

            if (branch.kind == BranchKind.ELSE) {
                domainElseBranch = branch
            } else {
                domainBranches.add(branch)
            }
        }

        val triggerFamily = TriggerFamily.valueOf(revisionEntity.triggerFamily)
        val trigger = when (triggerFamily) {
            TriggerFamily.LOCATION -> TriggerDefinition(
                family = triggerFamily,
                kind = TriggerKind.valueOf(revisionEntity.triggerKind),
                areaIds = sources.map { it.sourceId }
            )
            TriggerFamily.BLUETOOTH -> TriggerDefinition(
                family = triggerFamily,
                kind = TriggerKind.valueOf(revisionEntity.triggerKind),
                deviceId = sources.firstOrNull()?.sourceId
            )
        }

        return RuleDefinition(
            ruleId = revisionEntity.ruleId,
            revision = revisionEntity.revision,
            name = revisionEntity.name,
            mode = RuleMode.valueOf(revisionEntity.mode),
            trigger = trigger,
            branches = domainBranches,
            elseBranch = domainElseBranch,
            ruleFormatVersion = revisionEntity.ruleFormatVersion,
            semanticsVersion = revisionEntity.semanticsVersion
        )
    }

    fun toEntities(
        rule: RuleDefinition,
        nowUtcMs: Long
    ): Pair<RuleRevisionEntity, Triple<List<BranchEntity>, List<ActionEntity>, List<RuleSourceEntity>>> {
        val revisionEntity = RuleRevisionEntity(
            ruleId = rule.ruleId,
            revision = rule.revision,
            name = rule.name,
            mode = rule.mode.name,
            triggerFamily = rule.trigger.family.name,
            triggerKind = rule.trigger.kind.name,
            ruleFormatVersion = rule.ruleFormatVersion,
            semanticsVersion = rule.semanticsVersion,
            createdAtUtcMs = nowUtcMs
        )

        val allBranches = mutableListOf<BranchDefinition>()
        allBranches.addAll(rule.branches)
        rule.elseBranch?.let { allBranches.add(it) }

        val branchEntities = allBranches.mapIndexed { index, b ->
            BranchEntity(
                ruleId = rule.ruleId,
                revision = rule.revision,
                branchId = b.branchId,
                position = index,
                branchKind = b.kind.name,
                predicateKind = b.condition.kind.name,
                predicateSourceId = b.condition.areaId ?: b.condition.deviceId,
                predicateSourceRevision = 1
            )
        }

        val actionEntities = mutableListOf<ActionEntity>()
        allBranches.forEach { b ->
            b.actions.forEachIndexed { index, a ->
                actionEntities.add(
                    ActionEntity(
                        ruleId = rule.ruleId,
                        revision = rule.revision,
                        actionId = a.actionId,
                        branchId = b.branchId,
                        position = index,
                        actionKind = a.kind.name,
                        assetId = a.assetId,
                        wallpaperTarget = a.wallpaperTarget?.name,
                        volumePercent = a.volumePercent
                    )
                )
            }
        }

        val sourceEntities = when (rule.trigger.family) {
            TriggerFamily.LOCATION -> rule.trigger.areaIds.map { areaId ->
                RuleSourceEntity(
                    ruleId = rule.ruleId,
                    revision = rule.revision,
                    sourceId = areaId,
                    sourceRevision = 1
                )
            }
            TriggerFamily.BLUETOOTH -> listOfNotNull(
                rule.trigger.deviceId?.let { devId ->
                    RuleSourceEntity(
                        ruleId = rule.ruleId,
                        revision = rule.revision,
                        sourceId = devId,
                        sourceRevision = 1
                    )
                }
            )
        }

        return Pair(revisionEntity, Triple(branchEntities, actionEntities, sourceEntities))
    }
}
