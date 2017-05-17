package xosmig.myjunit

import kotlin.reflect.KClass

/**
 * This [Throwable] within [TestResult.Fail.errors] means that
 * [MTest.expected] was set, but no errors happened during the test run.
 *
 * @property [expected] expected error that hasn't happened.
 */
class ExpectedNotHappened internal constructor(val expected: KClass<out Throwable>): Throwable()
