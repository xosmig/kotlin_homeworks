package xosmig.testutils

import java.lang.Math.min
import java.nio.file.Files.*
import java.nio.file.Path
import java.util.*

class RandomUtils(seed: Long = 827): Random(seed) {

    companion object {
        val ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_ "
    }

    fun nextIntClosed(bound: Int) = nextInt(bound + 1)

    fun nextString(length: Int = nextInt(20) + 1, trimmed: Boolean = true): String {
        val builder = StringBuilder()
        for (i in ALPHABET.indices) {
            val spaceAllowed = (i != 1 && i != length) || !trimmed
            builder.append(ALPHABET[nextInt(ALPHABET.length - if (spaceAllowed) {0} else {1})])
        }
        return builder.toString()
    }

    fun nextBoolean(trueProbability: Double = 0.5): Boolean = nextDouble() < trueProbability

    fun randomContent(file: Path, maxSize: Int = 1024) {
        newOutputStream(file).use {
            val content = ByteArray(nextInt(maxSize) + 1)
            nextBytes(content)
            it.write(content)
        }
    }

    fun randomDirectory(root: Path,
                        maxNumberOfDirectories: Int = nextInt(100) + 1,
                        maxNumberOfFiles: Int = nextInt(100) + 1,
                        maxSubdirNumber: Int = nextInt(10) + 1,
                        maxNumOfFilesInOneDir: Int = nextInt(10) + 1,
                        maxFileSize: Int = nextInt(1000) + 1,
                        emptyFileProbability: Double = 0.05,
                        allowEmptyDirectories: Boolean = false) {

        fun impl(root: Path, maxDirsNum: Int, maxFilesNum: Int) {
            val dirs = nextIntClosed(min(maxDirsNum, maxSubdirNumber))
            val files = run {
                val res = nextIntClosed(min(maxFilesNum, maxNumOfFilesInOneDir))
                if (dirs == 0 && res == 0 && !allowEmptyDirectories) { 1 } else { res }
            }

            var dirsLeft = maxDirsNum - dirs
            var filesLeft = maxFilesNum - files

            for (i in 1..dirs) {
                val nextPath = root.resolve(nextString())
                createDirectories(nextPath)
                val spendDirs = nextIntClosed(dirsLeft)
                val spendFiles = nextIntClosed(filesLeft)
                dirsLeft -= spendDirs
                filesLeft -= spendFiles
                impl(nextPath, spendDirs, spendFiles)
            }

            for (i in 1..files) {
                val nextPath = root.resolve(nextString())
                if (nextBoolean(emptyFileProbability)) {
                    // empty file
                    createFile(nextPath)
                    continue
                }
                randomContent(nextPath, maxFileSize)
            }
        }

        impl(root, maxNumberOfDirectories, maxNumberOfFiles)
    }
}
