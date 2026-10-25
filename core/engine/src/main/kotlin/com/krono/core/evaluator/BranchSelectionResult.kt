package com.krono.core.evaluator

import com.krono.core.model.BranchDefinition

sealed class BranchSelectionResult {
    data class Match(val branch: BranchDefinition) : BranchSelectionResult()
    data class WaitingData(val reason: String) : BranchSelectionResult()
    object NoMatch : BranchSelectionResult()
}
