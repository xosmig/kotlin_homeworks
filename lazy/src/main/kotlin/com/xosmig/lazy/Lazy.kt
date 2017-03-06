package com.xosmig.lazy

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

/**
 * An interface for lazy evaluations.
 * Also contains some factory static methods.
 */
interface Lazy<out T> {
    /**
     * Returns the result of the evaluation (computes if necessary).
     * Should always return the same result.
     *
     * @return the result of the evaluation.
     */
    fun get(): T

    companion object {
        /**
         * Wraps a single value into [Lazy].
         *
         * @param[value] value to return from the [get] method.
         * @return [Lazy] which always returns [value] from the [get] method.
         */
        fun<T> value(value: T): Lazy<T> = LazyValue(value)

        /**
         * Wraps [expression] into thread-unsafe [Lazy].
         *
         * In the case of only one thread calls `expression.get()`
         * only once at the moment of the first [get] call.
         * [get] method returns the result of the first `expression.get()` call.
         * The behaviour is undefined in the case of concurrent operations.
         *
         * @param[expression] an expression for lazy evaluation
         * @return [expression] wrapped into thread-unsafe [Lazy].
         */
        fun<T> threadUnsafe(expression: () -> T): Lazy<T> = ThreadUnsafeLazyExpr(expression)

        /**
         * Wraps [expression] into thread-safe [Lazy].
         *
         * Calls `expression.get()` only once at the moment of the first [get] call.
         * [get] method returns the result of the first `expression.get()` call.
         *
         * @param[expression] an expression for lazy evaluation
         * @return [expression] wrapped into thread-safe [Lazy].
         */
        fun<T> threadSafe(expression: () -> T): Lazy<T> = ThreadSafeLazyExpr(expression)

        /**
         * Wraps [expression] into lock-free thread-safe [Lazy].
         *
         * May call `expression.get()` multiple times concurrently, but only when [get] called.
         * [get] always returns the same result,
         * but it's not necessary the result of the first `expression.get()` call.
         *
         * @param[expression] an expression for lazy evaluation
         * @return [expression] wrapped into lock-free thread-safe [Lazy].
         */
        fun<T> lockFree(expression: () -> T): Lazy<T> = LockFreeLazyExpr(expression)
    }
}

private class LazyValue<out T>(private val value: T) : Lazy<T> {
    override fun get(): T {
        return value
    }
}

private class ThreadUnsafeLazyExpr<out T>(expression: () -> T) : Lazy<T> {
    private var expression: (() -> T)? = expression
    private var result: T? = null

    override fun get(): T {
        val expression = this.expression
        if (expression != null) {
            result = expression()
            this.expression = null
        }
        return result as T
    }
}

private class ThreadSafeLazyExpr<out T>(expression: () -> T) : Lazy<T> {
    object NOTHING  // singleton

    @Volatile private var result: Any? = NOTHING
    private var expression: (() -> T)? = expression

    override fun get(): T {
        // Uses double-checked locking.
        var tmp = result
        if (tmp == NOTHING) {
            synchronized(this) {
                tmp = result
                if (tmp == NOTHING) {
                    tmp = expression!!.invoke()
                    result = tmp
                    expression = null
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        return result as T
    }
}


private class LockFreeLazyExpr<out T>(expression: () -> T) : Lazy<T> {
    companion object {
        private val resultUpdater =
                AtomicReferenceFieldUpdater.newUpdater(LockFreeLazyExpr::class.java, Any::class.java, "result")
    }

    object NOTHING  // singleton

    @Volatile private var result: Any? = NOTHING
    private var expression: (() -> T)? = expression

    override fun get(): T {
        val expression = this.expression
        if (resultUpdater.get(this) == NOTHING) {

            resultUpdater.compareAndSet(this, NOTHING, expression!!.invoke())
            this.expression = null
        }

        @Suppress("UNCHECKED_CAST")
        return resultUpdater.get(this) as T
    }
}

