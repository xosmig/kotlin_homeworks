package xosmig.myjunit

import xosmig.utils.loadHierarchy
import java.nio.file.Path
import java.util.*

/**
 * Used to manage and run tests from a file system.
 *
 * @constructor Loads classes to be tested and classes with tests. Creates a [Tester] object.
 * @param [classPath] the path to the directory which is the root of the hierarchy of classes to be tested.
 * @param [testsPath] the path to the directory which is the root of the hierarchy of classes with tests.
 */
class Tester(classPath: Path, testsPath: Path) {
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
    fun runAll(): List<TestResult> {
        val res = ArrayList<TestResult>()
        runAll { result ->
            res.add(result)
        }
        return res
    }

    /**
     * Runs all found tests successively. Runs [callback] after each test on its result.
     *
     * @param [callback] will be executed after each test on its result.
     */
    fun runAll(callback: (TestResult) -> Unit) {
        for (clazz in testClasses) {
            ClassTester(clazz).runAllTests(callback)
        }
    }
}
