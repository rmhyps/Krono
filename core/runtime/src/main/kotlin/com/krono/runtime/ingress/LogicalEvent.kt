package com.krono.runtime.ingress

data class LogicalEvent(
    val eventId: String,
    val eventKey: String,
    val eventKind: String,
    val causeKey: String,
    val receivedElapsedMs: Long,
    val receivedAtUtcMs: Long,
    val originCommandId: String? = null,
    val sourceId: String? = null,
    val sourceFamily: String
)
