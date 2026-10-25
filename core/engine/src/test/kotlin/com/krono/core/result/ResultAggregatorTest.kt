package com.krono.core.result

import org.junit.Assert.assertEquals
import org.junit.Test

class ResultAggregatorTest {

    @Test
    fun allAppliedResultsInSuccess() {
        val results = listOf(
            ActionExecutionResult(actionId = "a1", status = ActionExecutionStatus.APPLIED),
            ActionExecutionResult(actionId = "a2", status = ActionExecutionStatus.APPLIED)
        )
        assertEquals(RuleExecutionSummary.SUCCESS, ResultAggregator.aggregate(results))
    }

    @Test
    fun allNoChangeNeededResultsInSuccess() {
        val results = listOf(
            ActionExecutionResult(actionId = "a1", status = ActionExecutionStatus.NO_CHANGE_NEEDED)
        )
        assertEquals(RuleExecutionSummary.SUCCESS, ResultAggregator.aggregate(results))
    }

    @Test
    fun reT30_firstAppliedSecondFailed_resultsInPartial() {
        val results = listOf(
            ActionExecutionResult(actionId = "a1", status = ActionExecutionStatus.APPLIED),
            ActionExecutionResult(actionId = "a2", status = ActionExecutionStatus.FAILED, reason = "IO failure"),
            ActionExecutionResult(actionId = "a3", status = ActionExecutionStatus.SKIPPED)
        )
        assertEquals(RuleExecutionSummary.PARTIAL, ResultAggregator.aggregate(results))
    }

    @Test
    fun reT31_noAppliedWithUnverified_resultsInUnverified() {
        val results = listOf(
            ActionExecutionResult(actionId = "a1", status = ActionExecutionStatus.UNVERIFIED),
            ActionExecutionResult(actionId = "a2", status = ActionExecutionStatus.SKIPPED)
        )
        assertEquals(RuleExecutionSummary.UNVERIFIED, ResultAggregator.aggregate(results))
    }

    @Test
    fun noAppliedWithFailed_resultsInFailed() {
        val results = listOf(
            ActionExecutionResult(actionId = "a1", status = ActionExecutionStatus.FAILED, reason = "Permission denied"),
            ActionExecutionResult(actionId = "a2", status = ActionExecutionStatus.SKIPPED)
        )
        assertEquals(RuleExecutionSummary.FAILED, ResultAggregator.aggregate(results))
    }

    @Test
    fun noAppliedNoFailedWithBlocked_resultsInBlocked() {
        val results = listOf(
            ActionExecutionResult(actionId = "a1", status = ActionExecutionStatus.BLOCKED, reason = "Context unavailable")
        )
        assertEquals(RuleExecutionSummary.BLOCKED, ResultAggregator.aggregate(results))
    }

    @Test
    fun emptyOrCancelledOnly_resultsInSkipped() {
        val results = listOf(
            ActionExecutionResult(actionId = "a1", status = ActionExecutionStatus.CANCELLED)
        )
        assertEquals(RuleExecutionSummary.SKIPPED, ResultAggregator.aggregate(results))
        assertEquals(RuleExecutionSummary.SKIPPED, ResultAggregator.aggregate(emptyList()))
    }
}
