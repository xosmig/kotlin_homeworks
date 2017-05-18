package com.xosmig.myjunit

import java.io.IOException
import java.nio.file.Paths

val FAIL_EXITCODE = 6
val EXCEPTION_EXITCODE = 12

/**
 * Takes <classPath> and <testsPath> as its command line arguments. Where:
 *
 * <classPath> is the path to the directory which is the root of the hierarchy of classes to be tested.
 *
 * <testsPath> is the path to the directory which is the root of the hierarchy of classes with tests.
 */
fun main(args: Array<String>) {
    if (args.size != 2) {
        System.err.println("Expected exactly 2 argument: <classPath> <testsPath>")
        System.exit(2)
    }

    val summary = try {
        Tester(classPath = Paths.get(args[0]), testsPath = Paths.get(args[1])).runAll { testResult ->
            println(testResult)
        }
    } catch (e: java.nio.file.NoSuchFileException) {
        System.err.println("File not found: ${e.file}")
        System.exit(EXCEPTION_EXITCODE)
        return
    } catch (e: IOException) {
        System.err.println("Oops, something went wrong. Check the arguments.")
        System.err.println(e)
        System.exit(EXCEPTION_EXITCODE)
        return
    }

    println()
    println(summary)
    println()

    System.exit(if (summary.isSuccessful) { 0 } else { FAIL_EXITCODE })
}
