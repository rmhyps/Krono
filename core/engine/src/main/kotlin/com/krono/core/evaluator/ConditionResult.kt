package com.krono.core.evaluator

sealed class ConditionResult {
    object True : ConditionResult()
    object False : ConditionResult()
    data class Unknown(val reason: String) : ConditionResult()
}
