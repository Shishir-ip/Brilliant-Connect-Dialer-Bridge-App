package com.shishir.dialerbridge

import com.shishir.dialerbridge.util.SafetyChecker
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyCheckerTest {

    @Test
    fun `regular numbers are valid`() {
        assertTrue(SafetyChecker.check("01712345678") is SafetyChecker.SafetyResult.Valid)
        assertTrue(SafetyChecker.check("+8801712345678") is SafetyChecker.SafetyResult.Valid)
        assertTrue(SafetyChecker.check("0295551234") is SafetyChecker.SafetyResult.Valid)
    }

    @Test
    fun `emergency numbers are blocked`() {
        for (number in listOf("999", "112", "911", "100", "000")) {
            assertTrue(
                "Expected $number to be flagged as emergency",
                SafetyChecker.check(number) is SafetyChecker.SafetyResult.Emergency
            )
        }
        assertTrue(
            SafetyChecker.check("911") is SafetyChecker.SafetyResult.Emergency
        )
    }

    @Test
    fun `ussd codes are blocked`() {
        assertTrue(SafetyChecker.check("*123#") is SafetyChecker.SafetyResult.Ussd)
        assertTrue(SafetyChecker.check("#0#") is SafetyChecker.SafetyResult.Ussd)
        assertTrue(SafetyChecker.check("*544#") is SafetyChecker.SafetyResult.Ussd)
    }

    @Test
    fun `short and empty numbers are blocked`() {
        assertTrue(SafetyChecker.check("12") is SafetyChecker.SafetyResult.TooShort)
        assertTrue(SafetyChecker.check("") is SafetyChecker.SafetyResult.TooShort)
        assertTrue(SafetyChecker.check("   ") is SafetyChecker.SafetyResult.TooShort)
    }

    @Test
    fun `emergency check works with formatted input`() {
        assertTrue(SafetyChecker.isEmergency(" 999 "))
        assertTrue(SafetyChecker.isEmergency("+999"))
        assertTrue(!SafetyChecker.isEmergency("9999"))
    }
}
