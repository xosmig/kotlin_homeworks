package com.xosmig.lazy

import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class LazyTest {

    @Test
    fun valueTest() {
        val five = Lazy.value(5)
        assertThat(five.get(), equalTo(5))

        val lazyNull: Lazy<String?> = Lazy.value(null)
        assertTrue(lazyNull.get() === null)

        val hello = "hello"
        val lazyHello1: Lazy<String> = Lazy.value(hello)
        val lazyHello2: Lazy<Any> = lazyHello1
        assertTrue(lazyHello2.get() === hello)
    }

    @Test
    fun threadUnsafeTest() {
        var executed = 0
        val lazy = Lazy.threadUnsafe {
            executed += 1
            "hello, ${Math.random()}"
        }

        assertThat(executed, equalTo(0))
        val result = lazy.get()
        assertThat(executed, equalTo(1))
        assertTrue(lazy.get() === result)
        assertTrue(lazy.get() === result)
        assertThat(executed, equalTo(1))
    }

    @Volatile private var antiOptimizer = 0L
    private fun doSomeWork() {
        for (i in 0..Math.round(Math.random() * 1e5)) {
            antiOptimizer = (antiOptimizer + i) % 123456789
        }
    }

    @Test
    fun threadSafeTest() {
        val result = AtomicReference(null as String?)
        val lazy = Lazy.threadSafe {
            val res = "hello${Math.random()}"
            assertTrue(result.compareAndSet(null, res))
            doSomeWork()
            res
        }

        assertTrue(result.get() === null)
        val answers = arrayOfNulls<String?>(10)
        val threads = Array(10) { i ->
            Thread {
                answers[i] = lazy.get()
            }
        }
        threads.forEach(Thread::start)
        threads.forEach(Thread::join)

        assertTrue(result.get() !== null)
        assertTrue(answers.filter { it !== result.get() }.isEmpty())
    }

    @Test
    fun lockFreeTest() {
        val executed = AtomicInteger(0)
        val lazy = Lazy.lockFree {
            executed.incrementAndGet()
            doSomeWork()
            "hello${Math.random()}"
        }

        assertThat(executed.get(), equalTo(0))
        val answers = arrayOfNulls<String?>(10)
        val threads = Array(10) { i ->
            Thread {
                answers[i] = lazy.get()
            }
        }
        threads.forEach(Thread::start)
        threads.forEach(Thread::join)

        assertTrue(executed.get() >= 1)
        assertTrue(answers.filter { it !== answers[0] }.isEmpty())
    }
}
