package com.krono.core.ports

import com.krono.core.model.ActionDefinition
import com.krono.core.result.ActionExecutionResult

interface EffectPort {
    suspend fun executeAction(action: ActionDefinition): ActionExecutionResult
}
