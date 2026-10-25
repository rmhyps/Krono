package com.krono.runtime.coordinator

import java.util.UUID

object RuntimeEpoch {
    val current: String = "epoch-" + UUID.randomUUID().toString()
}
