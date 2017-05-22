package com.xosmig.myjunit

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.ArrayList

/**
 * Class to manage and run tests from one class.
 *
 * @constructor Creates a [ClassTester] object.
 * @property [testClass] class with tests.
 * @param [testClass] class with tests.
 */
class ClassTester(val testClass: Class<*>) {
    private val methods: List<Method> = testClass.declaredMethods.asList()
    /**
     * All found [MTest] methods.
     */
    val tests = methods.filter { it.isAnnotationPresent(MTest::class.java) }
    /**
     * All found [MBefore] methods.
     */
    val beforeMethods = methods.filter { it.isAnnotationPresent(MBefore::class.java) }
    /**
     * All found [MAfter] methods.
     */
    val afterMethods = methods.filter { it.isAnnotationPresent(MAfter::class.java) }

    /**
     * Runs all tests successively.
     *
     * @return [List] of the tests' results.
     */
    fun runAllTests(): List<TestResult> {
        val res = ArrayList<TestResult>()
        runAllTestsImpl { result ->
            res.add(result)
        }
        return res
    }


    /**
     * Runs all tests successively. Runs [callback] after each test on its result.
     *
     * @param [callback] will be executed after each test on its result.
     */
    fun runAllTests(callback: (TestResult) -> Unit) = runAllTestsImpl(callback)

    private inline fun runAllTestsImpl(callback: (TestResult) -> Unit) {
        for (test in tests) {
            callback(runTest(test))
        }
    }

    /**
     * Runs one test with the given name.
     *
     * @throws [IllegalArgumentException] if there is no test with the given name.
     */
    fun runTest(testName: String): TestResult {
        val testMethod = tests.filter { it.name == testName }.firstOrNull()
                ?: throw IllegalArgumentException("Test with name $testName is not found.")
        return runTest(testMethod)
    }

    private fun runTest(testMethod: Method): TestResult {
        val annotation = testMethod.getAnnotation(MTest::class.java)
                ?: throw IllegalArgumentException("Not a test")

        if (annotation.ignore != "") {
            return TestResult.Ignored(testClass, testMethod, annotation.ignore)
        }

        val errors = ArrayList<Throwable>()
        var expectedHappened = (annotation.expected == NoErrorsExpected::class)
        fun processError(original: Throwable, fromTestInvocation: Boolean = false) {
            val cause = if (original is InvocationTargetException || original is InstantiationException) {
                original.cause
            } else {
                throw original
            } ?: return

            if (fromTestInvocation && annotation.expected == cause::class) {
                expectedHappened = true
                return
            }

            errors.add(cause)
        }

        var testExecuted = false
        val startTime = System.currentTimeMillis()

        try {
            val instance = testClass.newInstance()

            try {
                for (beforeMethod in beforeMethods) {
                    beforeMethod.invoke(instance)
                }
                try {
                    testExecuted = true
                    testMethod.invoke(instance)
                } catch (e: Throwable) {
                    processError(e, fromTestInvocation = true)
                }
            } catch (e: Throwable) {
                processError(e)
            }

            for (afterMethod in afterMethods) {
                try {
                    afterMethod.invoke(instance)
                } catch (e: Throwable) {
                    processError(e)
                }
            }
        } catch (e: Throwable) {
            processError(e)
        }

        val time = System.currentTimeMillis() - startTime
        if (testExecuted && !expectedHappened) {
            errors.add(ExpectedNotHappened(annotation.expected))
        }

        return if (errors.isNotEmpty()) {
            TestResult.Failed(testClass, testMethod, errors, time)
        } else {
            TestResult.Passed(testClass, testMethod, time)
        }
    }
}
