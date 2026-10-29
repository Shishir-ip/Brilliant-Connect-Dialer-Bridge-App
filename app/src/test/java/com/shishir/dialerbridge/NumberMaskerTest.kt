package com.shishir.dialerbridge

import com.shishir.dialerbridge.util.NumberMasker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberMaskerTest {

    @Test
    fun `mask keeps prefix and suffix only`() {
        val masked = NumberMasker.mask("+8801712345678")
        assertTrue(masked.startsWith("+8801"))
        assertTrue(masked.endsWith("5678"))
        assertTrue(masked.contains("*"))
        assertFalse(masked.contains("712345"))
    }

    @Test
    fun `mask handles short input`() {
        assertEquals("*", NumberMasker.mask("1"))
        assertEquals("****", NumberMasker.mask("1234"))
    }

    @Test
    fun `maskInText hides numbers inside log lines`() {
        val line = "Handoff started for +8801712345678 to Brilliant Connect"
        val masked = NumberMasker.maskInText(line)
        assertFalse(masked.contains("712345"))
        assertTrue(masked.contains("Brilliant Connect"))
    }

    @Test
    fun `mask is stable for empty strings`() {
        assertEquals("", NumberMasker.mask(""))
    }
}
