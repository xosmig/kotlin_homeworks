package xosmig.myjunit

import java.lang.reflect.Method
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Represents result of running one test.
 */
open class TestResult private constructor(val testClass: Class<*>, val test: Method) {

    val name get() = "${testClass.canonicalName}#${test.name}"

    /**
     * Means that the test was ignored.
     *
     * @property [comment] comment on ignoring the test.
     */
    class Ignored internal constructor(testClass: Class<*>, test: Method, val comment: String):
            TestResult(testClass, test) {

        override fun toString(): String = "Test $name ignored: \"$comment\""
    }

    /**
     * Means that test successfully passed.
     *
     * @property [time] time spent on the test and [MBefore] and [MAfter] methods in milliseconds.
     */
    class Success internal constructor(testClass: Class<*>, test: Method, val time: Long):
            TestResult(testClass, test) {

        override fun toString(): String = "Test $name passed in $time ms"
    }

    /**
     * Means that test failed.
     *
     * @property [time] time spent on the test and [MBefore] and [MAfter] methods in milliseconds.
     * @property [errors] [List] of all errors which happened during the test and [MBefore] and [MAfter] methods.
     */
    class Fail internal constructor(testClass: Class<*>, test: Method, val errors: List<Throwable>, val time: Long):
            TestResult(testClass, test) {

        override fun toString(): String {
            val writer = StringWriter()
            writer.append("Test $name failed in $time ms. See errors bellow.")
            for (error in errors) {
                error.printStackTrace(PrintWriter(writer))
            }
            return writer.toString()
        }
    }
}
