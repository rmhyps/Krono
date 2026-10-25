package com.krono.core.episode

data class EpisodeClaim(
    val ruleId: String,
    val ruleRevision: Int,
    val activationGeneration: Int,
    val branchId: String,
    val episodeKey: String
) {
    val claimKey: String = "$ruleId:$ruleRevision:$activationGeneration:$branchId:$episodeKey"
}
