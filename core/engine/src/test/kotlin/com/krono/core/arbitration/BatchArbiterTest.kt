package com.krono.core.arbitration

import com.krono.core.fixtures.KronoFixtures
import com.krono.core.model.ActionDefinition
import com.krono.core.model.ActionKind
import com.krono.core.model.BranchDefinition
import com.krono.core.model.BranchKind
import com.krono.core.model.ConditionPredicate
import com.krono.core.model.PredicateKind
import com.krono.core.model.WallpaperTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatchArbiterTest {

    @Test
    fun reT17_twoCandidatesSameTarget_higherPriorityWins() {
        val branchHigh = BranchDefinition(
            branchId = "bh",
            kind = BranchKind.ALWAYS,
            condition = ConditionPredicate(kind = PredicateKind.ALWAYS),
            actions = listOf(
                ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20)
            )
        )
        val branchLow = BranchDefinition(
            branchId = "bl",
            kind = BranchKind.ALWAYS,
            condition = ConditionPredicate(kind = PredicateKind.ALWAYS),
            actions = listOf(
                ActionDefinition(actionId = "a2", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 50)
            )
        )

        val candidateHigh = ArbitrationCandidate(
            ruleId = "r-high",
            ruleRevision = 1,
            activationGeneration = 1,
            priorityPosition = 0,
            branch = branchHigh
        )
        val candidateLow = ArbitrationCandidate(
            ruleId = "r-low",
            ruleRevision = 1,
            activationGeneration = 1,
            priorityPosition = 1,
            branch = branchLow
        )

        val result = BatchArbiter.arbitrate(listOf(candidateLow, candidateHigh))
        assertEquals(1, result.winners.size)
        assertEquals("r-high", result.winners[0].candidate.ruleId)

        val lowDecision = result.decisions.first { it is ArbitrationDecision.ConflictLost }
        assertEquals("r-low", (lowDecision as ArbitrationDecision.ConflictLost).candidate.ruleId)
    }

    @Test
    fun reT18_lowerCandidateMultiActionOneTargetConflicts_wholeBranchConflictLost() {
        val branchHighWallpaper = BranchDefinition(
            branchId = "bw",
            kind = BranchKind.ALWAYS,
            condition = ConditionPredicate(kind = PredicateKind.ALWAYS),
            actions = listOf(
                ActionDefinition(
                    actionId = "aw",
                    kind = ActionKind.SET_WALLPAPER,
                    assetId = KronoFixtures.ASSET_IMAGE_A_ID,
                    wallpaperTarget = WallpaperTarget.SYSTEM
                )
            )
        )
        val branchLowMulti = BranchDefinition(
            branchId = "bm",
            kind = BranchKind.ALWAYS,
            condition = ConditionPredicate(kind = PredicateKind.ALWAYS),
            actions = listOf(
                ActionDefinition(actionId = "av", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 30),
                ActionDefinition(
                    actionId = "aw2",
                    kind = ActionKind.SET_WALLPAPER,
                    assetId = KronoFixtures.ASSET_IMAGE_B_ID,
                    wallpaperTarget = WallpaperTarget.SYSTEM
                )
            )
        )

        val candidateHigh = ArbitrationCandidate(
            ruleId = "r-wallpaper",
            ruleRevision = 1,
            activationGeneration = 1,
            priorityPosition = 0,
            branch = branchHighWallpaper
        )
        val candidateLow = ArbitrationCandidate(
            ruleId = "r-multi",
            ruleRevision = 1,
            activationGeneration = 1,
            priorityPosition = 1,
            branch = branchLowMulti
        )

        val result = BatchArbiter.arbitrate(listOf(candidateHigh, candidateLow))
        assertEquals(1, result.winners.size)
        assertEquals("r-wallpaper", result.winners[0].candidate.ruleId)

        val lostDecision = result.decisions.first { it is ArbitrationDecision.ConflictLost } as ArbitrationDecision.ConflictLost
        assertEquals("r-multi", lostDecision.candidate.ruleId)
        assertTrue(lostDecision.conflictingTargets.contains("wallpaper.system"))
    }

    @Test
    fun reT19_highestCandidatePreflightBlocked_doesNotReserveTargets() {
        val branchHigh = BranchDefinition(
            branchId = "bh",
            kind = BranchKind.ALWAYS,
            condition = ConditionPredicate(kind = PredicateKind.ALWAYS),
            actions = listOf(
                ActionDefinition(actionId = "a1", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 20)
            )
        )
        val branchLow = BranchDefinition(
            branchId = "bl",
            kind = BranchKind.ALWAYS,
            condition = ConditionPredicate(kind = PredicateKind.ALWAYS),
            actions = listOf(
                ActionDefinition(actionId = "a2", kind = ActionKind.SET_MEDIA_VOLUME, volumePercent = 50)
            )
        )

        val candidateHigh = ArbitrationCandidate(
            ruleId = "r-high",
            ruleRevision = 1,
            activationGeneration = 1,
            priorityPosition = 0,
            branch = branchHigh,
            isPreflightReady = false
        )
        val candidateLow = ArbitrationCandidate(
            ruleId = "r-low",
            ruleRevision = 1,
            activationGeneration = 1,
            priorityPosition = 1,
            branch = branchLow,
            isPreflightReady = true
        )

        val result = BatchArbiter.arbitrate(listOf(candidateHigh, candidateLow))
        assertEquals(1, result.winners.size)
        assertEquals("r-low", result.winners[0].candidate.ruleId)

        val blockedDecision = result.decisions.first { it is ArbitrationDecision.PreflightBlocked } as ArbitrationDecision.PreflightBlocked
        assertEquals("r-high", blockedDecision.candidate.ruleId)
    }
}
