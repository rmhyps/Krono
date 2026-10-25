package com.krono.core.episode

import com.krono.core.evaluator.BranchSelectionResult

sealed class LocationEpisodeTransition {
    data class NewBranchEpisode(val branchId: String, val episodeKey: String) : LocationEpisodeTransition()
    data class ExistingEpisodeContinued(val branchId: String, val episodeKey: String) : LocationEpisodeTransition()
    data class EpisodeClosed(val previousBranchId: String) : LocationEpisodeTransition()
    object WaitingDataRetained : LocationEpisodeTransition()
}

class LocationEpisodeTracker(
    private val ruleId: String
) {
    private var activeBranchId: String? = null
    private var activeEpisodeKey: String? = null
    private var episodeCounter = 0

    fun onBranchSelectionChanged(selection: BranchSelectionResult): LocationEpisodeTransition {
        return when (selection) {
            is BranchSelectionResult.Match -> {
                val matchedBranchId = selection.branch.branchId
                if (matchedBranchId == activeBranchId && activeEpisodeKey != null) {
                    LocationEpisodeTransition.ExistingEpisodeContinued(matchedBranchId, activeEpisodeKey!!)
                } else {
                    episodeCounter++
                    val newKey = "loc:$ruleId:$matchedBranchId:$episodeCounter"
                    activeBranchId = matchedBranchId
                    activeEpisodeKey = newKey
                    LocationEpisodeTransition.NewBranchEpisode(matchedBranchId, newKey)
                }
            }
            is BranchSelectionResult.WaitingData -> {
                LocationEpisodeTransition.WaitingDataRetained
            }
            is BranchSelectionResult.NoMatch -> {
                val previous = activeBranchId
                activeBranchId = null
                activeEpisodeKey = null
                if (previous != null) {
                    LocationEpisodeTransition.EpisodeClosed(previous)
                } else {
                    LocationEpisodeTransition.WaitingDataRetained
                }
            }
        }
    }

    fun getActiveBranchId(): String? = activeBranchId
    fun getActiveEpisodeKey(): String? = activeEpisodeKey
}
