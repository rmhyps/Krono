package com.krono.app.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.krono.app.hub.KronoRuntimeHub
import com.krono.core.evaluator.BranchSelectionResult
import com.krono.core.evaluator.EvaluationSnapshot
import com.krono.core.evaluator.RuleEvaluator
import com.krono.core.model.ActionDefinition
import com.krono.core.model.RuleDefinition
import com.krono.core.result.ActionExecutionResult
import com.krono.data.entity.ExecutionHistoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class KronoViewModel(application: Application) : AndroidViewModel(application) {

    val hub = KronoRuntimeHub.getInstance(application)
    val store = hub.store
    val coordinator = hub.coordinator
    val assetRepository = hub.assetRepository

    private val _rules = MutableStateFlow<List<Pair<RuleDefinition, Boolean>>>(emptyList())
    val rules: StateFlow<List<Pair<RuleDefinition, Boolean>>> = _rules.asStateFlow()

    private val _history = MutableStateFlow<List<ExecutionHistoryEntity>>(emptyList())
    val history: StateFlow<List<ExecutionHistoryEntity>> = _history.asStateFlow()

    private val _simulationResult = MutableStateFlow<BranchSelectionResult?>(null)
    val simulationResult: StateFlow<BranchSelectionResult?> = _simulationResult.asStateFlow()

    private val _testActionResult = MutableStateFlow<ActionExecutionResult?>(null)
    val testActionResult: StateFlow<ActionExecutionResult?> = _testActionResult.asStateFlow()

    init {
        viewModelScope.launch {
            refreshRules()
            refreshHistory()
            hub.syncMonitoring()
        }

        viewModelScope.launch {
            hub.onHistoryUpdated.collectLatest {
                refreshHistory()
            }
        }
    }

    fun refreshRules() {
        viewModelScope.launch {
            _rules.value = store.getAllRulesWithStatus()
        }
    }

    fun deleteRule(ruleId: String) {
        viewModelScope.launch {
            store.deleteRule(ruleId)
            refreshRules()
            hub.syncMonitoring()
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            _history.value = store.getExecutionHistory(limit = 50)
        }
    }

    fun toggleRuleActive(ruleId: String, currentActive: Boolean) {
        viewModelScope.launch {
            store.setRuleActive(ruleId, !currentActive)
            refreshRules()
            hub.syncMonitoring()
        }
    }

    fun saveRule(rule: RuleDefinition, active: Boolean) {
        android.util.Log.d("KRONO_DIAG", "[VM] saveRule ruleId=${rule.ruleId} name='${rule.name}' family=${rule.trigger.family} active=$active")
        viewModelScope.launch {
            store.saveRule(rule)
            store.setRuleActive(rule.ruleId, active)
            refreshRules()
            hub.syncMonitoring()
        }
    }

    fun simulateRule(rule: RuleDefinition, snapshot: EvaluationSnapshot) {
        val result = RuleEvaluator.selectBranch(rule, snapshot)
        _simulationResult.value = result
    }

    fun clearSimulationResult() {
        _simulationResult.value = null
    }

    fun executeTestAction(action: ActionDefinition) {
        android.util.Log.d("KRONO_DIAG", "[VM] executeTestAction manual test actionId=${action.actionId} kind=${action.kind} assetId=${action.assetId} target=${action.wallpaperTarget}")
        viewModelScope.launch {
            val result = hub.executeTestAction(action)
            _testActionResult.value = result
        }
    }

    fun clearTestActionResult() {
        _testActionResult.value = null
    }

    fun clearHistory() {
        viewModelScope.launch {
            store.clearHistory(clearThroughSeq = Long.MAX_VALUE)
            refreshHistory()
        }
    }

    suspend fun importWallpaperImage(uri: android.net.Uri): String? {
        val resolver = getApplication<Application>().contentResolver
        return try {
            resolver.openInputStream(uri)?.use { stream ->
                val entity = assetRepository.importAsset(stream, resolver.getType(uri) ?: "image/jpeg")
                entity?.assetId
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun importCroppedWallpaper(bitmap: Bitmap): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val byteOutputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteOutputStream)
            val inputStream = java.io.ByteArrayInputStream(byteOutputStream.toByteArray())
            val entity = assetRepository.importAsset(inputStream, "image/png")
            entity?.assetId
        } catch (_: Exception) {
            null
        }
    }

    fun getAssetFile(assetId: String): File {
        return assetRepository.getAssetFile("$assetId.bin")
    }
}
