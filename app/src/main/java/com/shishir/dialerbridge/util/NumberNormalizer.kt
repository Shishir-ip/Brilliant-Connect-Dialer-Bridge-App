package com.shishir.dialerbridge.util

/**
 * Pure number formatting/matching helpers used by the handoff pipeline.
 * Keeping this free of Android dependencies makes it unit-testable.
 */
object NumberNormalizer {

    enum class FormatOption {
        AS_DIALED,
        E164,
        LOCAL
    }

    /** Strips whitespace and formatting characters, keeping digits, *, # and a leading +. */
    fun clean(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        val hasPlus = trimmed.startsWith("+")
        val cleanChars = trimmed.filter { it.isDigit() || it == '*' || it == '#' }
        return if (hasPlus) "+$cleanChars" else cleanChars
    }

    fun digitsOnly(input: String): String = input.filter { it.isDigit() }

    fun extractLast10Digits(input: String): String {
        val digits = digitsOnly(input)
        return if (digits.length >= 10) digits.takeLast(10) else digits
    }

    /**
     * Bangladesh-focused matching: numbers are considered equal when the last
     * 10 digits match (ignoring country/area prefixes), or exact match for short codes.
     */
    fun numbersMatch(first: String, second: String): Boolean {
        val d1 = digitsOnly(first)
        val d2 = digitsOnly(second)
        if (d1.isEmpty() || d2.isEmpty()) return false
        if (d1.length >= 10 && d2.length >= 10) {
            return d1.takeLast(10) == d2.takeLast(10)
        }
        return d1 == d2
    }

    fun toE164(input: String): String {
        val cleaned = clean(input)
        if (cleaned.isEmpty()) return ""

        val digits = digitsOnly(cleaned)

        if (digits.length == 11 && digits.startsWith("01")) return "+88$digits"
        if (digits.length == 13 && digits.startsWith("8801")) return "+$digits"
        if (digits.length == 15 && digits.startsWith("008801")) return "+${digits.substring(2)}"

        if (cleaned.startsWith("+")) return cleaned
        if (cleaned.startsWith("00") && cleaned.length > 4) return "+" + cleaned.substring(2)

        return cleaned
    }

    fun toLocal(input: String): String {
        val cleaned = clean(input)
        if (cleaned.isEmpty()) return ""

        val digits = digitsOnly(cleaned)

        if (digits.length == 15 && digits.startsWith("008801")) return digits.takeLast(11)
        if (digits.length == 13 && digits.startsWith("8801")) return digits.takeLast(11)
        if (digits.length == 11 && digits.startsWith("01")) return digits

        return cleaned
    }

    fun format(input: String, option: FormatOption): String = when (option) {
        FormatOption.AS_DIALED -> clean(input)
        FormatOption.E164 -> toE164(input)
        FormatOption.LOCAL -> toLocal(input)
    }

    /** Maps the persisted preference value to a [FormatOption]. */
    fun formatOptionOf(preference: String): FormatOption = when (preference) {
        "e164" -> FormatOption.E164
        "local" -> FormatOption.LOCAL
        else -> FormatOption.AS_DIALED
    }
}
