package com.krono.app.hub

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.krono.core.evaluator.BluetoothConnectionState
import com.krono.core.evaluator.ConditionResult
import com.krono.core.evaluator.EvaluationSnapshot
import com.krono.core.fixtures.KronoFixtures
import com.krono.core.model.ActionDefinition
import com.krono.core.model.PredicateKind
import com.krono.core.model.RuleDefinition
import com.krono.core.model.TriggerFamily
import com.krono.core.ports.ClockPort
import com.krono.core.result.ActionExecutionResult
import com.krono.data.database.KronoDatabase
import com.krono.data.entity.ActionHistoryEntity
import com.krono.data.entity.ExecutionHistoryEntity
import com.krono.data.store.RoomAutomationStore
import com.krono.platform.bluetooth.BluetoothEventHandler
import com.krono.platform.effect.AndroidCapabilityManager
import com.krono.platform.effect.AndroidEffectAdapter
import com.krono.platform.effect.AudioEffectManager
import com.krono.platform.effect.WallpaperEffectManager
import com.krono.platform.location.AndroidMonitoringAdapter
import com.krono.platform.location.GeofenceArea
import com.krono.platform.location.GeofenceEventHandler
import com.krono.platform.location.GeofenceManager
import com.krono.platform.location.KronoLocationMonitor
import com.krono.runtime.coordinator.KronoRuntimeCoordinator
import com.krono.runtime.coordinator.PlanExecutionReport
import com.krono.runtime.ingress.LogicalEvent
import com.krono.runtime.journal.EffectJournalRecorder
import com.krono.runtime.recovery.RuntimeRecoveryHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

class KronoRuntimeHub private constructor(private val appContext: Context) {

    private val errorHandler = CoroutineExceptionHandler { _, error ->
        Log.e("KronoRuntimeHub", "Pekerjaan otomasi gagal; periksa sumber dan database", error)
    }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + errorHandler)

    val database = KronoDatabase.getInstance(appContext)
    val store = RoomAutomationStore(database)
    val assetRepository = com.krono.data.repository.AssetRepository(appContext, database.kronoDao())

    val audioManager = AudioEffectManager(appContext)
    val wallpaperManager = WallpaperEffectManager(appContext)
    val ringerModeManager = com.krono.platform.effect.RingerModeEffectManager(appContext)
    val effectAdapter = AndroidEffectAdapter(audioManager, wallpaperManager, ringerModeManager)
    val capabilityManager = AndroidCapabilityManager(appContext)
    val geofenceManager = GeofenceManager(appContext)
    val locationMonitor = KronoLocationMonitor(appContext)
    val monitoringAdapter = AndroidMonitoringAdapter(geofenceManager, locationMonitor)
    val journalRecorder = EffectJournalRecorder()
    val recoveryHandler = RuntimeRecoveryHandler(journalRecorder)

    val clockPort = object : ClockPort {
        override fun nowElapsedMs(): Long = SystemClock.elapsedRealtime()
        override fun nowUtcMs(): Long = System.currentTimeMillis()
    }

    val coordinator = KronoRuntimeCoordinator(
        clockPort = clockPort,
        capabilityPort = capabilityManager,
        effectPort = effectAdapter,
        store = store,
        monitoringPort = monitoringAdapter,
        journalRecorder = journalRecorder,
        recoveryHandler = recoveryHandler
    )

    private val _onHistoryUpdated = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onHistoryUpdated: SharedFlow<Unit> = _onHistoryUpdated.asSharedFlow()
    val processSessionId: String = UUID.randomUUID().toString().take(8)

    init {
        android.util.Log.d("KRONO_DIAG", "[HUB] Initializing KronoRuntimeHub processSessionId=$processSessionId pid=${android.os.Process.myPid()}")
        scope.launch {
            coordinator.initialize()
            setupEventHandlers()
        }
    }

    fun setupEventHandlers() {
        val onLoc = { areaId: String, isInside: Boolean, elapsedMs: Long, utcMs: Long ->
            handleLocationTransition(areaId, isInside, elapsedMs, utcMs, null)
        }
        locationMonitor.onTransitionListener = onLoc
        GeofenceEventHandler.asyncListener = { areaId, isInside, elapsedMs, utcMs, onComplete ->
            handleLocationTransition(areaId, isInside, elapsedMs, utcMs, onComplete)
        }

        BluetoothEventHandler.listener = { deviceAddress, isConnected, elapsedMs, utcMs ->
            handleBluetoothTransition(deviceAddress, isConnected, elapsedMs, utcMs)
        }

        com.krono.platform.wifi.WifiEventHandler.listener = { ssid, isConnected, elapsedMs, utcMs ->
            handleWifiTransition(ssid, isConnected, elapsedMs, utcMs)
        }
    }

    fun handleLocationTransition(
        areaId: String,
        isInside: Boolean,
        elapsedMs: Long,
        utcMs: Long,
        onComplete: (() -> Unit)? = null
    ) {
        val traceId = UUID.randomUUID().toString().take(6)
        android.util.Log.d("KRONO_DIAG", "[HUB] handleLocationTransition() traceId=$traceId psid=$processSessionId areaId=$areaId isInside=$isInside elapsedMs=$elapsedMs")
        scope.launch {
            try {
                val activeRules = store.getActiveRules().filter { it.trigger.family == TriggerFamily.LOCATION }
                android.util.Log.d("KRONO_DIAG", "[HUB] traceId=$traceId found ${activeRules.size} active rules: ${activeRules.map { "${it.ruleId}:rev${it.revision}" }}")
                if (activeRules.isEmpty()) {
                    android.util.Log.w("KRONO_DIAG", "[HUB] traceId=$traceId ABORTED: No active location rules found in store")
                    return@launch
                }

                val allAreaStates = mutableMapOf<String, ConditionResult>()
                for (r in activeRules) {
                    for (b in r.branches) {
                        val bArea = b.condition.areaId ?: "area-${b.branchId}"
                        if (bArea == areaId) {
                            allAreaStates[bArea] = if (isInside) ConditionResult.True else ConditionResult.False
                        } else if (bArea !in allAreaStates) {
                            allAreaStates[bArea] = ConditionResult.False
                        }
                    }
                }

                val snapshot = EvaluationSnapshot(
                    areaStates = allAreaStates,
                    nowElapsedMs = elapsedMs,
                    nowUtcMs = utcMs
                )

                val event = LogicalEvent(
                    eventId = "ev-loc-" + UUID.randomUUID().toString().take(8),
                    eventKey = "ek-loc-$areaId:$isInside:$elapsedMs",
                    eventKind = "GEOFENCE_MEMBERSHIP_CHANGED",
                    causeKey = "cause-loc-$areaId:$isInside",
                    receivedElapsedMs = elapsedMs,
                    receivedAtUtcMs = utcMs,
                    sourceFamily = "LOCATION",
                    sourceId = areaId
                )
                android.util.Log.d("KRONO_DIAG", "[HUB] traceId=$traceId Dispatched onLogicalEvent to coordinator: event=${event.eventId} causeKey=${event.causeKey} areaStates=$allAreaStates")

                val reports = coordinator.onLogicalEvent(event, snapshot)
                android.util.Log.d("KRONO_DIAG", "[HUB] traceId=$traceId coordinator returned ${reports.size} reports")
                for (rep in reports) {
                    android.util.Log.d("KRONO_DIAG", "[HUB] traceId=$traceId report ruleId=${rep.ruleId} summary=${rep.summary}")
                    for (act in rep.actionResults) {
                        android.util.Log.d("KRONO_DIAG", "[HUB] traceId=$traceId action actionId=${act.actionId} status=${act.status} reason=${act.reason ?: ""}")
                    }
                }
                recordReportsToHistory(reports, utcMs, if (isInside) "Masuk $areaId" else "Keluar $areaId", areaId)
            } finally {
                onComplete?.invoke()
            }
        }
    }

    fun handleBluetoothTransition(deviceAddress: String, isConnected: Boolean, elapsedMs: Long, utcMs: Long) {
        scope.launch {
            val state = if (isConnected) BluetoothConnectionState.CONNECTED else BluetoothConnectionState.DISCONNECTED
            val snapshot = EvaluationSnapshot(
                bluetoothStates = mapOf(deviceAddress to state),
                nowElapsedMs = elapsedMs,
                nowUtcMs = utcMs
            )

            val event = LogicalEvent(
                eventId = "ev-bt-" + UUID.randomUUID().toString().take(8),
                eventKey = "ek-bt-$deviceAddress:$isConnected:$elapsedMs",
                eventKind = if (isConnected) "BLUETOOTH_CONNECTED" else "BLUETOOTH_DISCONNECTED",
                causeKey = "cause-bt-$deviceAddress:$isConnected",
                receivedElapsedMs = elapsedMs,
                receivedAtUtcMs = utcMs,
                sourceFamily = "BLUETOOTH",
                sourceId = deviceAddress
            )

            val reports = coordinator.onLogicalEvent(event, snapshot)
            recordReportsToHistory(reports, utcMs, if (isConnected) "Terhubung $deviceAddress" else "Terputus $deviceAddress", deviceAddress)
        }
    }

    fun handleWifiTransition(ssid: String, isConnected: Boolean, elapsedMs: Long, utcMs: Long) {
        val traceId = UUID.randomUUID().toString().take(6)
        android.util.Log.d("KRONO_DIAG", "[HUB] handleWifiTransition() traceId=$traceId ssid=$ssid isConnected=$isConnected elapsedMs=$elapsedMs")
        scope.launch {
            val activeRules = store.getActiveRules().filter { it.trigger.family == TriggerFamily.WIFI }
            android.util.Log.d("KRONO_DIAG", "[HUB] traceId=$traceId found ${activeRules.size} active Wi-Fi rules")
            if (activeRules.isEmpty()) {
                android.util.Log.w("KRONO_DIAG", "[HUB] traceId=$traceId ABORTED: No active Wi-Fi rules found in store")
                return@launch
            }

            val allWifiStates = mutableMapOf<String, com.krono.core.evaluator.WifiConnectionState>()
            for (r in activeRules) {
                val targetSsid = r.trigger.wifiSsid ?: continue
                if (targetSsid.equals(ssid, ignoreCase = true)) {
                    allWifiStates[targetSsid] = if (isConnected) {
                        com.krono.core.evaluator.WifiConnectionState.CONNECTED
                    } else {
                        com.krono.core.evaluator.WifiConnectionState.DISCONNECTED
                    }
                } else if (targetSsid !in allWifiStates) {
                    allWifiStates[targetSsid] = com.krono.core.evaluator.WifiConnectionState.DISCONNECTED
                }
            }

            val snapshot = EvaluationSnapshot(
                wifiStates = allWifiStates,
                nowElapsedMs = elapsedMs,
                nowUtcMs = utcMs
            )

            val event = LogicalEvent(
                eventId = "ev-wifi-" + UUID.randomUUID().toString().take(8),
                eventKey = "ek-wifi-$ssid:$isConnected:$elapsedMs",
                eventKind = if (isConnected) "WIFI_CONNECTED" else "WIFI_DISCONNECTED",
                causeKey = "cause-wifi-$ssid:$isConnected",
                receivedElapsedMs = elapsedMs,
                receivedAtUtcMs = utcMs,
                sourceFamily = "WIFI",
                sourceId = ssid
            )
            android.util.Log.d("KRONO_DIAG", "[HUB] traceId=$traceId Dispatched Wi-Fi event to coordinator: ${event.eventId}")

            val reports = coordinator.onLogicalEvent(event, snapshot)
            recordReportsToHistory(
                reports = reports,
                utcMs = utcMs,
                branchLabel = if (isConnected) "Terhubung Wi-Fi: $ssid" else "Terputus Wi-Fi: $ssid",
                sourceId = ssid
            )
        }
    }

    private suspend fun recordReportsToHistory(
        reports: List<PlanExecutionReport>,
        utcMs: Long,
        branchLabel: String,
        sourceId: String
    ) {
        if (reports.isEmpty()) return
        for (rep in reports) {
            val historyId = "hist-" + UUID.randomUUID().toString()
            store.recordExecutionHistory(
                ExecutionHistoryEntity(
                    historyId = historyId,
                    opportunityKey = "opp-" + rep.planId,
                    originEventSeq = System.currentTimeMillis(),
                    ruleIdSnapshot = rep.ruleId,
                    ruleNameSnapshot = rep.ruleId,
                    originKind = "AUTOMATION",
                    branchLabelSnapshot = branchLabel,
                    sourceLabelsJson = "[\"$sourceId\"]",
                    summaryResult = rep.summary.name,
                    startedAtUtcMs = utcMs,
                    finishedAtUtcMs = System.currentTimeMillis()
                ),
                rep.actionResults.mapIndexed { idx, act ->
                    ActionHistoryEntity(
                        historyId = historyId,
                        stepIndex = idx,
                        actionIdSnapshot = act.actionId,
                        actionKind = "ACTION",
                        targetSummaryJson = "{}",
                        resultCode = act.status.name,
                        evidenceLevel = "OBSERVED",
                        evidenceSummaryJson = act.evidenceSummary ?: act.reason
                    )
                }
            )
        }
        _onHistoryUpdated.tryEmit(Unit)
    }

    suspend fun syncMonitoring(startService: Boolean = true) {
        val activeRules = store.getActiveRules()
        val locationAreas = mutableListOf<GeofenceArea>()

        for (rule in activeRules) {
            if (rule.trigger.family == TriggerFamily.LOCATION) {
                for (branch in rule.branches) {
                    if (branch.condition.kind == PredicateKind.LOCATION_IN_AREA) {
                        val areaId = branch.condition.areaId ?: "area-${branch.branchId}"
                        val lat = branch.condition.latitude ?: -7.894303
                        val lng = branch.condition.longitude ?: 112.595458
                        val rad = branch.condition.radiusM ?: 150.0
                        locationAreas.add(GeofenceArea(areaId, lat, lng, rad))
                    }
                }
            }
        }
        android.util.Log.d("KRONO_DIAG", "[HUB] syncMonitoring(startService=$startService) activeRules=${activeRules.size} locationAreas=${locationAreas.map { it.areaId }}")

        if (locationAreas.isNotEmpty() && startService) {
            android.util.Log.d("KRONO_DIAG", "[HUB] syncMonitoring: Triggering KronoAutomationService.start()")
            val started = com.krono.app.service.KronoAutomationService.start(appContext)
            if (!started) {
                android.util.Log.w("KRONO_DIAG", "[HUB] syncMonitoring: KronoAutomationService could not be started (permissions missing or OS rejection)")
            }
        } else if (locationAreas.isEmpty()) {
            android.util.Log.d("KRONO_DIAG", "[HUB] syncMonitoring: locationAreas is empty, stopping KronoAutomationService")
            com.krono.app.service.KronoAutomationService.stop(appContext)
        }

        // A foreground service calls this again after startForeground succeeds.
        if (!startService && locationAreas.isNotEmpty()) {
            android.util.Log.d("KRONO_DIAG", "[HUB] syncMonitoring(!startService): Calling monitoringAdapter.updateAreas and checkCurrentLocation")
            monitoringAdapter.updateAreas(locationAreas)
        }

        if (!startService && locationAreas.isNotEmpty()) {
            locationMonitor.checkCurrentLocation { areaMap ->
                if (areaMap.isNotEmpty()) {
                    scope.launch {
                        val nowElapsed = SystemClock.elapsedRealtime()
                        val nowUtc = System.currentTimeMillis()

                        val snapshotAreaStates = areaMap.mapValues { (_, inside) ->
                            if (inside) ConditionResult.True else ConditionResult.False
                        }
                        val snapshot = EvaluationSnapshot(
                            areaStates = snapshotAreaStates,
                            nowElapsedMs = nowElapsed,
                            nowUtcMs = nowUtc
                        )

                        val matchedArea = areaMap.entries.firstOrNull { it.value }?.key
                        if (matchedArea != null) {
                            val event = LogicalEvent(
                                eventId = "ev-init-" + UUID.randomUUID().toString().take(8),
                                eventKey = "ek-init-$matchedArea:$nowElapsed",
                                eventKind = "GEOFENCE_MEMBERSHIP_CHANGED",
                                causeKey = "cause-init-$matchedArea",
                                receivedElapsedMs = nowElapsed,
                                receivedAtUtcMs = nowUtc,
                                sourceFamily = "LOCATION",
                                sourceId = matchedArea
                            )
                            val reports = coordinator.onLogicalEvent(event, snapshot)
                            recordReportsToHistory(reports, nowUtc, "Kondisi Lokasi Terkini Cocok ($matchedArea)", matchedArea)
                        }
                    }
                }
            }
        }
    }

    suspend fun executeTestAction(action: ActionDefinition): ActionExecutionResult {
        return coordinator.executeTestAction(action, UUID.randomUUID().toString())
    }

    companion object {
        @Volatile
        private var instance: KronoRuntimeHub? = null

        fun getInstance(context: Context): KronoRuntimeHub {
            return instance ?: synchronized(this) {
                instance ?: KronoRuntimeHub(context.applicationContext).also { instance = it }
            }
        }
    }
}
