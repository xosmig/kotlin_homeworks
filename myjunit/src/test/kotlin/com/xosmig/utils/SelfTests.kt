package com.xosmig.utils

import com.xosmig.myjunit.MTest
import org.junit.Assert.assertTrue

class SelfTests {
    @MTest(expected = AssertionError::class)
    fun testWithExpected() {
        assertTrue(false)
    }

    @MTest(ignore = "This test should be ignored.")
    fun testWithIgnore() {
        assertTrue(false)
    }
}
