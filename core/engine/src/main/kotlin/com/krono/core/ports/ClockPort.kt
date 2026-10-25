package com.krono.core.ports

interface ClockPort {
    fun nowElapsedMs(): Long
    fun nowUtcMs(): Long
}
