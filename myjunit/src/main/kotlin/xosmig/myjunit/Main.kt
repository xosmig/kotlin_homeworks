package xosmig.myjunit

import java.nio.file.Paths

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
    var fail = false
    /*Tester(classPath = Paths.get(args[0]), testsPath = Paths.get(args[1])).runAll { result ->
        when (result) {
            is TestResult.Ignored -> {
                println("Test ${}")
            }
            is TestResult.Success -> {

            }
            is TestResult.Fail -> {

            }
        }
    }*/
}
