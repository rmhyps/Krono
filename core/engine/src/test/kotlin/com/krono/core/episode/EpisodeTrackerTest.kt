package com.krono.core.episode

import com.krono.core.evaluator.BluetoothConnectionState
import com.krono.core.evaluator.BranchSelectionResult
import com.krono.core.fixtures.KronoFixtures
import com.krono.core.model.BranchDefinition
import com.krono.core.model.BranchKind
import com.krono.core.model.ConditionPredicate
import com.krono.core.model.PredicateKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodeTrackerTest {

    @Test
    fun reT09_bluetoothMultipleProfilesSameEpisode_resultsInDuplicateReport() {
        val tracker = BluetoothEpisodeTracker()
        val deviceId = KronoFixtures.TWS_DEVICE_ID

        val first = tracker.onConnectionStateChanged(deviceId, BluetoothConnectionState.CONNECTED)
        assertTrue(first is BluetoothTransitionResult.NewEpisode)
        val firstKey = (first as BluetoothTransitionResult.NewEpisode).episodeKey

        val second = tracker.onConnectionStateChanged(deviceId, BluetoothConnectionState.CONNECTED)
        assertTrue(second is BluetoothTransitionResult.DuplicateReport)
        val secondKey = (second as BluetoothTransitionResult.DuplicateReport).episodeKey

        assertEquals(firstKey, secondKey)
    }

    @Test
    fun reT10_bluetoothDisconnectThenReconnect_createsNewEpisode() {
        val tracker = BluetoothEpisodeTracker()
        val deviceId = KronoFixtures.TWS_DEVICE_ID

        val first = tracker.onConnectionStateChanged(deviceId, BluetoothConnectionState.CONNECTED)
        val firstKey = (first as BluetoothTransitionResult.NewEpisode).episodeKey

        val disconnect = tracker.onConnectionStateChanged(deviceId, BluetoothConnectionState.DISCONNECTED)
        assertTrue(disconnect is BluetoothTransitionResult.Disconnected)

        val reconnect = tracker.onConnectionStateChanged(deviceId, BluetoothConnectionState.CONNECTED)
        assertTrue(reconnect is BluetoothTransitionResult.NewEpisode)
        val secondKey = (reconnect as BluetoothTransitionResult.NewEpisode).episodeKey

        assertNotEquals(firstKey, secondKey)
    }

    @Test
    fun bluetoothUnknownState_doesNotOpenNewEpisodeOrCloseExisting() {
        val tracker = BluetoothEpisodeTracker()
        val deviceId = KronoFixtures.TWS_DEVICE_ID

        tracker.onConnectionStateChanged(deviceId, BluetoothConnectionState.CONNECTED)
        val activeKeyBefore = tracker.getActiveEpisode(deviceId)

        val unknownResult = tracker.onConnectionStateChanged(deviceId, BluetoothConnectionState.UNKNOWN)
        assertTrue(unknownResult is BluetoothTransitionResult.Ignored)

        val activeKeyAfter = tracker.getActiveEpisode(deviceId)
        assertEquals(activeKeyBefore, activeKeyAfter)
    }

    @Test
    fun reT12_locationA_unknown_A_retainsSameEpisode() {
        val tracker = LocationEpisodeTracker("rule-loc-1")
        val branchA = BranchDefinition(
            branchId = "branch-a",
            kind = BranchKind.IF,
            condition = ConditionPredicate(kind = PredicateKind.LOCATION_IN_AREA, areaId = KronoFixtures.AREA_A_ID),
            actions = emptyList()
        )

        val first = tracker.onBranchSelectionChanged(BranchSelectionResult.Match(branchA))
        assertTrue(first is LocationEpisodeTransition.NewBranchEpisode)
        val firstKey = (first as LocationEpisodeTransition.NewBranchEpisode).episodeKey

        val unknownStep = tracker.onBranchSelectionChanged(BranchSelectionResult.WaitingData("GPS weak"))
        assertTrue(unknownStep is LocationEpisodeTransition.WaitingDataRetained)
        assertEquals("branch-a", tracker.getActiveBranchId())
        assertEquals(firstKey, tracker.getActiveEpisodeKey())

        val second = tracker.onBranchSelectionChanged(BranchSelectionResult.Match(branchA))
        assertTrue(second is LocationEpisodeTransition.ExistingEpisodeContinued)
        val secondKey = (second as LocationEpisodeTransition.ExistingEpisodeContinued).episodeKey

        assertEquals(firstKey, secondKey)
    }

    @Test
    fun locationA_then_B_createsNewBranchEpisode() {
        val tracker = LocationEpisodeTracker("rule-loc-1")
        val branchA = BranchDefinition(
            branchId = "branch-a",
            kind = BranchKind.IF,
            condition = ConditionPredicate(kind = PredicateKind.LOCATION_IN_AREA, areaId = KronoFixtures.AREA_A_ID),
            actions = emptyList()
        )
        val branchB = BranchDefinition(
            branchId = "branch-b",
            kind = BranchKind.ELSE_IF,
            condition = ConditionPredicate(kind = PredicateKind.LOCATION_IN_AREA, areaId = KronoFixtures.AREA_B_ID),
            actions = emptyList()
        )

        val first = tracker.onBranchSelectionChanged(BranchSelectionResult.Match(branchA))
        val firstKey = (first as LocationEpisodeTransition.NewBranchEpisode).episodeKey

        val second = tracker.onBranchSelectionChanged(BranchSelectionResult.Match(branchB))
        assertTrue(second is LocationEpisodeTransition.NewBranchEpisode)
        val secondKey = (second as LocationEpisodeTransition.NewBranchEpisode).episodeKey

        assertNotEquals(firstKey, secondKey)
        assertEquals("branch-b", tracker.getActiveBranchId())
    }

    @Test
    fun episodeClaim_uniqueness() {
        val claim1 = EpisodeClaim(
            ruleId = "r1",
            ruleRevision = 1,
            activationGeneration = 1,
            branchId = "b1",
            episodeKey = "ep-1"
        )
        val claim2 = EpisodeClaim(
            ruleId = "r1",
            ruleRevision = 1,
            activationGeneration = 1,
            branchId = "b1",
            episodeKey = "ep-1"
        )
        val claim3 = EpisodeClaim(
            ruleId = "r1",
            ruleRevision = 1,
            activationGeneration = 1,
            branchId = "b1",
            episodeKey = "ep-2"
        )

        assertEquals(claim1.claimKey, claim2.claimKey)
        assertNotEquals(claim1.claimKey, claim3.claimKey)
    }
}
