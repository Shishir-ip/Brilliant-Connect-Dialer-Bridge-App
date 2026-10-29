package com.shishir.dialerbridge

import com.shishir.dialerbridge.util.NumberNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberNormalizerTest {

    @Test
    fun `clean keeps digits plus star hash and leading plus`() {
        assertEquals("+8801712345678", NumberNormalizer.clean("+880 171-234-5678"))
        assertEquals("*123#", NumberNormalizer.clean(" *123# "))
        assertEquals("01712345678", NumberNormalizer.clean("(017) 1234-5678"))
    }

    @Test
    fun `clean of empty input is empty`() {
        assertEquals("", NumberNormalizer.clean(""))
        assertEquals("", NumberNormalizer.clean("   "))
    }

    @Test
    fun `digitsOnly strips everything else`() {
        assertEquals("8801712345678", NumberNormalizer.digitsOnly("+88 (017) 1234-5678"))
        assertEquals("", NumberNormalizer.digitsOnly("abc"))
    }

    @Test
    fun `numbersMatch compares last 10 digits`() {
        assertTrue(NumberNormalizer.numbersMatch("+8801712345678", "01712345678"))
        assertTrue(NumberNormalizer.numbersMatch("8801712345678", "1712345678"))
        assertFalse(NumberNormalizer.numbersMatch("01712345678", "01712345679"))
    }

    @Test
    fun `numbersMatch requires exact equality for short codes`() {
        assertTrue(NumberNormalizer.numbersMatch("12345", "12345"))
        assertFalse(NumberNormalizer.numbersMatch("12345", "123456"))
        assertFalse(NumberNormalizer.numbersMatch("", "12345"))
    }

    @Test
    fun `toE164 normalizes Bangladeshi formats`() {
        assertEquals("+8801712345678", NumberNormalizer.toE164("01712345678"))
        assertEquals("+8801712345678", NumberNormalizer.toE164("8801712345678"))
        assertEquals("+8801712345678", NumberNormalizer.toE164("008801712345678"))
        assertEquals("+15551234567", NumberNormalizer.toE164("+15551234567"))
    }

    @Test
    fun `toLocal reduces Bangladeshi formats to 11 digits`() {
        assertEquals("01712345678", NumberNormalizer.toLocal("+8801712345678"))
        assertEquals("01712345678", NumberNormalizer.toLocal("8801712345678"))
        assertEquals("01712345678", NumberNormalizer.toLocal("008801712345678"))
        assertEquals("01712345678", NumberNormalizer.toLocal("01712345678"))
    }

    @Test
    fun `format honours option`() {
        val input = "+8801712345678"
        assertEquals("+8801712345678", NumberNormalizer.format(input, NumberNormalizer.FormatOption.E164))
        assertEquals("01712345678", NumberNormalizer.format(input, NumberNormalizer.FormatOption.LOCAL))
        assertEquals("+8801712345678", NumberNormalizer.format(input, NumberNormalizer.FormatOption.AS_DIALED))
    }

    @Test
    fun `formatOptionOf maps preference keys`() {
        assertEquals(NumberNormalizer.FormatOption.E164, NumberNormalizer.formatOptionOf("e164"))
        assertEquals(NumberNormalizer.FormatOption.LOCAL, NumberNormalizer.formatOptionOf("local"))
        assertEquals(NumberNormalizer.FormatOption.AS_DIALED, NumberNormalizer.formatOptionOf("as_dialed"))
        assertEquals(NumberNormalizer.FormatOption.AS_DIALED, NumberNormalizer.formatOptionOf("unknown"))
    }

    @Test
    fun `extractLast10Digits keeps short input intact`() {
        assertEquals("2345678", NumberNormalizer.extractLast10Digits("2345678"))
        assertEquals("6789012345", NumberNormalizer.extractLast10Digits("123456789012345"))
    }
}
