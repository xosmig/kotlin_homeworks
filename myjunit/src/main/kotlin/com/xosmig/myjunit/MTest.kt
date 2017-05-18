package com.xosmig.myjunit

import kotlin.reflect.KClass

/**
 * Annotating a `public void` with [MTest] marks the method as a test.
 *
 * @property [ignore] if this property is not equal to an empty string, than
 * the test will be ignored and this property is a custom comment on ignoring the test.
 *
 * @property [expected] if this property is set to something apart from its default value,
 * than the test will pass successfully if and only if throws an exception of the given class.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MTest(val ignore: String = "", val expected: KClass<out Throwable> = NoErrorsExpected::class)

internal class NoErrorsExpected internal constructor(): Throwable()
