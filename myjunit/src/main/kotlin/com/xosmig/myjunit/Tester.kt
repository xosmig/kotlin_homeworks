package com.xosmig.myjunit

import com.xosmig.utils.loadHierarchy
import java.io.IOException
import java.nio.file.Path
import java.util.*

/**
 * Used to manage and run tests from a file system.
 *
 * @constructor Loads classes to be tested and classes with tests. Creates a [Tester] object.
 * @param [classPath] the path to the directory which is the root of the hierarchy of classes to be tested.
 * @param [testsPath] the path to the directory which is the root of the hierarchy of classes with tests.
 * @throws [IOException] if something went wrong in the process of loading classes.
 */
class Tester @Throws(IOException::class) constructor(classPath: Path, testsPath: Path) {
    /**
     * List of classes from the test hierarchy.
     */
    val testClasses: List<Class<*>>

    init {
        loadHierarchy(classPath)
        testClasses = loadHierarchy(testsPath)
    }

    /**
     * Runs all tests successively.
     *
     * @return [List] of the tests' results.
     */
    fun runAll(): Pair<List<TestResult>, Summary> {
        val results = ArrayList<TestResult>()
        val summary = runAll { testResult ->
            results.add(testResult)
        }
        return Pair(results, summary)
    }

    /**
     * Runs all found tests successively. Runs [callback] after each test on its result.
     *
     * @param [callback] will be executed after each test on its result.
     */
    fun runAll(callback: (TestResult) -> Unit): Summary {
        var successful = 0
        var failed = 0
        var ignored = 0
        val startTime = System.currentTimeMillis()

        for (clazz in testClasses) {
            ClassTester(clazz).runAllTests { testResult ->
                when (testResult) {
                    is TestResult.Passed -> {
                        successful += 1
                    }
                    is TestResult.Failed -> {
                        failed += 1
                    }
                    is TestResult.Ignored -> {
                        ignored += 1
                    }
                }
                callback(testResult)
            }
        }

        val totalTime = System.currentTimeMillis() - startTime
        return Summary(successful, failed, ignored, totalTime)
    }

    /**
     * Run summary. Contains all main information about run.
     *
     * @property [passed] number of passed tests.
     * @property [failed] number of passed tests.
     * @property [ignored] number of ignored tests.
     * @property [testsNumber] number of found tests.
     * @property [isSuccessful] true if all tests were either ignored or passed.
     */
    class Summary internal constructor(val passed: Int,
                                       val failed: Int,
                                       val ignored: Int,
                                       val totalTime: Long ) {

        val isSuccessful: Boolean get() = failed == 0
        val testsNumber: Int get() = passed + failed + ignored

        override fun toString(): String = """
        |Passed: $passed
        |Failed: $failed
        |Ignored: $ignored
        |Total: $testsNumber
        |
        |${ if (isSuccessful) { "SUCCESS" } else { "FAIL" } }
        |
        |Total time: $totalTime
        |""".trimMargin()
    }
}
