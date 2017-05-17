package xosmig.myjunit

/**
 * Annotating a `public void` method with [MBefore] causes that method to be run before every [MTest] method.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MBefore
