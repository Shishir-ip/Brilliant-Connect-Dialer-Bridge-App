package com.shishir.dialerbridge.util

/**
 * Guards against routing calls that must never go through a VoIP bridge:
 * emergency numbers, USSD codes and short/special numbers.
 */
object SafetyChecker {

    sealed class SafetyResult {
        data object Valid : SafetyResult()
        data class Emergency(val reason: String) : SafetyResult()
        data class Ussd(val reason: String) : SafetyResult()
        data class TooShort(val reason: String) : SafetyResult()
    }

    // Includes worldwide emergency short codes plus Bangladesh (999) and neighbors.
    private val KNOWN_EMERGENCY_NUMBERS = setOf(
        "999", "112", "911", "100", "101", "102", "108", "000", "110", "119", "991", "333"
    )

    fun isEmergency(rawNumber: String): Boolean {
        val clean = NumberNormalizer.clean(rawNumber)
        val digits = NumberNormalizer.digitsOnly(clean)
        return digits in KNOWN_EMERGENCY_NUMBERS || clean in KNOWN_EMERGENCY_NUMBERS
    }

    fun isUssd(rawNumber: String): Boolean {
        val trimmed = rawNumber.trim()
        return trimmed.startsWith("*") || trimmed.startsWith("#") || trimmed.endsWith("#")
    }

    fun isTooShort(rawNumber: String): Boolean {
        val digits = NumberNormalizer.digitsOnly(rawNumber)
        return digits.length < 3
    }

    fun check(rawNumber: String): SafetyResult {
        if (rawNumber.isBlank()) {
            return SafetyResult.TooShort("Dialed number is empty")
        }
        if (isEmergency(rawNumber)) {
            return SafetyResult.Emergency(
                "Emergency calls must be placed directly using your SIM card carrier"
            )
        }
        if (isUssd(rawNumber)) {
            return SafetyResult.Ussd(
                "USSD codes (* or #) cannot be routed through VoIP apps; use your SIM carrier"
            )
        }
        if (isTooShort(rawNumber)) {
            return SafetyResult.TooShort("Phone numbers must have at least 3 digits")
        }
        return SafetyResult.Valid
    }
}
