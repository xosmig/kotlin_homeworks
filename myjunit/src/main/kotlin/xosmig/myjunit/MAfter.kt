package xosmig.myjunit

/**
 * Annotating a `public void` method with [MAfter] causes that method to be run after every [MTest] method.
 * [MAfter] methods are guaranteed to run even if a Before or Test method throws an exception.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MAfter
