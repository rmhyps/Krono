package com.krono.core.validation

sealed class RuleValidationResult {
    object Valid : RuleValidationResult()
    data class Invalid(val errors: List<ValidationError>) : RuleValidationResult()
}

data class ValidationError(
    val field: String,
    val errorCode: String,
    val message: String
)
