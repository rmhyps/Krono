package com.krono.core.ports

import com.krono.core.model.ActionDefinition

sealed class CapabilityStatus {
    object Ready : CapabilityStatus()
    data class Blocked(val reason: String) : CapabilityStatus()
    data class Unknown(val reason: String) : CapabilityStatus()
}

interface CapabilityPort {
    fun checkCapability(action: ActionDefinition): CapabilityStatus
}
